// Implementation of the Api interface for LCR Leader Election protocol
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ApiImpl provides the implementation of the LCR Leader Election protocol.
 * It manages election logic, and message passing between nodes.
 */
public class ApiImpl extends UnicastRemoteObject implements Api {

    private Node Node = new Node(0); // The local node instance
    private Api nextNode; // Reference to the next node in the ring
    private int leaderID; // The elected leader's ID
    private boolean isLeader; // True if this node is the leader
    private final AtomicBoolean electionStarted = new AtomicBoolean(false); // Ensures election starts only once
    private volatile boolean leaderKnown = false; // Tracks if leader is known
    private final AtomicBoolean uiStarted = new AtomicBoolean(false);

    // Constructor
    protected ApiImpl() throws RemoteException {
        super();
    }

    public void setNode(int uid) { // Sets the unique ID for this node.
        Node.setID(uid);
    }

    public void setNextNode(Api nextNode) { // Sets the reference to the next node in the ring.
        this.nextNode = nextNode;
    }


    /**
     * Marks this node as the leader.
     */
    public void setAsLeader() {
        this.isLeader = true;
    }


    /**
     * Safely triggers the election only once per node.
     */
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


    /**
     * Initiates the election by sending an ELECTION message to the next node.
     */
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

    /**
     * Handles incoming ELECTION messages according to LCR protocol.
     * @param uid Unique identifier of the sender node
     */
    @Override
    public void receiveELECTION(int uid) throws RemoteException {
        // Slow down for demonstration purposes
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (uid == this.Node.getID()) {
            // If UID of the incoming election message matches this node's ID,
            // this node is the leader
            leaderID = this.Node.getID();
            this.setAsLeader();

            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Declaring Victory...");
            System.out.println("Process[" + this.Node.getID() + "] Sending LEADER(" + leaderID + ") Announcement...");
            this.nextNode.receiveLEADER(this.Node.getID()); // Announce self as leader
        }

        if (uid > this.Node.getID()) {
            // Forward the election message if UID is higher than this node's ID
            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Forwarding message");
            this.nextNode.receiveELECTION(uid);
        } else if (uid < this.Node.getID()) {
            // Drop the election message if UID is lower than this node's ID
            System.out.println("Process[" + this.Node.getID() + "] Received ELECTION(" + uid + ") Message. Dropping message");
        }
    }

    /**
     * Handles incoming LEADER messages and propagates leader announcement.
     * @param leaderID Unique identifier of the elected leader
     */
    @Override
    public void receiveLEADER(int leaderID) throws RemoteException {
        // Slow down for demonstration purposes
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (leaderID != this.Node.getID()) {
            // Announce new leader to this node and forward
            System.out.println("Process[" + this.Node.getID() + "] Received LEADER(" + leaderID + ") Announcement");
            System.out.println("Process[" + this.Node.getID() + "]: New Leader Is Process[" + leaderID + "]");
            this.nextNode.receiveLEADER(leaderID);
        } else if (leaderID == this.Node.getID()) {
            // All nodes have received the leader announcement
            System.out.println("All Processes Have Received LEADER(" + leaderID + ") Announcement.");
            System.out.println("Process[" + this.Node.getID() + "] Election Complete");
        }
        startUiOnce();
    }

    /** Public so App can call it. */
    public void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Type 'exit' to quit:");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                System.exit(0);
            } else {
                System.out.println("Invalid command.");
            }
        }
    }
    private void startUiOnce() { // This methods will be called after election ends. It askes user to exit..
        if (uiStarted.compareAndSet(false, true)) {
            new Thread(this::handleUserInput, "UI-" + this.Node.getID()).start();
        }
    }
}
