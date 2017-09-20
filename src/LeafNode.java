import java.util.*;

/**
   LeafNodes of B+ trees
 */
public class LeafNode extends Node {

	/**
       Construct a LeafNode object and initialize it with the parameters.
       @param d the degree of the leafnode
       @param k the first key value of the node
       @param n the next node
       @param p the previous node
	 */
	public LeafNode(int d, int k, Node n, Node p) {
		super (d, n, p);
		keys[1] = k;
		lastindex = 1;
	}


	public void outputForGraphviz() {

		// The name of a node will be its first key value
		// String name = "L" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();

		// Now, prepare the label string
		String label = "";
		for (int j = 0; j < lastindex; j++) {
			if (j > 0) label += "|";
			label += String.valueOf(keys[j+1]);
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/**
	the minimum number of keys the leafnode should have.
	 */
	public int minkeys() {
		int min = 0;
		///////////////////
		// ADD CODE HERE //
		///////////////////
		min = (int)Math.ceil((degree - 1) / 2.0);
		return Math.max(min, 0);
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined.
       @return true if this node can be combined with other; otherwise false.
	 */
	public boolean combinable(Node other) {
		///////////////////
		// ADD CODE HERE //
		///////////////////
		return lastindex + other.lastindex <= maxkeys();
	}

	/**
       Combines contents of this node and its next sibling (nextsib)
       into a single node
	 */
	public void combine() {
		///////////////////
		// ADD CODE HERE //
		///////////////////

		if (next == null) {
			return;
		}

		System.out.println("combine leaf:\t\t" + Arrays.toString(keys) + lastindex + "\t" + Arrays.toString(next.keys) + next.lastindex);

		Node nextNode = next;
		// transfer next sibling node's date into current node
		for (int i = 1; i <= next.lastindex; i++) {  // start from 1
			lastindex += 1;
			keys[lastindex] = next.keys[i];
			ptrs[lastindex] = next.ptrs[i];
		}
		// connect node, delete nextNode
		Node nextNextNode = nextNode.next;
		this.next = nextNextNode;
		if (nextNextNode != null) {
			nextNextNode.prev = this;
		}
		// deal with parent. nextNode
		Node parentNode = nextNode.parentref.getNode();
		int parentIndex = nextNode.parentref.getIndex();
		parentNode.delete(parentIndex);
	}

	/**
       Redistributes keys and pointers in this node and its
       next sibling so that they have the same number of keys
       and pointers, or so that this node has one more key and
       one more pointer,.
       @return int Returns key that must be inserted
       into parent node.
	 */
	public int redistribute() {
		int key = 0;
		///////////////////
		// ADD CODE HERE //
		///////////////////

		System.out.print("\nredistribute leaf:\t" + Arrays.toString(keys) + lastindex + "\t" + Arrays.toString(next.keys) + next.lastindex);

		// since it is a leaf node, only need to address keys, not ptrs
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 1; i <= lastindex; i++) {
			temp.add(keys[i]);
		}
		Node nextNode = next;
		for (int i = 1; i <= nextNode.lastindex; i++) {
			temp.add(nextNode.keys[i]);
		}

		this.cleanup();
		next.cleanup();

		// distribute data
		Collections.sort(temp);
		int mid = (int)Math.ceil(temp.size() / 2.0);
		lastindex = 0;
		for (int i = 0; i < mid; i++) {
			lastindex += 1;
			keys[lastindex] = temp.get(i);
		}
		nextNode.lastindex = 0;
		for (int i = mid ; i < temp.size(); i++) {
			nextNode.lastindex += 1;
			nextNode.keys[nextNode.lastindex] = temp.get(i);
		}
		System.out.println("\t" + Arrays.toString(this.keys) + lastindex);

		// return the first key of the new node
		key = nextNode.keys[1];
		return key;
	}

	public int redistribute_delete() {
		return this.redistribute();
	}

	/**
       Insert val into this node at keys [i].  (Ignores ptr) Called when this
       node is not full.
       @param val the value to insert to current node
       @param ptr not used now, use null when call this method
       @param i the index where this value should be
	 */
	public void insertSimple(int val, Node ptr, int i) {
		////////////////////
		// ADD CODE HERE  //
		////////////////////

		// shift to right by one position
		lastindex += 1;
		for (int j = lastindex; j >= i; j--) {
			keys[j] = keys[j - 1];
			ptrs[j] = ptrs[j - 1];
		}
		keys[i] = val;
		ptrs[i] = ptr;
	}


	/**
       Deletes keys[i] and ptrs[i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys from index i+1 on
       one position to the left.
	 */
	public void deleteSimple(int i) {
		///////////////////
		// ADD CODE HERE //
		///////////////////

		// shift to left by one position
		System.out.println("delete leaf:\t\t" + Arrays.toString(keys) + lastindex + "\tindex:" + i + "\tval:" + keys[i]);
		lastindex -= 1;
		for (int j = i; j <= lastindex; j++) {
			keys[j] = keys[j + 1];
			ptrs[j] = ptrs[j + 1];
		}
		keys[lastindex + 1] = 0;
	}

	public boolean underfull() {
		return lastindex < minkeys();
	}

	/**
       Uses findKeyIndex, and if val is found, returns the reference with match set to true, otherwise returns
       the reference with match set to false.
       @return a Reference object referring to this node.
	 */
	public Reference search(int val) {
		Reference ref = null;
		///////////////////
		// ADD CODE HERE //
		///////////////////
		int index = findKeyIndex(val);
		if (keys[index] == val) {
			ref = new Reference(this, index, true);
		} else {
			ref = new Reference(this, index, false);
		}
		return ref;
	}

	/**
       Insert val into this, creating split
       and recursive insert into parent if necessary
       Note that ptr is ignored.
       @param val the value to insert
       @param ptr (not used now, use null when calling this method)
	 */
	public void insert(int val, Node ptr) {
		///////////////////
		// ADD CODE HERE //
		///////////////////
		if (full()) {
			Node nextNode = this.getNext();
			LeafNode newLeaf = new LeafNode(degree, val, nextNode, this);
			this.next = newLeaf;
			if (nextNode != null) {
				nextNode.prev = newLeaf;
			}
			int newKey = redistribute();

			Reference theParent = this.getParent();
			if (theParent == null) {
				new InternalNode(degree, this, newKey, newLeaf, null, null);
			} else {
				Node parentNode = theParent.getNode();
				parentNode.insert(newKey, newLeaf);
			}
		} else {
			int index = findKeyIndex(val);
			if (val > keys[index]) {
				index = this.getLast() + 1;
			}
			this.insertSimple(val, null, index);
		}
	}

	/**
       Print to stdout the content of this node
	 */
	void printNode() {
		System.out.print ("[");
		for (int i = 1; i < lastindex; i++)
			System.out.print (keys[i]+" ");
		System.out.print (keys[lastindex] + "]");
	}
}
