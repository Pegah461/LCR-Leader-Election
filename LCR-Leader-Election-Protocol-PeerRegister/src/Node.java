import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    void recieveELECTION(int uid) throws RemoteException;
    void recieveLEADER(int uid) throws RemoteException;
    void setNextNode(Node nextNode) throws RemoteException;
    int getId() throws RemoteException;
}