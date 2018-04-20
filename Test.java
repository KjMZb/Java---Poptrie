import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class tests the claims made by Hirochika Asai
 * and Yasuhiro Ohara that their Poptrie lookup algorithm
 * can achieve 90 million lookups per-second on a single 
 * thread. It does so by building a multiway trie structure 
 * from a data set, building a Poptrie data structure from 
 * that multiway trie, and then uses the lookup algorithm 
 * to determine the average number of prefixes it can determine 
 * the next hop information for (lookups) within a given time 
 * frame. 
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Test {
	

	/**
	 * This method constructs a multiway trie structure,
	 * with a stride of six, from the data contained within
	 * the file whose name and path is passed in as an argument.
	 * 
	 * @param    fileName   The path to and file name of the data
	 * @return   Object[]   A tuple containing the multiway trie
	 *                      structure, an ArrayList of next hops,
	 *                      a HashMap of prefix, next hop pairs,
	 *                      and the number of lines in the file.
	 */
	public Object [] buildMultiwayTrie( String fileName ){
	
		try {
			
			// Establish a connection to the file
			BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
			
			// Initialize the multiway trie structure. A stride 
			// of six is used since the authors used a stride of
			// six in their Poptrie structure. Six is used to fit
			// the size of registers in 64-bit CPUs.
			MultiwayTrie trie = new MultiwayTrie( 6 );
			
			// Unique next hops contained within the data
			ArrayList<String> nextHops = new ArrayList<String>();
			
			// Maps prefix to next hop
			HashMap<String,String> mapping = new HashMap<String,String>();
			
			// Number of lines in the file
			int lines = 0;
			
			// String content of current line
			String line;
			while( ( line = reader.readLine() ) != null ){
				
				lines++;
				
				// Break line into parts
				String[] parts = line.split(" ");
				
				// Obtain prefix
				String prefix = parts[0].split("/")[0];
				
				// Obtain prefix length
				int prefixLen = Integer.parseInt( parts[0].split("/")[1] );
				
				// Obtain next hop
				String hop = parts[1];
								
				// If the next hop is already recorded, move along
				if( !nextHops.contains( hop ) ) {
					nextHops.add( hop );
				}
				
				// Enter prefix next hop pair into map
				mapping.put( parts[0], hop);
				
				// Add prefix to multiway trie
				trie.add( ipToBits( prefix ), prefixLen, nextHops.indexOf( hop ) );
			}
			reader.close();
			
			// Return multiway trie, next hops, map of prefix
			// next hop pairs, and the number of lines
			Object [] tuple = { trie, nextHops, mapping, lines};
			return tuple;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method returns the correctness of the Poptrie data
	 * structure by searching for the next hop of every prefix
	 * within the data. Note: The results are typically a fraction
	 * of a percent less than 100, this has to do with certain 
	 * prefixes becoming un-reachable. Most likely, these isolated
	 * prefixes are the prefixes of networks but not the user 
	 * machines on the networks.
	 * 
	 * @param    fileName     The path to and file name of the data
	 * @param    pop          Poptrie data structure
	 * @param    searcher     Poptrie search algorithm
	 * @param    hops         Next hops
	 * @param    mapping      Prefix - next hop pairs
	 * @param    fileLength   Number of lines in the file
	 * @return   float        Percent of correctly found next hops
	 */
	public float correctness( String fileName, Poptrie pop, Poptrie_Search searcher,
			                 ArrayList<String> hops, HashMap<String,String> mapping, int fileLength ) {
		
		try {
			
			// Establish a connection to the file
			BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
			
			// Read lines into a string array
			String [] lines = new String [fileLength];
			for( int i = 0; i < fileLength; i++ ) {
				lines[i] = reader.readLine();
			}
			reader.close();
			
			// Initialize array to contain Long versions
			// of the prefixes
			long [] prefixes = new long [fileLength];
			for( int j = 0; j < fileLength; j++ ) {
				String line = lines[j];
				prefixes[j] = ipToBits( line.split(" ")[0].split("/")[0] );
			}
			
			// Total number of correctly found next hops
			float total = 0;
			
			//float bad = 0; // Used in debugging
			//int lowest = 32; // Used in debugging
			
			// For every prefix in the data, find its
			// next hop within the Poptrie structure
			for( int a = 0; a < fileLength; a++ ) {
				long prefix = prefixes[a];
				int index = searcher.lookup( pop, prefix );
				
				// Return next hop based upon returned index
				String nextHopCandidate = hops.get( index );
				
				// Compare next hop referenced by returned index
				// to recorded correct next hop. If correct, 
				// increment total number of correct.
				String nextHopReal = mapping.get( lines[a].split(" ")[0] );
				if( nextHopCandidate.equals( nextHopReal ) ) {
					total++;
				}
				
// This commented out section is used in debugging
//				else{
//					bad++;
//					int prefixLen = Integer.parseInt( lines[a].split(" ")[0].split("/")[1] );
//					lowest = prefixLen < lowest ? prefixLen : lowest;
//				}
				
			}
			
			return (total/fileLength) * 100;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}	
	}
	
	/**
	 * This method finds the average number of prefixes the 
	 * Poptrie search algorithm can process in a given time
	 * frame.
	 *  
	 * @param    fileName     The path to and file name of the data
	 * @param    pop          Poptrie data structure
	 * @param    searcher     Poptrie search algorithm
	 * @param    hops         Next hops
	 * @param    time         The number of seconds before time out
	 * @param    fileLength   Number of lines in the file
	 * @return
	 */
	public long tester( String fileName, Poptrie pop, Poptrie_Search searcher,
			            ArrayList<String> hops, int time, int fileLength ) {	
		
		try {
			
			// Establish a connection to the file
			BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
			
			// Read lines into a string array
			String [] lines = new String [fileLength];
			for( int i = 0; i < fileLength; i++ ) {
				lines[i] = reader.readLine();
			}
			reader.close();
			
			// Initialize array to contain Long versions
			// of the prefixes			
			long [] prefixes = new long [fileLength];
			for( int j = 0; j < fileLength; j++ ) {
				String line = lines[j];
				prefixes[j] = ipToBits( line.split(" ")[0].split("/")[0] );

			}
			
			// Generate random numbers to be used to 
			// select random prefixes to be used in
			// the search
			Random rand = new Random();
			int number = 380000000;
			int [] lineNumbers = new int [number];
			for( int z = 0; z < number; z++ ) {
				lineNumbers[z] = rand.nextInt( fileLength );
			}
			
			// Total of completed searches
			int total = 0;
			
			// Time to run in miliseconds
			long timeInMS = time * 1000;
			
			// Begin timing
			long begin = System.currentTimeMillis();
			
			// Continue searching until timeout
			while( System.currentTimeMillis() - begin < timeInMS  ) {
				searcher.lookup( pop, prefixes[ lineNumbers[ total++ ] ] );
			}
			
			// Return the average
			return total/time;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;	
	}
	
	/**
	 * Converts an IPv4 address given as a string into 
	 * a bit vector represented by a primitive long. A
	 * int is not used, since an int in Java can only
	 * represent 2^{31}-1 values.
	 * @param    key    The IPv4 address as a string.
	 * @return   long   The bit vector representation of
	 *                  the IPv4 address.
	 */	
	private long ipToBits( String key ) {
		
		// Break the address into parts at the periods
		String[] parts = key.split("\\.");
		long bits = 0;
		long offset = 7;
		for( String part: parts ) {
			long asLong = Long.parseLong( part );
			bits = bits ^ ( asLong << 31 - offset );
			offset += 8;
		}
		return bits;
	}
	
	/**
	 * The main method. It sets up and runs the various 
	 * tests of the Poptrie search algorithm. It performs
	 * three types of searches: correctness, result, and 
	 * average number of lookups per second. Correctness
	 * finds the number of correctly found next hops, while
	 * result finds the next hop of particular prefixes.
	 * The average number of lookups per second test finds
	 * how many prefixes the Poptrie search algorithm can
	 * process in a second by taking an average over a 
	 * given time period.
	 * @param   args   Command line arguments (not used)
	 */
	@SuppressWarnings("unchecked")
	public static void main( String [] args ) {
		
		// Time how long it takes to build the multiway trie and
		// Poptrie data structures from the data
		long start = System.currentTimeMillis();
		Test testing = new Test();
		Object [] tuple = testing.buildMultiwayTrie( "C:/Users/Kyle McGlynn/workspace/Poptrie/src/destinations.txt" );
		
		// Build Poptrie structure
		Poptrie popcorn = new Poptrie( (MultiwayTrie) tuple[0], 0 );
		
		System.out.println("Build time in ms: " + (System.currentTimeMillis() - start));
		
		// "FIB/RIB"/next hops
		ArrayList<String> hops = (ArrayList<String>) tuple[1];
		
		// Prefix - next hop pairs
		HashMap<String, String> real = (HashMap<String,String>) tuple[2];
		
		// Number of lines in the data
		int fileLength = (Integer) tuple[3];
		
		// Poptrie search algorithm
		Poptrie_Search searcher = new Poptrie_Search();
				
		// Percentage of correctly found next hops
		float correct = new Test().correctness( "C:/Users/Kyle McGlynn/workspace/Poptrie/src/destinations.txt",
				                                popcorn, searcher, hops, real, fileLength );
		System.out.println("Percent correct: " + correct );
		
		// Find the next hop of the specific prefix
		long bits = testing.ipToBits( "41.206.16.0" );
		Integer index = searcher.lookup( popcorn, bits );
		System.out.println( hops.get( index ) );
//		
//		long bits1 = Conversions.ipToBits( "32.7.32.0" );
//		Integer index1 = searcher.lookup( popcorn, bits1 );
//		System.out.println( hops.get( index1 ) );
//
//		long bits2 = Conversions.ipToBits( "84.108.100.0" );
//		Integer index2 = searcher.lookup( popcorn, bits2 );
//		System.out.println( hops.get( index2 ) );
//		
//		long bits3 = Conversions.ipToBits( "145.128.16.0" );
//		Integer index3 = searcher.lookup( popcorn, bits3 );
//		System.out.println( hops.get( index3 ) );
//		
//		long bits4 = Conversions.ipToBits( "112.176.0.0" );
//		Integer index4 = searcher.lookup( popcorn, bits4 );
//		System.out.println( hops.get( index4 ) );				
//		
//		long bits5 = Conversions.ipToBits( "75.160.0.0" );
//		Integer index5 = searcher.lookup( popcorn, bits5 );
//		System.out.println( hops.get( index5 ) );
//		
//		long bits6 = Conversions.ipToBits( "65.192.0.0" );
//		Integer index6 = searcher.lookup( popcorn, bits6 );
//		System.out.println( hops.get( index6 ) );		
//
//		long bits7 = Conversions.ipToBits( "197.30.168.0" );
//		Integer index7 = searcher.lookup( popcorn, bits7 );
//		System.out.println( hops.get( index7 ) );
//
//		long bits8 = Conversions.ipToBits( "39.249.0.0" );
//		Integer index8 = searcher.lookup( popcorn, bits8 );
//		System.out.println( hops.get( index8 ) );	
		
//		long bits9 = Conversions.ipToBits( "24.54.112.0" );
//		long bits9 = Conversions.ipToBits( "24.54.112.0" );
//		Integer index9 = searcher.lookup( popcorn, bits9 );

		// Find the average number of hops for the
		// given time frames. 
		for( int i = 5; i <= 25; i += 5 ) {
			long total = new Test().tester( "C:/Users/Kyle McGlynn/workspace/Poptrie/src/destinations.txt", popcorn, searcher, hops, i, fileLength );			
			System.out.println( total + " Mlps" );
		}
	}
}