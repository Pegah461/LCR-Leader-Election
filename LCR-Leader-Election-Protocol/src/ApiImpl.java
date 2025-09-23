import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApiImpl extends UnicastRemoteObject implements Api{
    private Node Node = new Node(0);
    private Api nextNode;
    private int leaderID;
    private boolean isLeader;
    private final AtomicBoolean electionStarted = new AtomicBoolean(false);
    private volatile boolean leaderKnown = false;

    protected ApiImpl() throws RemoteException {
        super();
    }

    public void setNode(int uid){
        Node.setID(uid);
    }

    public void setNextNode(Api nextNode){
        this.nextNode = nextNode;
    }

    public void setAsLeader(){
        this.isLeader = true;
    }

    /** Safe one-time election trigger */
    public void initiateElectionOnce() {
        if (leaderKnown) {
            System.out.println("Process[" + Node.getID() + "] Leader already known. Ignoring start.");
            return;
        }
        if (!electionStarted.compareAndSet(false, true)) {
            System.out.println("Process[" + Node.getID() + "] Election already initiated here.");
            return;
        }
        initiateElection();
    }

    private void initiateElection() {
        System.out.println("Process[" + Node.getID() + "] Initiating Election...");
        try {
            if (this.nextNode == null) {
                System.out.println("Process[" + Node.getID() + "] NextNode Not Connected Yet. Cannot Start.");
                return;
            }
            System.out.println("Process[" + Node.getID() + "] Sending ELECTION(" + this.Node.getID() + ")");
            this.nextNode.receiveELECTION(this.Node.getID());
        } catch (RemoteException e) {
            System.out.println("Process[" + Node.getID() + "] Failed To Initiate Election: " + e.getMessage());
        }
    }

    @Override
    public void receiveELECTION(int uid) throws RemoteException {

        // Slow down for demonstration purposes
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }

        if(uid == this.Node.getID()){
            leaderID = this.Node.getID();
            this.setAsLeader();

            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Declaring Victory...");
            System.out.println("Process[" + this.Node.getID() + "] Sending LEADER(" + leaderID + ") Announcement...");
            this.nextNode.receiveLEADER(this.Node.getID());
        }

        if(uid > this.Node.getID()){
            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Forwarding message");
            this.nextNode.receiveELECTION(uid);
        }else if(uid < this.Node.getID()){
            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Dropping message");
        }
    }

    @Override
    public void receiveLEADER(int leaderID) throws RemoteException {
        // Slow down for demonstration purposes
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }

        if(leaderID != this.Node.getID()){
           System.out.println("Process[" + this.Node.getID() + "] Received LEADER(" + leaderID + ") Announcement");
           System.out.println("Process[" + this.Node.getID() + "]: New Leader Is Process[" + leaderID + "]");
           this.nextNode.receiveLEADER(leaderID);
        }else if(leaderID == this.Node.getID()){
            System.out.println("All Processes Have Received LEADER(" + leaderID + ") Announcement.");
            System.out.println("Process[" + this.Node.getID() + "] Election Complete");
        }
    
    }
}