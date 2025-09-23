import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerRegisterImpl extends UnicastRemoteObject implements PeerRegister {

    private static class Peer {
        final int id;
        final String binding;  // "Node<id>"
        Peer(int id) { this.id = id; this.binding = "Node" + id; }
    }

    // Preserve **join order** with LinkedHashMap (insertion order)
    private final LinkedHashMap<Integer, Peer> peers = new LinkedHashMap<>();
    private final AtomicBoolean electionInProgress = new AtomicBoolean(false);

    public PeerRegisterImpl() throws RemoteException { super(); }

    @Override
    public synchronized boolean register(int id) throws RemoteException {
        if (electionInProgress.get()) {
            System.out.println("[PR] Reject Process[" + id + "]: Election in Progress..");
            return false;
        }
        if (peers.containsKey(id)) {
            System.out.println("[PR] Process[" + id + "] Re-Registered.");
            return true;
        }

        // Add new peer in join order (LinkedHashMap preserves insertion order)
        peers.put(id, new Peer(id));
        System.out.println("[PR] Registered Process[" + id + "]");
        logRing();

        try {
            Registry r = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);

            // Build current join order
            java.util.List<Integer> order = new java.util.ArrayList<>(peers.keySet());
            int size = order.size();

            if (size == 1) {
                // Only one node so far — no wiring yet
                return true;
            }

            // Place new node between its predecessor and successor in join order
            int idx = order.indexOf(id);
            int predIdx = (idx - 1 + size) % size;
            int succIdx = (idx + 1) % size;

            int predId = order.get(predIdx);
            int succId = order.get(succIdx);

            Node newNode     = (Node) r.lookup("Node" + id);
            Node predecessor = (Node) r.lookup("Node" + predId);
            Node successor   = (Node) r.lookup("Node" + succId);

            // Rewire the ring: predecessor -> newNode -> successor
            predecessor.setNextNode(newNode);
            newNode.setNextNode(successor);

            System.out.println("[PR] Rewired: pred(" + predId + ") -> " + id + " -> succ(" + succId + ")");

        } catch (Exception e) {
            throw new RemoteException("PeerRegister: wiring failed for Process[" + id + "]: " + e, e);
        }

        return true;
    }


    @Override
    public synchronized Node getSuccessor(int id) throws RemoteException {
        if (!peers.containsKey(id)) throw new RemoteException("Process[" + id + "] not registered with PeerRegister.");

        // Build the ring in **join order** (no sorting)
        List<Integer> order = new ArrayList<>(peers.keySet());
        int idx = order.indexOf(id);
        int succId = order.get((idx + 1) % order.size());
        String succBinding = peers.get(succId).binding;

        try {
            Registry r = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            return (Node) r.lookup(succBinding);
        } catch (Exception e) {
            throw new RemoteException("PeerRegister: cannot lookup Next-Node(" + succId + ") / " + succBinding, e);
        }
    }

    @Override
    public boolean isElectionInProgress() { return electionInProgress.get(); }

    @Override
    public void announceElectionStart() {
        if (electionInProgress.compareAndSet(false, true)) {
            System.out.println("[PR] Election STARTED — new registrations are blocked.");
        }
    }

    @Override
    public void announceElectionEnd(int leaderId) {
        electionInProgress.set(false);
        System.out.println("[PR] Election ENDED — Leader: Node(" + leaderId + "). Registrations reopened.");
    }

    private void logRing() {
        System.out.println("[PR] Current ring (join order): " + peers.keySet());
    }

    /** Optional standalone launcher (binds as "Node0" by default). */

    private static void printDISP(int registryPort) {
        String[] title = {
        " _   _    ___   ____   _____ ",
        "| \\ | |  /   \\ |  _ \\ | ____|",
        "|  \\| | | | | || | | ||  _|  ",
        "| |\\  | | |_| || |_| || |___ ",
        "|_| \\_|  \\___/ |____/ |_____|",
        "   P E E R  R E G I S T E R"
     };
        System.out.println("---------------------------------------------");
        for (String line : title) System.out.println(line);
        System.out.println("---------------------------------------------");
        System.out.printf("Registry Port: %d%n", registryPort);
        System.out.println("---------------------------------------------");
    }

    public static void main(String[] args) throws Exception {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : Registry.REGISTRY_PORT;
        String binding = (args.length > 1) ? args[1] : "Node0";

        Registry r;
        try { r = LocateRegistry.getRegistry(port); r.list(); }
        catch (Exception notRunning) { r = LocateRegistry.createRegistry(port); }

        r.rebind(binding, new PeerRegisterImpl());
        //System.out.println("[PR] Running on port " + port + " as " + binding + "");

        System.out.println("Booting up PeerRegister...");
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor(); // Clear console
        printDISP(port);
    }
}