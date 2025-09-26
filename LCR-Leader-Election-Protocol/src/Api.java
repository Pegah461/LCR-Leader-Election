
// Remote interface for LCR Leader Election protocol
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Api defines the remote methods for the LCR Leader Election protocol.
 * Nodes use these methods to communicate election and leader messages.
 */
public interface Api extends Remote {

    /** receiveELECTION()
     * Called when a node forwards an ELECTION message to its next-node.
     */
    void receiveELECTION(int uid) throws RemoteException;

    /**
     * Called when a node forwards a LEADER announcement to its next-node.
     */
    void receiveLEADER(int leaderID) throws RemoteException;
}