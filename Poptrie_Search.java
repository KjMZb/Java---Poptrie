/**
 * This class is an implementation of Hirochika Asai
 * and Yasuhiro Ohara's Poptrie lookup algorithm, for
 * fast IP lookup.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Poptrie_Search {

	/**
	 * This method is the implementation of Hirochika Asai
     * and Yasuhiro Ohara's Poptrie lookup algorithm. It 
     * takes as its arguments a Poptrie data structure and
     * an IP address. The implementation was guided by their
     * pseudo-code.
     * 
     * Note: In their paper, it is never mentioned what data 
     * type the IP address is. To improve the performance of 
	 * our own implementation, the IP address has been provided 
	 * as a primitive long data type.
     *  
	 * @param    t         The Poptrie data structure that 
	 *                     shall be searched.
	 * @param    key       The IP address for whom we want to 
	 *                     find the FIB index of the next hop. 
	 * @return   Integer   The FIB index stored in a leaf node
	 */
	public Integer lookup( Poptrie t, long key ) {
		
		// Extract the s number of most significant 
		// bits from the IP address
		int index = extract( key, 0, t.s );
		
		// Acquire the direct_index value from the 
		// node in the D array
		int dindex = t.D[index].direct_index;

		// If the most significant bit is a one,
		// then this node is a leaf and its direct_index
		// is the value of the FIB index. Otherwise, 
		// it is an internal node and its direct_index 
		// is its position in the N array.
		if ( ( dindex & ( 1 << 31 ) ) != 0 ) {
			return dindex & ( (1 << 31) - 1);
		}
		index = dindex;
		
		int offset = t.s;
		
		// Acquire the current node's descendant bit vector
		long vector = t.N[index].vector;

		// Extract the next six bits starting at the offset
		int v = extract( key, offset, 6 );
		
		// So long as the position in the vector at v is
		// not zero, then there are more internal nodes 
		// to traverse
		while( ( vector & (1L << v) ) != 0 ){
			
			// Acquire the starting index of this node's
			// children that are internal nodes
			int base = t.N[index].base1;
			
			// Recent versions of Java are said to use the popcnt
			// instruction inside of Long.bitCount.
			// Count the number of ones in the descendant bit vector.
			// This will determine the offset from base1 that the
			// target internal node is located
			int bc = Long.bitCount( vector & ( ( 2L << v ) - 1 ) );
			
			// Calculate the index of the target internal node
			index = base + bc - 1;
			
			vector = t.N[index].vector;
			offset += 6;
			v = extract( key, offset, 6);
		}
		
		// Acquire the starting index of this node's
		// children that are leaf nodes
		int base = t.N[index].base0;

		// Count the number of ones in the leaf bit vector. 
		// This will determine the offset from base0 that
		// the target leaf node is located.
		int bc = Long.bitCount( t.N[index].leafvec & ( ( 2L << v ) - 1 ) );
		
		// Return the FIB index stored in the leaf
		return t.L[base + bc -1].fibIndex;
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
