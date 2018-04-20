/**
 * This class represents the nodes of a
 * multiway trie data structure. The multiway
 * trie data structure is then used to build
 * a Poptrie data structure.
 * 
 * @author Krishna Neupane
 * @author Bhaarat Pachori
 * @author Kyle McGlynn
 */
public class Node{
	
	// The internal and leaf node children
	// of this node
	Node [] children;
	
	// Indicates whether this node is 
	// a leaf or not.
	boolean leaf = false;
	
	// The FIB index stored in this
	// node if it is a leaf.
	Integer fibIndex;
	
	// The level within the multiway trie at 
	// which this node was created
	int level;
	
	// The value of the number of most significant bits
	// equal to the level of this node's IP address
	int prefixValue;
	
	// The prefix length of the IP address stored if
	// this node is a leaf node.
	int prefixLen;
	
	// The IP address stored in this
	// node if it is a leaf node
	long ip;
	
	/**
	 * The constructor. It takes as its argument the
	 * number of bits that shall be used as the stride
	 * in the multiway trie data structure.
	 * 
	 * @param   prefixBits   The number of bits used in
	 *                       the stride of the multiway
	 *                       trie data structure.
	 */
	public Node(int prefixBits){
		this.children =  new Node[(1 << prefixBits)];
	}
	
}