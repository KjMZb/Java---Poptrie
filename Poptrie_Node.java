/**
 * This class represents the nodes of a
 * Poptrie data structure. They contain
 * descendant and leaf bit vectors according
 * to the the Poptrie structure described 
 * in Hirochika Asai and Yasuhiro Ohara's paper.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Poptrie_Node {
	
	// The descendant bit vector, represented
	// by a primitive long value. Since Java 
	// provides 64 bits for a primitive long,
	// this is perfect to represent the 64 
	// possibilities of a six bit stride used
	// by the authors.
	long vector = 0L;
	
	// The leaf bit vector, represented
	// by a primitive long value. Since Java 
	// provides 64 bits for a primitive long,
	// this is perfect to represent the 64 
	// possibilities of a six bit stride used
	// by the authors.
	long leafvec = 0L;
	
	// The index of this node'ss first internal
	// child node in the N array.
	int base1 = -1;
	
	// The index of this node'ss first leaf
	// child node in the L array.	
	int base0 = -1;
	
	// This node's direct_index value. If its
	// most significant bit is a one, than this
	// node is a leaf and the remaining bits are
	// the FIB index. Otherwise, it is a internal
	// node and the remaining bits are its index
	// within the N array.
	int direct_index;
	
	// If this node is a leaf, then it contains
	// the FIB index of the matching prefix's
	// next hop.
	Integer fibIndex;
	
	// A reference to a corresponding node in
	// the multiway trie structure used to build
	// the Poptrie structure. Once this has been
	// processed, this reference is set to null 
	// to save memory.
	Node node;
	
	/**
	 * This constructor is used to create internal 
	 * nodes within the Poptrie structure
	 * @param   node   The corresponding internal
	 *                 node in the multiway trie
	 *                 structure
	 */
	public Poptrie_Node( Node node ){
		this.node = node;
	}
	
	/**
	 * This constructor is used to create leaf 
	 * nodes within the Poptrie structure
	 * @param   fibIndex   The FIB index of this
	 *                     prefix's next hop.
	 */
	public Poptrie_Node( Integer fibIndex ) {
		this.fibIndex = fibIndex;
	}
	
}
