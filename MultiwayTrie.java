/**
 * This class is an implementation of a multiway trie
 * data structure for IP lookup. It is used to build
 * a Poptrie data structure, so no lookup methods
 * have been implemented.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class MultiwayTrie {
	
	// Number of bits from the IP address used
	// to determine the next node
	private int prefixBits;
	
	// The root node of the multiway trie structure
	private Node root;
	
	// Number of internal nodes in the multiway trie structure
	private int internalSize;
	
	// Number of leaf nodes in the multiway trie structure
	private int leafSize;
	
	/**
	 * The constructor. It takes as an argument the number
	 * of bits that shall be used for the stride. 
	 * @param   prefixBits   The number of bits that shall
	 *                       be used for the stride.
	 */
	public MultiwayTrie( int prefixBits ) {
		this.prefixBits = prefixBits;
		this.root = new Node(prefixBits);
		this.internalSize = 1;
		this.leafSize = 0;
	}
	
	/**
	 * This method takes an IP address, its prefix length,
	 * and its next hop's corresponding index in the FIB and 
	 * creates an entry in the multiway trie structure.
	 * @param   ip          The IP address entered as a primitive
	 *                      long for fast extraction
	 * @param   prefixLen   The length of the prefix
	 * @param   fibIndex    The index in the FIB of this IP 
	 *                      address's next hop
	 */
	public void add( long ip, int prefixLen, Integer fibIndex ) {
		priv_add( ip, prefixLen, fibIndex, root, 0, prefixBits );
	}
	

	/**
	 * This method performs the actual addition of an IP, its
	 * prefix length, and IP address into the structure. It does
	 * so by recursively iterating over the data structure, looking
	 * for the proper place to make the entry.
	 * @param   ip          The IP address entered as a primitive
	 *                      long for fast extraction
	 * @param   prefixLen   The length of the prefix
	 * @param   fibIndex    The index in the FIB of this IP
	 *                      address's next hop
	 * @param   current     The current node in the structure
	 * @param   offset      The offset in the IP address from
	 *                      which to extract bits
	 * @param   level       The current level in the structure
	 */
	private void priv_add( long ip, int prefixLen, Integer fibIndex, Node current, int offset, int level ) {
			
		// Determine the index in the child node array
		int index = extract( ip, offset, prefixBits );
	
		// Are the next six bits the end of the prefix?
		if( prefixLen == level ) {
			
			// Is there already a child at the index?
			if( current.children[index] == null ) {
				current.children[index] = new Node( prefixBits );
				current.children[index].leaf = true;
				current.children[index].prefixLen = prefixLen;
				current.children[index].level = level;
				current.children[index].fibIndex = fibIndex;
				current.children[index].prefixValue = extract( ip, 0, level );
				current.children[index].ip = ip;
				leafSize++;
			}
			else {
				
				// Is the child a leaf? Then change it to this fib index.
				if( current.children[index].leaf ) {
					current.children[index].fibIndex = fibIndex;
					current.children[index].ip = ip;
					current.children[index].prefixLen = prefixLen;
					current.children[index].prefixValue = extract( ip, 0, level );
				} 
	
				// The child must be an internal node
				else {
					
					// Expand this prefix downwards, just in case it is the only one for the fibIndex
					holepunch( ip, prefixLen, fibIndex, current.children[index], offset + prefixBits, level + prefixBits ); 
				}
			}
		}
	
	
		// Prefix exceeded
		else if( prefixLen < level ) {
			
			// Since the prefix is exceeded, we need to expand the prefix
			holepunch( ip, prefixLen, fibIndex, current, offset, level );

		}
		
		// Since we have not yet met or exceeded the prefix,
		// then the next node must be an internal node
		else {
	
			// Is there already a child?
			if( current.children[index] == null ) {
				current.children[index] = new Node( prefixBits );
				current.children[index].level = level;
				current.children[index].prefixValue = extract( ip, 0, level );
				internalSize++;
			}
			else{
				
				// Is it a leaf? If so, change it to an internal node
				// and expand the leaf's prefix
				if( current.children[index].leaf ) {
					current.children[index].leaf = false;
					holepunch( current.children[index].ip, current.children[index].prefixLen, 
							   current.children[index].fibIndex, current.children[index], 
							   offset + prefixBits, level + prefixBits ); 
					current.children[index].ip = ip;
					current.children[index].prefixValue = extract( ip, 0, level );
					current.children[index].level = level;
					internalSize++;
				}
			}
			
			priv_add( ip, prefixLen, fibIndex, current.children[index], offset + prefixBits, level + prefixBits );
		}	
	}
	
	/**
	 * This method performs prefix expansion of the given IP
	 * address at the given node. If need be, it will expand
	 * further down the structure than the given node.
	 * @param   ip          The IP address entered as a primitive
	 *                      long for fast extraction
	 * @param   prefixLen   The length of the prefix
	 * @param   fibIndex    The index in the FIB of this IP
	 *                      address's next hop
	 * @param   current     The current node in the structure
	 * @param   offset      The offset in the IP address from
	 *                      which to extract bits
	 * @param   level       The current level in the structure
	 */
	private void holepunch( long ip, int prefixLen, Integer fibIndex, Node current, int offset, int level ) {
		
		// In the case that the prefix length is less than the current offset,
		// we want to determine the next stride of bits, since it can be assumed
		// that the difference between the prefix length and offset has been found
		// and the possible values enumerated. However, if the prefix
		// length is greater than the offset, then we want to know the difference
		// between the prefix length and the current level.
		int len = prefixLen < offset ?   ( level - offset ) : ( level - prefixLen ); 
		
		// Determine the lowest possible value of the prefix expansion
		int lowest = extract( ip, ( prefixLen < offset ? offset : prefixLen ), len);
	
		// Find the highest possible value of the prefix expansion
		int highest = lowest | ( ( 1 << len ) - 1 );
		
		// Determine the value of the IP address up to this level
		int prefix = extract( ip, 0, level );
		
		// Determine the value of the stride of bits
		int kbits = extract( ip, offset, prefixBits );
		
		// Send the value down by combining the value
		// of the stride of bits with the lowest to 
		// the highest possible prefix expansions
		for( int i = lowest; i <= highest; i++ ) {
			
			// Is the spot empty?
			if( current.children[kbits+i] == null ) {
				current.children[kbits+i] = new Node( prefixBits );
				current.children[kbits+i].leaf = true;
				current.children[kbits+i].level = level;
				current.children[kbits+i].prefixLen = prefixLen;
				current.children[kbits+i].fibIndex = fibIndex;
				current.children[kbits+i].prefixValue = prefix | i;
				current.children[kbits+i].ip = ip;
				leafSize++;				
			}
	
			// Is it an internal node? If so, expand the prefix further
			else if( !current.children[kbits+i].leaf ) {
				holepunch( ip, prefixLen, fibIndex, current.children[kbits+i], offset+prefixBits, level+prefixBits ); 
			}
			
			// If this leaf has a more specific prefix ( a longer prefix ) than
			// the indicated leaf node, than replace the leaf node's values
			// with this FIB index, IP address, prefix value, and prefix length.
			else if ( current.children[kbits+i].prefixLen < prefixLen ) {
				current.children[kbits+i].fibIndex = fibIndex;
				current.children[kbits+i].prefixValue = prefix | i;
				current.children[kbits+i].ip = ip;
				current.children[kbits+i].prefixLen = prefixLen;
			}
		}
	}
	
	/**
	 * This method returns the root node
	 * of the multiway trie structure.
	 * @return   Node   The root node  
	 */
	public Node getRoot() {
		return root;
	}
	
	/**
	 * This method returns the number of
	 * bits used in the stride.
	 * @return   int   The number of bits
	 *                 used in the stride
	 */
	public int getBits() {
		return prefixBits;
	}
	
	/**
	 * This method returns the number of
	 * internal nodes in the structure
	 * @return   int   The number of
	 *                 internal nodes
	 */
	public int getInternalSize() {
		return	internalSize;
	}
	
	/**
	 * This method returns the number of
	 * leaf nodes in the structure
	 * @return   int   The number of leaf 
	 *                 nodes in the structure.
	 */
	public int getLeafSize() {
		return	leafSize;
	}	
	
	/**
	 * Extract integer values of specified length of bits
	 * at the specified offset within a bit representation
	 * of an IPv4 address.
	 * @param    key      The IP address represented as a primitive
	 *                    long for fast extraction
	 * @param    offset   The starting position from which
	 *                    extraction shall begin.
	 * @param    len      The length of the region to be extracted
	 * @return   int      The integer value of the extracted bits
	 */
	public int extract( long key, int offset, int len ) {
		
		// Determine how far into the bit representation
		// we must shift
		long shift = 31 - len - offset + 1;
		
		// Construct a mask that shall extract the desired bits
		long left = ( ( 1L << len ) - 1 ) << shift;
		
		// Apply the mask
		long and = key & left;
		
		// Shift back to the right so that the value of 
		// the extracted bits is returned.
		return (int) and >>> shift;
	}
	
}