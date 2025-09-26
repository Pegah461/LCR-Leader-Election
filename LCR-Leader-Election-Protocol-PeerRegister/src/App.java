import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class App {

    private static void printDISP(int nodeID, int registryPort, String startAtStr) {
        String[] title = {
        " _   _    ___   ____   _____ ",
        "| \\ | |  /   \\ |  _ \\ | ____|",
        "|  \\| | | | | || | | ||  _|  ",
        "| |\\  | | |_| || |_| || |___ ",
        "|_| \\_|  \\___/ |____/ |_____|",
        "      P R O C E S S [" + nodeID + "]"
     };
        System.out.println("=============================================");
        for (String line : title) System.out.println(line);
        System.out.println("=============================================");
        System.out.printf("Registry : %d%n", registryPort);
        System.out.printf("Start At : %s%n", startAtStr);
        System.out.println("=============================================");
    }

    public static void main(String[] args) {
        // Usage: java App <nodeId> <startAt(HH:mm:ss)>
        if (args.length != 2) {
            System.out.println("Usage: java App <nodeId> <startAt(HH:mm:ss)>");
            return;
        }

        final int nodeId;
        String startAtStr = args[1];
        try {
            nodeId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid nodeId: " + args[0]);
            return;
        }

        try {
            // 1) Ensure local registry on default port (1099)
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
                registry.list(); // ping existing
            } catch (Exception notRunning) {
                registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            }

            // 2) Create and bind this node
            NodeImpl node = new NodeImpl(nodeId);
            String myBinding = "Node" + nodeId;
            try {
                registry.bind(myBinding, node);
            } catch (AlreadyBoundException e) {
                registry.rebind(myBinding, node);
            }
            System.out.println("Process[" + nodeId + "]: Bound as " + myBinding + " on Registry Port " + Registry.REGISTRY_PORT);

            // 3) Lookup Peer Register ("Node0") and register
            PeerRegister peerRegister = (PeerRegister) registry.lookup("Node0");
            boolean ok = peerRegister.register(nodeId);
            if (!ok) {
                System.err.println("Process[" + nodeId + "]: Registration denied — election in progress. Try again later.");
                System.exit(2);
            }
            System.out.println("Process[" + nodeId + "]: Registered with PeerRegister.");

            // Parse wall-clock start time and schedule a simultaneous start
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            ZoneId zone = ZoneId.systemDefault();
            LocalDate today = LocalDate.now(zone);
            ZonedDateTime startZdt = null;
            LocalTime t = null;
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            boolean validTime = false;
            while (!validTime) {
                try {
                    t = LocalTime.parse(startAtStr, fmt);
                    startZdt = ZonedDateTime.of(today, t, zone);
                    if (startZdt.isBefore(ZonedDateTime.now(zone))) {
                        // If time already passed today, ask user for a new time
                        System.out.println("The time you entered has already passed. Please enter a new time (HH:mm:ss): ");
                        startAtStr = scanner.nextLine();
                    } else {
                        validTime = true;
                    }
                } catch (Exception e) {
                    System.out.println("Invalid time format. Please enter time as HH:mm:ss: ");
                    startAtStr = scanner.nextLine();
                }
            }

            long millis = Duration.between(Instant.now(), startZdt.toInstant()).toMillis();

            try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor(); // Clear console

            printDISP(nodeId, Registry.REGISTRY_PORT, startAtStr);
            System.out.println("Process[" + nodeId + "]: Scheduled to start election at: " + startZdt);

            Thread starter = new Thread(() -> {
                try {
                    if (millis > 0) Thread.sleep(millis);
                    // Mark election start via register (blocks new registrations) then start
                    try {
                        if (!peerRegister.isElectionInProgress()) {
                            peerRegister.announceElectionStart();
                        }
                    } catch (Exception ignored) {}
                    node.initiateElection();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }, "StartElection-" + nodeId);
            starter.setDaemon(true);
            starter.start();

            // 6) Keep process alive
            while (true) {
                try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException ignored) {}
            }

        } catch (Exception e) {
            System.err.println("Fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}