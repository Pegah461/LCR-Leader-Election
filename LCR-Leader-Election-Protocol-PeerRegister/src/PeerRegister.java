import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerRegister extends Remote {
    /**
     * Register this node ID. Returns true if accepted; false if an election is in progress.
     * The register does NOT participate in the election.
     */
    boolean register(int id) throws RemoteException;

    /**
     * Ask the register to choose and return your successor Node stub (ring decided by the register).
     */
    Node getSuccessor(int id) throws RemoteException;

    /** Diagnostics and lifecycle controls for elections (invoked by nodes). */
    boolean isElectionInProgress() throws RemoteException;
    void announceElectionStart() throws RemoteException;
    void announceElectionEnd(int leaderId) throws RemoteException;
}