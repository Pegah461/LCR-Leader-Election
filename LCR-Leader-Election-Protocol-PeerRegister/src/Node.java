import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Node interface for the LCR Leader Election protocol using RMI.
 * Defines remote methods for election and leader notification, node linking, and UID retrieval.
 */
public interface Node extends Remote {

    /**
     * Receives an ELECTION message with a UID from another node.
     */
    void recieveELECTION(int uid) throws RemoteException;

    /**
     * Receives a LEADER message with a UID indicating the elected leader.
     */
    void recieveLEADER(int uid) throws RemoteException;

    /**
     * Sets the next node in the ring topology.
     * Peer Register will call this method to link nodes in join order.
     * @param nextNode Reference to the next Node in the ring.
     */
    void setNextNode(Node nextNode) throws RemoteException;

}