/**
 * This class simulates the search of a Poptrie
 * structure using the Poptrie search algorithm
 * for fast IP lookup in a multi-threaded environment.
 * This is only a simulation since, unlike a real
 * router, this thread starts off with a collection
 * randomly selected integers that it will use to
 * select a prefix. The next hop information for
 * the chosen prefix shall than be looked up in
 * the Poptrie structure. A running total of
 * completed lookups before timeout is recorded.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Poptrie_Lookup implements Runnable{
	
	// The running total of completed lookups
	private int total = 0;
	
	// The Poptrie structure to be searched
	private Poptrie pop;
	
	// The Poptrie search algorithm
	private Poptrie_Search searcher;
	
	// An array of randomly chosen integers that
	// correspond to prefixes in the data
	private int [] lineNumbers;
	
	// An array of prefixes created from the data
	private long [] prefixes; 
	
	// The amount of time in seconds that
	// the thread will run before finishing
	private int time;
	
	/**
	 * The constructor. It initializes the Poptrie data
	 * structure, the Poptrie search algorithm, the random
	 * number and prefix arrays, and the time that this
	 * thread shall run for in seconds.
	 * 
	 * @param   pop           The Poptrie data structure to
	 *                        be searched
	 * @param   searcher      The Poptrie search algorithm
	 * @param   lineNumbers   An array of randomly chosen integers
	 *                        that correspond to prefixes in the data
	 * @param   prefixes      An array of prefixes created from
	 *                        the data
	 * @param   time          The number of seconds this thread
	 *                        shall run before exiting
	 */
	public Poptrie_Lookup( Poptrie pop, Poptrie_Search searcher, 
			               int [] lineNumbers, long [] prefixes, int time ) {
		this.pop = pop;
		this.searcher = searcher;
		this.lineNumbers = lineNumbers;
		this.prefixes = prefixes;
		this.time = time;
	}
	
	/**
	 * The run method of this thread that is invoked by
	 * the start method.
	 */
	public void run(){
		
		// The time in miliseconds that this thread 
		// shall run for before exiting
		long timeInMS = time * 1000;
		
		// Start timing
		long begin = System.currentTimeMillis();
		
		// Until the timeout is reached, keep searching
		while( System.currentTimeMillis() - begin < timeInMS ) {
			searcher.lookup( pop, prefixes[ lineNumbers[ total++ ] ] );
		}
	}
	
	/**
	 * This method returns the total number of lookups that
	 * this thread was able to achieve before timeout.
	 * 
	 * @return   int   The total number of lookups achieved
	 */
	public long totalLookUps(){
		return total;
	}
}