import java.util.*;


/**
    Internal Nodes of B+-Trees.
    @author cs127b
 */
public class InternalNode extends Node{

	/**
       Construct an InternalNode object and initialize it with the parameters.
       @param d degree
       @param p0 the pointer at the left of the key
       @param k1 the key value
       @param p1 the pointer at the right of the key
       @param n the next node
       @param p the previous node
	 */
	public InternalNode(int d, Node p0, int k1, Node p1, Node n, Node p) {
		super (d, n, p);
		ptrs[0] = p0;
		keys[1] = k1;
		ptrs[1] = p1;
		lastindex = 1;
		if (p0 != null) {
			p0.setParent(new Reference (this, 0, false));
		}
		if (p1 != null) {
			p1.setParent(new Reference (this, 1, false));
		}
	}

	/**
       The minimal number of keys this node should have.
       @return the minimal number of keys a leaf node should have.
	 */
	public int minkeys() {
		int min = 0;
		///////////////////
		// ADD CODE HERE //
		///////////////////
		if (parentref == null) {
			return 1;
		}
		min = (int)Math.ceil((degree) / 2.0) - 1;
		//min = (int)Math.ceil(degree / 2.0);
		return Math.max(min, 0);
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined.
	 */
	public boolean combinable(Node other) {
		return lastindex + other.lastindex <= maxkeys() - 1;
	}

	/**
       Combines contents of this node and its next sibling (next)
       into a single node,
	 */
	public void combine() {
		// differences with leafNode: need to address ptrs, and need to demote parent
		if (next == null) {
			return;
		}
		System.out.println("combine internal:\t" + Arrays.toString(keys) + lastindex + "\t" + Arrays.toString(next.keys) + next.lastindex);
		Node nextNode = next;

		// demote parent
		Node parentNode = nextNode.parentref.getNode();
		int parentIndex = nextNode.parentref.getIndex();
		lastindex += 1;
		keys[lastindex] = parentNode.keys[parentIndex];
		ptrs[lastindex] = next.ptrs[0];
		ptrs[lastindex].parentref = new Reference(this, lastindex, false);

		parentNode.delete(parentIndex);  // delete parent

		// transfer next sibling node's date into current node
		for (int i = 1; i <= next.lastindex; i++) {  // start from 1
			lastindex += 1;
			keys[lastindex] = next.keys[i];
			ptrs[lastindex] = next.ptrs[i];
			ptrs[lastindex].parentref = new Reference(this, lastindex, false);  // set parent
		}
		// connect node, delete nextNode
		Node nextNextNode = nextNode.next;
		this.next = nextNextNode;
		if (nextNextNode != null) {
			nextNextNode.prev = this;
		}
	}

	/**
       Redistributes keys and pointers in this node and its
       next sibling so that they have the same number of keys
       and pointers, or so that this node has one more key and
       one more pointer.  Returns the key that must be inserted
       into parent node.
       @return the value to be inserted to the parent node
	 */
	public int redistribute() {
		int key = 0;
		System.out.println("redistribute internal:\t" + Arrays.toString(keys) + lastindex + "\t" + Arrays.toString(next.keys) + next.lastindex);

		// when insert, does not need to demote since right sibling's parent is empty
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Node> tempPtrs = new ArrayList<Node>();
		Node nextNode = next;
		int insertIndex = findKeyIndex(nextNode.keys[1]);

		if (nextNode.keys[1] > keys[lastindex]) {
			insertIndex += 1;
		}

		int i = 0;
		while (i < insertIndex) {
			temp.add(keys[i]);
			tempPtrs.add(ptrs[i]);
			i += 1;
		}
		temp.add(nextNode.keys[1]);
		tempPtrs.add(nextNode.ptrs[1]);
		while (i <= lastindex) {
			temp.add(keys[i]);
			tempPtrs.add(ptrs[i]);
			i += 1;
		}

		// exclude the middle
		int mid = (int)Math.ceil(temp.size() / 2.0);
		key = temp.get(mid);

		this.cleanup();
		next.cleanup();

		// distribute data
		lastindex = 0;
		ptrs[lastindex] = tempPtrs.get(0);
		getPtr(0).parentref = new Reference(this, 0, false);
		for (i = 1; i < mid; i++) {
			lastindex += 1;
			keys[lastindex] = temp.get(i);
			ptrs[lastindex] = tempPtrs.get(i);
			getPtr(lastindex).parentref = new Reference(this, lastindex, false);
		}
		nextNode.lastindex = 0;
		nextNode.ptrs[0] = tempPtrs.get(mid);
		nextNode.getPtr(0).parentref = new Reference(nextNode, 0, false);
		for (i = mid + 1; i < temp.size(); i++) {
			nextNode.lastindex += 1;
			nextNode.keys[nextNode.lastindex] = temp.get(i);
			nextNode.ptrs[nextNode.lastindex] = tempPtrs.get(i);
			nextNode.getPtr(nextNode.lastindex).parentref = new Reference(nextNode, nextNode.lastindex, false);
		}

		// return the excluded key
		return key;
	}

	public int redistribute_delete() {
		int key = 0;

		System.out.println("redistribute internal:\t" + Arrays.toString(keys) + lastindex + "\t" + Arrays.toString(next.keys) + next.lastindex);

		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Node> tempPtrs = new ArrayList<Node>();
		Node nextNode = next;

		// combine data: this
		for (int i = 0; i <= lastindex; i++) {
			temp.add(keys[i]);
			tempPtrs.add(ptrs[i]);
		}
		// combine data: demote parent
		Node parentNode = next.parentref.getNode();
		int parentIndex = next.parentref.getIndex();
		int parentKey = parentNode.keys[parentIndex];
		temp.add(parentKey);
		tempPtrs.add(next.ptrs[0]);  // demoted parent with next [0] ptr
		// combine data: next
		for (int i = 1; i <= nextNode.lastindex; i++) {  // start from 1
			temp.add(next.keys[i]);
			tempPtrs.add(next.ptrs[i]);
		}

		// exclude the middle
		int mid = (int)Math.ceil(temp.size() / 2.0);
		key = temp.get(mid);

		this.cleanup();
		next.cleanup();

		// distribute data
		lastindex = 0;
		ptrs[lastindex] = tempPtrs.get(0);
		getPtr(0).parentref = new Reference(this, 0, false);
		for (int i = 1; i < mid; i++) {
			lastindex += 1;
			keys[lastindex] = temp.get(i);
			ptrs[lastindex] = tempPtrs.get(i);
			getPtr(lastindex).parentref = new Reference(this, lastindex, false);
		}
		nextNode.lastindex = 0;
		nextNode.ptrs[0] = tempPtrs.get(mid);
		nextNode.getPtr(0).parentref = new Reference(nextNode, 0, false);
		for (int i = mid + 1; i < temp.size(); i++) {
			nextNode.lastindex += 1;
			nextNode.keys[nextNode.lastindex] = temp.get(i);
			nextNode.ptrs[nextNode.lastindex] = tempPtrs.get(i);
			nextNode.getPtr(nextNode.lastindex).parentref = new Reference(nextNode, nextNode.lastindex, false);
		}

		// return the excluded key
		return key;
	}

	/**
       Inserts (val, ptr) pair into this node
       at keys [i] and ptrs [i].  Called when this
       node is not full.  Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param val the value to insert
       @param ptr the pointer to insert
       @param i the position to insert the value and pointer
	 */
	public void insertSimple(int val, Node ptr, int i) {
		// shift to right by one position
		lastindex += 1;
		for (int j = lastindex; j >= i; j--) {
			keys[j] = keys[j - 1];
			ptrs[j] = ptrs[j - 1];
		}
		for (int j = i + 1; j <= lastindex; j++) {
			getPtr(j).getParent().increaseIndex();
		}
		keys[i] = val;
		ptrs[i] = ptr;
		ptr.setParent(new Reference(this, i , false));
	}

	/**
       Deletes keys[i] and ptrs[i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys and pointers from index i+1 on
       one position to the left. Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param i the index of the key to delete
	 */
	public void deleteSimple(int i) {
		// shift to left by one position
		System.out.println("delete internal:\t" + Arrays.toString(keys) + lastindex + "\tindex:" + i + "\tval:" + keys[i]);
		lastindex -= 1;
		for (int j = i; j <= lastindex; j++) {
			keys[j] = keys[j + 1];
			ptrs[j] = ptrs[j + 1];

		}
		for (int j = i; j <= lastindex; j++) {
			getPtr(j).getParent().decreaseIndex();
		}
		keys[lastindex + 1] = 0;
		ptrs[lastindex + 1] = null;
	}

	public boolean underfull() {  // self added, maybe should use minkeys
		return lastindex < minkeys();  // ptrs number: Math.ceil(degree / 2.0) ~ n
	}


	/**
       Uses findPtrInex and calles itself recursively until find the value or find the position
       where the value should be.
       @return the reference pointing to a leaf node.
	 */
	public Reference search(int val) {  // completely not sure
		Reference ref = null;
		// This method will call findPtrIndex(val)
		// which returns the array index i, such that the ith pointer in the node points to the subtree containing val.
		// It will then call search() recursively on the node returned in the previous step.
		int ptrIndex = findPtrIndex(val);
		Node subNode = ptrs[ptrIndex];
		ref = subNode.search(val); // return the reference pointing to a leaf node.
		return ref;
	}

	/**
       Insert (val, ptr) into this node. Uses insertSimple, redistribute etc.
       Insert into parent recursively if necessary
       @param val the value to insert
       @param ptr the pointer to insert
	 */
	public void insert(int val, Node ptr) {  // not sure
		if (full()) {
			Node nextNode = this.getNext();
			InternalNode newNode = new InternalNode(degree, null, val, ptr, nextNode, this);
			this.next = newNode;
			if (nextNode != null) {
				nextNode.prev = newNode;
			}
			int newKey = this.redistribute();

			Reference theParent = this.getParent();
			if (theParent == null) {
				new InternalNode(degree, this, newKey, newNode, null, null);
			} else {
				Node parentNode = theParent.getNode();
				parentNode.insert(newKey, newNode);
			}
		} else {
			int index = findKeyIndex(val);
			if (val > keys[lastindex]) {
				index = this.getLast() + 1;
			}
			this.insertSimple(val, ptr, index);
		}
	}

	public void outputForGraphviz() {
		// The name of a node will be its first key value
		// String name = "I" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();
		// Now, prepare the label string
		String label = "";
		for (int j = 0; j <= lastindex; j++) {
			if (j > 0) label += "|";
			label += "<p" + ptrs[j].myname + ">";
			if (j != lastindex) label += "|" + String.valueOf(keys[j+1]);
			// Write out any link now
			BTree.writeOut(myname + ":p" + ptrs[j].myname + " -> " + ptrs[j].myname + "\n");
			// Tell your child to output itself
			ptrs[j].outputForGraphviz();
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/**
       Print out the content of this node
	 */
	void printNode() {
		int j;
		System.out.print("[");
		for (j = 0; j <= lastindex; j++) {

			if (j == 0)
				System.out.print (" * ");
			else
				System.out.print(keys[j] + " * ");

			if (j == lastindex)
				System.out.print ("]");
		}
	}
}
