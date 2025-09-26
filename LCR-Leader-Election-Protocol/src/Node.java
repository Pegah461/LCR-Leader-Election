
/**
 * Node represents a participant in the LCR Leader Election protocol.
 * Each node has a unique identifier (uid).
 */
public class Node {
    private int uid; // Unique identifier for the node


    /**
     * Constructs a Node with the given unique identifier.
     */
    public Node(int uid) {
        this.uid = uid;
    }

    public int getID() { // Returns the unique ID of this node.
        return uid;
    }

    public void setID(int uid) { // Sets the unique ID of this node.
        this.uid = uid;
    }
}