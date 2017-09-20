public class Reference{

	private Node node;                // What node is the referencing node?
	private int index;
	// What is the pointer or key index within the
	//  referencing node?
	private boolean match;
	// If this is returned as a result of a
	//  search, was there a successful match?

	/**
       Constructs a Reference object and initializes the fields with the parameters.
       @param p the node it refers
       @param i the index of the key (pointer) it refers to
       @param m for the result of search, true if the key matches the search value
	 */
	public Reference (Node p, int i, boolean m){
		node = p;
		index = i;
		match = m;
	}

	/**
       Returns the Node which this reference refers to.
       @return A node object this reference refers to.
	 */
	public Node getNode () {
		return node;
	}

	/**
	Get the index
	@return the index
	 */
	public int getIndex () {
		return index;
	}

	/**
       Returns the match value of this reference. The match field is used only for returning a search result
       If the key value this reference refers to matches the search
       value, match is set to true, otherwise it is set to false.
       @return the match value
	 */
	public boolean getMatch () {
		return match;
	}

	/**
       Set the index value. Used when a search key moves within a node.
       @param i the new index value
	 */
	void setIndex (int i) {
		index = i;
	}

	/**
       Increase the index value. Used when a search key moves within a node.
	 */
	public void increaseIndex () {
		index++;
	}

	/**
       Decrease the index value. Used when a search key moves within a node.
	 */
	public void decreaseIndex () {
		index--;
	}
}
