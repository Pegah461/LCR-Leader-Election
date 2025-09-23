import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class App {

    private static void printDISP(int nodeID, int registryPort, String nextNode, int nextPort) {
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
        System.out.println("---------------------------------------------");
        System.out.printf("PORT : %d%n", registryPort);
        System.out.println("CONNECTED TO: " + nextNode + " AT PORT: " + nextPort + "");
        System.out.println("=============================================");
    }

    public static void main(String[] args){
        if (args.length != 5) {
            System.out.println("Usage: java App <process_id> <nextProcess> <registryPort> <nextPort> <startAt(HH:mm:ss)>");
            return;
        }

        try{
            int nodeID = Integer.parseInt(args[0]);
            String nextNode = args[1];            
            int registryPort = Integer.parseInt(args[2]);
            int nextPort = Integer.parseInt(args[3]);
            String startAtStr = args[4];

            ApiImpl node = new ApiImpl();
            node.setNode(nodeID);

            Registry registry = LocateRegistry.createRegistry(registryPort);
            registry.bind("Process" + nodeID, node);
            System.out.println("Process[" + nodeID + "] Registered on Port " + registryPort);

            // Retry mechanism to connect to next process
            int retries = 10;
            int delay = 5000; // 5 seconds

            while (retries > 0) {
                try {
                    Registry nextRegistry = LocateRegistry.getRegistry("localhost", nextPort);
                    node.setNextNode((Api) nextRegistry.lookup(nextNode));
                    System.out.println("Process[" + nodeID + "]: Connected To: " + nextNode);

                    // Slow down before clearing screen for demonstration purposes
                    try{
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        throw new RuntimeException(e);
                    }
                    
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor(); // Clear console

                    break; // Exit loop if successful
                } catch (Exception e) {
                    retries--;
                    System.err.println("Process[" + nodeID + "]: Failed To Connect To " + nextNode + " On Port " + nextPort + "");
                    if (retries > 0) {
                        System.out.println("Process[" + nodeID + "]: Retrying In " + (delay/1000) + "s...");
                        try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    } else {
                        System.err.println("Max Tries Reached. Exiting...");
                        System.exit(1);
                    }
                }
            }

            // Parse wall-clock start time and schedule a simultaneous start
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime t = LocalTime.parse(startAtStr, fmt);

            ZoneId zone = ZoneId.systemDefault();
            LocalDate today = LocalDate.now(zone);
            ZonedDateTime startZdt = ZonedDateTime.of(today, t, zone);
            if (startZdt.isBefore(ZonedDateTime.now(zone))) {
                // if time already passed today, schedule for tomorrow at same time
                startZdt = startZdt.plusDays(1);
            }

            long millis = Duration.between(Instant.now(), startZdt.toInstant()).toMillis();

            printDISP(nodeID, registryPort, nextNode, nextPort);
            //System.out.println("Process[" + nodeID + "]: Connected To: " + nextNode);
            System.out.println("Process[" + nodeID + "] Scheduled to Start Election At: " + startZdt);

            Thread starter = new Thread(() -> {
                try {
                    if (millis > 0) Thread.sleep(millis);
                    node.initiateElectionOnce();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "StartElection-" + nodeID);
            starter.setDaemon(true);
            starter.start();

            // Keep process alive (no console input anymore)
            while (true) {
                try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException ignored) {}
            }

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}