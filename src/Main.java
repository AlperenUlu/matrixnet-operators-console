import java.io.*;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            MatrixManager matrixManager = new MatrixManager();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                processCommand(line, writer, matrixManager);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, BufferedWriter writer, MatrixManager matrixManager)
            throws IOException {

        String[] parts = command.split("\\s+");
        String operation = parts[0];

        try {
            String result = "";

            switch (operation) {
                case "spawn_host":
                    String hostID = parts[1];
                    int clearanceLevel = Integer.parseInt(parts[2]);
                    result = matrixManager.spawnHost(hostID, clearanceLevel);
                    break;
                case "link_backdoor":
                    String firstHostID = parts[1];
                    String secondHostID = parts[2];
                    int latency = Integer.parseInt(parts[3]);
                    int bandwidth = Integer.parseInt(parts[4]);
                    int firewallLevel = Integer.parseInt(parts[5]);
                    result = matrixManager.linkBackdoor(firstHostID,secondHostID,latency
                    ,bandwidth,firewallLevel);
                    break;
                case "seal_backdoor":
                    String hostID1 = parts[1];
                    String hostID2 = parts[2];
                    result = matrixManager.sealBackdoor(hostID1, hostID2);
                    break;
                case "trace_route":
                    String sourceID = parts[1];
                    String destinationID = parts[2];
                    int minBandwidth = Integer.parseInt(parts[3]);
                    int lambda = Integer.parseInt(parts[4]);
                    result = matrixManager.traceRoute(sourceID,destinationID,minBandwidth,lambda);
                    break;
                case "scan_connectivity":
                    result = matrixManager.scanConnectivity();
                    break;
                case "simulate_breach":
                    if(parts.length == 2){
                        String firstID = parts[1];
                        result = matrixManager.simulateBreach(firstID);
                    }
                    else {
                        String firstID = parts[1];
                        String secondID = parts[2];
                        result = matrixManager.simulateBreach(firstID,secondID);
                    }
                    break;
                case "oracle_report":
                    result = matrixManager.oracleReport();
                    break;
                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();


        } catch (Exception e) {
            writer.write("Error processing command: " + command);
            writer.newLine();
        }

    }
}