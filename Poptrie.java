/**
 * This class is an implementation of Hirochika Asai
 * and Yasuhiro Ohara's Poptrie data structure. It is
 * meant to support their Poptrie lookup algorithm, for
 * fast IP lookup, which has been implemented according
 * to their pseudo-code in the Poptrie_Search class.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Poptrie {
	
	// The array of internal nodes
	Poptrie_Node [] N;
	
	// The array of leaf nodes
	Poptrie_Node [] L;
	
	// The array used in direct pointing
	Poptrie_Node [] D;
	
	// The number of most significant bits
	int s;
	
	// The number of bits used in every stride
	int stride = 6;
	
	/**
	 * The constructor. It takes as arguments the multiway
	 * trie structure that the Poptrie structure will be
	 * based off of, as well as the number of most significant
	 * bits used for direct pointing.
	 * @param   trie   The multiway trie structure that will
	 *                 be used to build the Poptrie structure
	 * @param   s      The number of most significant bits 
	 *                 used for direct pointing
	 */
	public Poptrie( MultiwayTrie trie, int s) {
		
		this.s = s;
		
		// Initialize the D, L, and N arrays
		this.D = new Poptrie_Node[1<<s];
		this.N = new Poptrie_Node[trie.getInternalSize()];
		this.L = new Poptrie_Node[trie.getLeafSize()];

		// Make the root node
		Poptrie_Node root = new Poptrie_Node( trie.getRoot() );
		N[0] = root;

		// Build the Poptrie structure
		iterBuildPop();		
	}
	
	/**
	 * This method iteratively builds the Poptrie structure
	 * by traversing over the N array.
	 */
	private void iterBuildPop() {
		
		// Current position in the N array
		int n_point = 0;
		
		// These will become the base1 and base0
		// values of the current Poptrie_Node.
		int startBase1 = 1;
		int startBase0 = 0;
		
		// If s=0, then direct pointing is turned off.
		// However, to avoid wasteful and time consuming 
		// checks in the lookup algorithm, D is populated 
		// by the root node.
		if( s == 0 ) {
			D[0] = N[0];
			N[0].direct_index = 0;
		}
		
		// Iterate over the N array and build the Poptrie structure.
		// This process continues until there are no more nodes in
		// the N array.
		int length = N.length;
		while( n_point < length ) {
			
			// Grab the current Poptrie_Node
			Poptrie_Node popnode = N[n_point];
			
			// Update to be consistant with the values calculated
			// in the last round
			int newBase1 = startBase1;
			int newBase0 = startBase0;
			
			// Used to determine chunks of contiguity
			// in the current node's leaf children
			int currentLeaf = -1;
			
			// Loop over the current node's children
			int childrenLength = popnode.node.children.length;		
			for( int i = 0; i < childrenLength; i++ ) {
				
				// Does the child exist?
				if( popnode.node.children[i] != null ) {			
				
					// If the child's leaf flag is set to true
					if( popnode.node.children[i].leaf ) {
						
						// Acquire the FIB index stored in the leaf.
						int newLeaf = popnode.node.children[i].fibIndex;
						
						// Whether or not this will be placed in the L array, it
						// is created for potential placement in the D array.
						Poptrie_Node popcorn = new Poptrie_Node( popnode.node.children[i].fibIndex );
										
						// If newLeaf != currentLeaf, then the contiguity of
						// the FIB indexes in the leaves has been broken, so
						// this leaf should be recorded in leafvec and the L array.
						if( newLeaf != currentLeaf ) {
							currentLeaf = newLeaf;							
							L[newBase0++] = popcorn;
							popnode.leafvec = popnode.leafvec ^ (1L << i); 
						}
						
						// If this leaf exists at the level indicated by s, then
						// we include it in the D array.
						if( popnode.node.children[i].level == s) {
							D[popnode.node.children[i].prefixValue] = popcorn;
							
							// Set the most significant bit to 1, indicating this is a leaf node
							int bit = 1 << 31;
							popcorn.direct_index = bit | popcorn.fibIndex;
						}
						
						// If this leaf at a level below s, we must perform prefix
						// expansion to fill out the D array.
						else if( popnode.node.children[i].level < s ){
							
							// Determine the difference between the level and s
							int shift = s - popnode.node.children[i].level;
							
							// Add a number of zeroes to the left of the prefix value
							// equal to the shift
							int lowest = popnode.node.children[i].prefixValue << shift;
							
							// Add a number of ones to the left of the prefix value
							// equal to the shift							
							int highest = lowest ^ ( ( 1 << shift ) - 1 );
							
							// Loop over the corresponding positions in the D
							// array, populating each with a leaf node.
							for( int z = lowest; z <= highest; z++ ) {
								D[z] = new Poptrie_Node( popnode.node.children[i].fibIndex );
								int bit = 1 << 31;
								D[z].direct_index = bit | popcorn.fibIndex;
							}
						}
						
						// If base0 has not been set yet
						if( popnode.base0 == -1 ) { 
							popnode.base0 = startBase0; 
						}
					}
					
					else {
						
						// Must be an internal node
						Poptrie_Node popcorn =  new Poptrie_Node( popnode.node.children[i] );
	
						N[newBase1] = popcorn;
						
						popnode.vector = popnode.vector ^ (1L << i);
						
						// If this node exists at the level indicated by s, then
						// we include it in the D array.
						if( popcorn.node.level == s ) {		
							D[popcorn.node.prefixValue] = popcorn;
							popcorn.direct_index = newBase1;
						}
						
						// If base1 has not been set yet
						if( popnode.base1 == -1 ) {
							popnode.base1 = startBase1;
						}					
						newBase1++;
					}
				}
			}
			
			// Destroy the reference to the corresponding
			// node from the multiway trie to free space
			popnode.node = null; // comment out this line of code for debugging
			n_point++;
			startBase1 = newBase1;
			startBase0 = newBase0;			
		}
	}
}