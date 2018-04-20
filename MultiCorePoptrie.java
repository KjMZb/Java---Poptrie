import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class tests the claims made by Hirochika Asai
 * and Yasuhiro Ohara that their Poptrie lookup algorithm
 * can achieve up to 250 million lookups per-second with
 * four threads. It does so by building a multiway trie 
 * structure from a data set, building a Poptrie data 
 * structure from that multiway trie, and then initializing 
 * threads to use the lookup algorithm to determine the 
 * average number of prefixes for which it can find the next 
 * hop information for (lookups) within a given time frame. 
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class MultiCorePoptrie {
	
	/**
	 * This method constructs a multiway trie structure,
	 * with a stride of six, from the data contained within
	 * the file whose name and path is passed in as an argument.
	 * 
	 * @param    fileName   The path to and file name of the data
	 * @return   Object[]   A tuple containing the multiway trie
	 *                      structure, an ArrayList of next hops,
	 *                      and the number of lines in the file.
	 */
	public Object[] testMultiwayTrie( String fileName ){
	
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

			// Number of lines in the file			
			int lines = 0;
			
			// String content of current line			
			String line;
			while( ( line=reader.readLine() ) != null ){
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
				
				// Add prefix to multiway trie				
				trie.add( ipToBits( prefix ), prefixLen, nextHops.indexOf( hop ) );

			}
			reader.close();
			
			// Return multiway trie, next hops,
			// and the number of lines			
			Object [] tuple = {trie, nextHops, lines};
			return tuple;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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
	 * This method finds the average number of prefixes the 
	 * Poptrie search algorithm can process in a given time
	 * frame using a specified number of threads.
	 *  
	 * @param    fileName     The path to and file name of the data
	 * @param    pop          Poptrie data structure
	 * @param    searcher     Poptrie search algorithm
	 * @param    hops         Next hops
	 * @param    threads      The number of threads to be used
	 * @param    time         The number of seconds before time out
	 * @param    fileLength   Number of lines in the file
	 * @return
	 */
	public int tester( String fileName, Poptrie pop, Poptrie_Search searcher,
			           ArrayList<String> hops, int threads, int time, int fileLength ) {
		
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
			
			// Initialize thread arrays
			Thread[] multiThread = new Thread [threads];
			Poptrie_Lookup [] lookups = new Poptrie_Lookup[threads];
			
			// Generate random numbers to be used to 
			// select random prefixes to be used in
			// the search
			Random rand = new Random();
			int number = 380000000;
			int [] lineNumbers = new int [number];
			
			// Create arrays of random numbers and create threads
			for( int c = 0; c < threads; c++ ) {
				for( int z = 0; z < number; z++ ) {
					lineNumbers[z] = rand.nextInt( fileLength );
				}
				lookups[c] = new Poptrie_Lookup( pop, searcher, lineNumbers, prefixes, time );
				multiThread[c] = new Thread( lookups[c] );
			}
			
			// Start threads
			for( int s = 0; s < threads; s++ ) {
				multiThread[s].start();
			}
			
			// Total of completed searches			
			int total = 0;			
			
			// Wait for threads to finish and extract total
			for( int w = 0; w < 4; w++ ) {
				try {
					multiThread[w].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				total += lookups[w].totalLookUps();
			}
						
			return total;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
		
	}
	
	/**
	 * The main method. It find the average number of 
	 * lookups per second any number of threads can find
	 * in total per second using the Poptrie search algorithm.
	 * @param   args   Command line arguments (not used)
	 */
	@SuppressWarnings("unchecked")
	public static void main( String [] args ) {

		// Time how long it takes to build the multiway trie and
		// Poptrie data structures from the data		
		long start = System.currentTimeMillis();
		Object [] tuple = new MultiCorePoptrie().testMultiwayTrie( "C:/Users/Kyle McGlynn/workspace/Poptrie/src/destinations.txt");
		
		// Build Poptrie structure
		Poptrie popcorn = new Poptrie( (MultiwayTrie)tuple[0], 12 );
		
		// "FIB/RIB"
		ArrayList<String> hops = (ArrayList<String>)tuple[1];
		
		// Number of lines in the file
		int lines = (Integer)tuple[2];
		
		System.out.println("Setup time: " + (System.currentTimeMillis() - start) );
		
		// Poptrie search algorithm		
		Poptrie_Search searcher = new Poptrie_Search();

		// Find the average number of hops for the
		// given time frames. 		
		for( int i = 5; i <= 25; i+=5 ){
			int total = new MultiCorePoptrie().tester( "C:/Users/Kyle McGlynn/workspace/Poptrie/src/destinations.txt", popcorn, searcher, hops, 4, i, lines);			
			System.out.println( total/i );
		}
	}
}