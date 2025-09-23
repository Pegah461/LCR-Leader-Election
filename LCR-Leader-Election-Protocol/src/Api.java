import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Api extends Remote{
    void receiveELECTION(int uid) throws RemoteException;
    void receiveLEADER(int leaderID) throws RemoteException;
}