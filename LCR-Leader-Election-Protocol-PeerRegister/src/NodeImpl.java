import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeImpl extends UnicastRemoteObject implements Node {
    private final int id;
    private volatile int leaderId;
    private volatile Node nextNode;
    private volatile boolean isLeader;
    private volatile boolean hasVoted;
    private final AtomicBoolean uiStarted = new AtomicBoolean(false);

    public NodeImpl(int nodeId) throws RemoteException {
        super();
        this.id = nodeId;
        this.isLeader = false;
        this.hasVoted = false;
    }

    @Override
    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }   

    @Override
    public void recieveELECTION(int UID) throws RemoteException {
        // Simulate network delay for visibility
        try { 
            Thread.sleep(2000); 
        } catch (InterruptedException e){ 
            Thread.currentThread().interrupt(); 
        }

        if (this.id == UID) {
            // Node's UID go full circle — Node is the leader
            this.leaderId = this.id;
            this.isLeader = true;
            this.hasVoted = true;
            System.out.println("Process[" + this.id + "]: Received ELECTION(" + UID + ") message. Declaring Victory...");
            System.out.println("Process[" + this.id + "]: Sending LEADER(" + this.id + ") Announcement..");
            nextNode.recieveLEADER(this.id);
            return;
        }

        if (this.id > UID) {
            System.out.println("Process[" + this.id + "]: Received ELECTION(" + UID + ") message. Dropping message.");
            this.hasVoted = true;
        } else if (this.id < UID) {
            System.out.println("Process[" + this.id + "]: Receiving ELECTION(" + UID + ") message. Forwarding message.");
            this.hasVoted = true;
            nextNode.recieveELECTION(UID);
        }
    }

    @Override
    public void recieveLEADER(int UID) throws RemoteException {
        // Simulate network delay for visibility
        try { 
            Thread.sleep(2000); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        }

        this.leaderId = UID;

        if (this.id == UID) {
            // The announce loop completed
            this.hasVoted = false;
            System.out.println("Process[" + this.id + "]: Received LEADER(" + UID + ")");
            System.out.println("Process[" + this.id + "]: Election is complete..");

            // Reopen registrations via register
            PeerRegister pr = PeerRegisterLookup.tryLookup();
            if (pr != null) {
                try { pr.announceElectionEnd(UID); } catch (Exception ignored) {}
            }

            startUiOnce(); // allow user to exit or start new election
            return;
        }else{
            this.hasVoted = false;
            this.isLeader = false;
            System.out.println("Process[" + id + "]: Received LEADER(" + UID + ") message");
            System.out.println("Process[" + id + "]: New Leader Is -> Process[" + UID + "]");
            System.out.println("Process[" + id + "]: Forwarding LEADER(" + UID + ") Announcement..");

            nextNode.recieveLEADER(UID);
        }
        startUiOnce(); // allow user to exit or start new election
        return;
    }

    @Override
    public int getId() { return this.id; }


    public void initiateElection() {

        if (nextNode == null) {
            System.out.println("Process[" + id + "]: Cannot start election — nextNode is null.");
            return;
        }
        // crude self-loop guard if more than one peer expected
        if (nextNode == this && PeerRegisterLookup.tryLookup() != null) {
            System.out.println("Process[" + id + "]: Next points to self — ring not wired yet. Aborting start.");
            return;
        }

        System.out.println("Process[" + id + "]: Detected Leader failure. Initiating election . . .");
        PeerRegister pr = PeerRegisterLookup.tryLookup();
        if (pr != null) {
            try {
                if (!pr.isElectionInProgress()) pr.announceElectionStart();
                else System.out.println("Process[" + id + "]: PeerRegister is Aware that Election is in Progress..");
            } catch (Exception e) {
                System.out.println("Process[" + id + "]: Unable to notify PeerRegister of election start: " + e.getMessage());
            }
        }

        try {
            System.out.println("Process[" + id + "]: Sending ELECTION(" + id + ") message..");
            nextNode.recieveELECTION(id);
        } catch (RemoteException e) {
            System.out.println("Process[" + id + "]: Failed to initiate election");
        }
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
    private void startUiOnce() {
        if (uiStarted.compareAndSet(false, true)) {
            new Thread(this::handleUserInput, "UI-" + id).start();
        }
    }

    /** Helper: lightweight locator for the register (Node0 @ default port). */
    static class PeerRegisterLookup {
        static PeerRegister tryLookup() {
            try {
                java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry.getRegistry(java.rmi.registry.Registry.REGISTRY_PORT);
                return (PeerRegister) r.lookup("Node0");
            } catch (Exception e) {
                return null;
            }
        }
    }
}