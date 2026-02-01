import java.util.LinkedList;

/**
 * Manages the graph of Hosts and Backdoors
 * This class handles the creation of the graph structure, pathfinding, connectivity analysis, and graph reporting.
 */
public class MatrixManager {
    private HostTable<Host> hostTable;
    private int totalClearance = 0;
    private int totalBandwidth = 0;
    private int totalUnsealedBackdoors = 0;

    /**
     * Constructs a new MatrixManager with an empty host table.
     */
    MatrixManager() {
        this.hostTable = new HostTable<>();
    }

    /**
     * Creates and adds a new Host to the graph.
     * @param hostID         The unique identifier for the host.
     * @param clearanceLevel The security clearance level of the host.
     * @return A status message indicating success or error.
     */
    public String spawnHost(String hostID, int clearanceLevel) {
        // Naming convention: Only uppercase A-Z, 0-9, or underscores are allowed
        for (int i = 0; i < hostID.length(); i++) {
            char letter = hostID.charAt(i);
            if (!((letter >= 'A' && letter <= 'Z') || (letter >= '0' && letter <= '9') || (letter == '_'))) {
                return "Some error occurred in spawn_host.";
            }
        }

        // Prevent duplicate hosts
        if(hostTable.containsID(hostID)) {
            return "Some error occurred in spawn_host.";
        }
        else {
            Host newHost = new Host(hostID, clearanceLevel);
            hostTable.put(newHost);

            // Update global stats
            totalClearance += clearanceLevel;

            return "Spawned host " + hostID + " with clearance level " + clearanceLevel + ".";
        }
    }

    /**
     * Establishes a connection between two existing hosts.
     * @param firstHostID   ID of the first host.
     * @param secondHostID  ID of the second host.
     * @param latency       The base latency of the connection.
     * @param bandwidth     The bandwidth capacity of the connection.
     * @param firewallLevel The firewall difficulty level.
     * @return A status message indicating the link was created or an error occurred.
     */
    public String linkBackdoor(String firstHostID, String secondHostID,
                               int latency, int bandwidth, int firewallLevel) {
        // Hosts must exist and cannot link to themselves
        if(!hostTable.containsID(firstHostID) || !hostTable.containsID(secondHostID) ||
                firstHostID.equals(secondHostID)) {
            return "Some error occurred in link_backdoor.";
        }
        Host firstHost = hostTable.get(firstHostID);
        Host secondHost = hostTable.get(secondHostID);

        // Check adjacency list to prevent duplicate edges
        LinkedList<Backdoor> backdoorLinkedList = firstHost.getAdjacencyList();
        for (Backdoor link : backdoorLinkedList) {
            Host neighborVertex = link.getBackdoorEnd(firstHost);
            String neighborID = neighborVertex.getHostID();
            if (neighborID.equals(secondHost.getHostID())) {
                return "Some error occurred in link_backdoor.";
            }
        }

        // Create the edge object
        Backdoor backdoor = new Backdoor(firstHost, secondHost, latency, bandwidth, firewallLevel);

        LinkedList<Backdoor> firstAdjacencyList = firstHost.getAdjacencyList();
        LinkedList<Backdoor> secondAdjacencyList = secondHost.getAdjacencyList();

        // Add edge to both nodes since the graph is undirected
        firstAdjacencyList.add(backdoor);
        secondAdjacencyList.add(backdoor);

        // Update global graph stats
        totalBandwidth += bandwidth;
        totalUnsealedBackdoors++;

        return "Linked " + firstHostID + " <-> " + secondHostID + " with latency " + latency + "ms, bandwidth "
                + bandwidth + "Mbps, firewall " + firewallLevel + ".";
    }

    /**
     * Switches the state of a backdoor between sealed (inactive) and unsealed (active).
     * @param firstHostID  ID of the first host.
     * @param secondHostID ID of the second host.
     * @return A message indicating the new state of the backdoor.
     */
    public String sealBackdoor(String firstHostID, String secondHostID) {
        // Hosts must exist and cannot link to themselves
        if (!hostTable.containsID(firstHostID) || !hostTable.containsID(secondHostID) ||
                firstHostID.equals(secondHostID)) {
            return "Some error occurred in seal_backdoor.";
        }
        Host firstHost = hostTable.get(firstHostID);
        Host secondHost = hostTable.get(secondHostID);

        // Search for the specific backdoor object connecting these two hosts
        Backdoor backdoor = null;
        LinkedList<Backdoor> backdoorLinkedList = firstHost.getAdjacencyList();
        for (Backdoor link : backdoorLinkedList) {
            Host neighborVertex = link.getBackdoorEnd(firstHost);
            String neighborID = neighborVertex.getHostID();
            if (neighborID.equals(secondHost.getHostID())) {
                backdoor = link;
                break;
            }
        }

        // If no link exists, we cannot seal or unseal
        if (backdoor == null) {
            return "Some error occurred in seal_backdoor.";
        }

        // If sealed we unseal, If unsealed we seal
        if (backdoor.isSealed()) {
            backdoor.setSealed(false);

            // Readd bandwidth to global pool
            totalBandwidth += backdoor.getBandwidthCapacity();
            totalUnsealedBackdoors++;

            return "Backdoor " + firstHostID + " <-> " + secondHostID + " unsealed.";
        }
        else {
            backdoor.setSealed(true);

            // Remove bandwidth from global pool
            totalBandwidth -= backdoor.getBandwidthCapacity();
            totalUnsealedBackdoors--;

            return "Backdoor " + firstHostID + " <-> " + secondHostID + " sealed.";
        }
    }

    /**
     * Finds the shortest path between two hosts using Dijkstra's algorithm.
     * Supports both standard latency and lambda based calculations.
     * @param sourceID     The starting host ID.
     * @param destID       The destination host ID.
     * @param minBandwidth The minimum required bandwidth for a valid path.
     * @param lambda       Penalty factor and if 0, standard Dijkstra is used.
     * @return A string representing the optimal path or failure message.
     */
    public String traceRoute(String sourceID, String destID, int minBandwidth, int lambda) {
        if (!hostTable.containsID(sourceID) || !hostTable.containsID(destID)) {
            return "Some error occurred in trace_route.";
        }

        // When source is the destination
        if (sourceID.equals(destID)) {
            return "Optimal route " + sourceID + " -> " + destID + ": " + sourceID + " (Latency = 0ms)";
        }

        // Initialize minimum heap for Dijkstra
        MinimumHeap minHeap = new MinimumHeap();
        Host sourceHost = hostTable.get(sourceID);
        Path initialPath = new Path(0, 0, sourceHost, null);
        minHeap.insert(initialPath);

        // Choose the strategy based on lambda value
        // If zero, take care only of latencies. If greater than zero, consider how many edges passed.
        if (lambda == 0) {
            return solveDijkstraWithoutLambda(minHeap, destID, minBandwidth);
        } else {
            return solveDijkstraWithLambda(minHeap, destID, minBandwidth, lambda);
        }
    }

    /**
     * Checks the connectivity of the entire graph.
     *
     * @return A message stating if the graph is fully connected or how many disconnected components exist.
     */
    public String scanConnectivity() {
        int totalHosts = hostTable.getSize();
        if (totalHosts <= 1) {
            return "Network is fully connected.";
        }

        // Count components
        // excludedHost equal to null means we have to scan entire graph
        int numComponents = countConnectedComponents(this.hostTable, null);


        // If there is only a single connected component, it means a path exists between every pair of hosts
        // Otherwise, the graph is fragmented into multiple isolated graphs
        if (numComponents == 1) {
            return "Network is fully connected.";
        }
        else {
            return "Network has " + numComponents + " disconnected components.";
        }
    }

    /**
     * Simulates the removal of a specific host to check if it is an articulation point.
     * @param hostID The ID of the host to remove.
     * @return A message indicating if the host is critical for connectivity.
     */
    public String simulateBreach(String hostID) {
        // The host must exist since we remove it
        if(!hostTable.containsID(hostID)) {
            return "Some error occurred in simulate_breach.";
        }
        Host removedHost = hostTable.get(hostID);

        // Calculate components in original state
        int currentComponents = countConnectedComponents(this.hostTable, null);

        // Calculate components simulating the host's removal
        int newComponents = countConnectedComponents(this.hostTable, removedHost);

        int totalHosts = hostTable.getSize();
        int remainingHosts = totalHosts - 1;

        // Determine if connectivity disturbed
        if (remainingHosts <= 1 || newComponents <= currentComponents) {
            return "Host " + hostID + " is NOT an articulation point. Network remains the same.";
        }
        else {
            return "Host " + hostID + " IS an articulation point.\nFailure results in " + newComponents + " disconnected components.";
        }
    }

    /**
     * Simulates the removal of a link to check if it is a bridge.
     *
     * @param firstHostID  ID of the first host.
     * @param secondHostID ID of the second host.
     * @return A message indicating if the connection is critical for connectivity.
     */
    public String simulateBreach(String firstHostID, String secondHostID) {
        // The hosts must exist and must not be the same since we remove the edge between them
        if (!hostTable.containsID(firstHostID) || !hostTable.containsID(secondHostID) || firstHostID.equals(secondHostID)) {
            return "Some error occurred in simulate_breach.";
        }
        Host host1 = hostTable.get(firstHostID);
        Backdoor backdoor = null;

        // Find the edge connecting these two hosts
        for (Backdoor link : host1.getAdjacencyList()) {
            Host neighbor = link.getBackdoorEnd(host1);
            if (neighbor != null && neighbor.getHostID().equals(secondHostID)) {
                backdoor = link;
                break;
            }
        }
        // If there is no backdoor we return an error message
        if (backdoor == null || backdoor.isSealed()) {
            return "Some error occurred in simulate_breach.";
        }

        // Count components with link active
        int currentComponents = countConnectedComponents(this.hostTable, null);

        // Seal link temporarily to simulate failure
        backdoor.setSealed(true);
        int newComponents = countConnectedComponents(this.hostTable, null);

        // Restore link state
        backdoor.setSealed(false);

        // Compare component counts
        if (newComponents > currentComponents) {
            return "Backdoor " + firstHostID + " <-> " + secondHostID + " IS a bridge.\nFailure results in " + newComponents + " disconnected components.";
        }
        else {
            return "Backdoor " + firstHostID + " <-> " + secondHostID + " is NOT a bridge. Network remains the same.";
        }
    }

    /**
     * Generates a comprehensive report of the graph status.
     * Includes host count, connectivity, cycle detection, and averages.
     * @return The report as a formatted string.
     */
    public String oracleReport() {
        String outputString = "--- Resistance Network Report ---\n";

        // Get counters from the table
        int totalHosts = hostTable.getSize();
        outputString += "Total Hosts: " + totalHosts + "\n";
        outputString += "Total Unsealed Backdoors: " + totalUnsealedBackdoors + "\n";

        // Perform BFS analysis to understand the graph status.
        int[] BFSStats = analyzeGraphWithBFS();
        int totalComponents = BFSStats[0];
        int hasCycles = BFSStats[1];

        // Determine global connectivity status.
        // If there is only 1 component, all nodes are reachable.
        // If there are 0 or 1 hosts, we consider it connected.
        boolean isConnected;
        if ((totalHosts <= 1) || (totalComponents == 1)) {
            isConnected = true;
        }
        else {
            isConnected = false;
        }

        outputString += "Network Connectivity: ";
        if (isConnected) {
            outputString += "Connected";
        }
        else {
            outputString += "Disconnected";
        }
        outputString += "\n";

        // Handle logic for Connected Components count.
        // If there are no hosts, components should be 0. If 1 host, it's 1 component.
        int componentString;
        if (totalHosts == 0) {
            componentString = 0;
        }
        else if (totalHosts == 1) {
            componentString = 1;
        }
        else {
            componentString = totalComponents;
        }
        outputString += "Connected Components: " + componentString + "\n";

        // Report if cycles were detected during BFS traversal
        outputString += "Contains Cycles: ";
        if (hasCycles == 1) {
            outputString += "Yes";
        }
        else {
            outputString += "No";
        }
        outputString += "\n";

        // Calculate average bandwidth.
        // Must check if totalUnsealedBackdoors greater than zero to avoid DivisionByZero exception.
        double avgBandwidth = 0.0;
        if (totalUnsealedBackdoors > 0) {
            avgBandwidth = (double) totalBandwidth / totalUnsealedBackdoors;
        }
        // Round the result
        double roundedBandwidth = (double) Math.round(avgBandwidth * 10) / 10.0;
        outputString += "Average Bandwidth: " + roundedBandwidth + "Mbps\n";

        // Calculate average clearance level.
        // Similar check for totalHosts > 0 to prevent division by zero.
        double avgClearance = 0.0;
        if (totalHosts > 0) {
            avgClearance = (double) totalClearance / totalHosts;
        }
        double roundedClearance = (double) Math.round(avgClearance * 10) / 10.0;
        outputString += "Average Clearance Level: " + roundedClearance;

        return outputString;
    }

    /**
     * Helper method to analyze the graph using Breadth-First Search.
     * Calculates the number of connected components and detects cycles.
     * @return An integer array: [0] = Component Count, [1] = Cycle Status (1 for Yes, 0 for No).
     */
    private int[] analyzeGraphWithBFS() {
        int componentCount = 0;
        boolean hasCycle = false;

        LinkedList<Host> allHosts = hostTable.getAllUsers();
        HashTable<Boolean> visited = new HashTable<>();

        // Map to track the parent of each node in the BFS tree.
        HashTable<String> parentMap = new HashTable<>();

        // Iterate through all hosts to ensure we find every disconnected component
        for (Host startNode : allHosts) {
            String startID = startNode.getHostID();

            // If a node hasn't been visited yet, it belongs to a new, unexplored component.
            if (!visited.containsID(startID)) {
                // Increment component counter for this new island
                componentCount++;

                LinkedList<Host> queue = new LinkedList<>();
                queue.add(startNode);
                visited.put(startID, true);

                while (!queue.isEmpty()) {
                    Host current = queue.removeFirst();
                    String currentID = current.getHostID();

                    // Get who comes before this node
                    String parentID = parentMap.get(currentID);

                    for (Backdoor link : current.getAdjacencyList()) {
                        // Ignore sealed connections
                        if (link.isSealed()) {
                            continue;
                        }

                        Host neighbor = link.getBackdoorEnd(current);
                        String neighborID = neighbor.getHostID();

                        if (!visited.containsID(neighborID)) {
                            //Mark visited, map the parent, and enqueue
                            visited.put(neighborID, true);
                            parentMap.put(neighborID, currentID);
                            queue.add(neighbor);
                        }
                        else {
                            // Node is already visited.
                            // If neighbor is not equal to parentID, it means we found a back edge to an ancestor.
                            if (parentID != null) {
                                if (!neighborID.equals(parentID)) {
                                    hasCycle = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Convert boolean flag to integer for the result array
        int cycleStatus;
        if (hasCycle) {
            cycleStatus = 1;
        } else {
            cycleStatus = 0;
        }

        int[] resultArray = new int[2];
        resultArray[0] = componentCount;
        resultArray[1] = cycleStatus;

        return resultArray;
    }

    /**
     * Implements standard Dijkstra's algorithm.
     * @param minHeap      The minimum heap containing paths to explore.
     * @param destID       The target host ID.
     * @param minBandwidth The minimum required bandwidth constraint.
     * @return A formatted string of the shortest path found.
     */
    private String solveDijkstraWithoutLambda(MinimumHeap minHeap, String destID, int minBandwidth) {
        // Map to store the minimum latency found so far for each host.
        HashTable<Integer> shortestLatency = new HashTable<>();

        // Set to keep track of finalized nodes.
        // Once a node is extracted from the MinHeap, we have found the shortest path to it.
        HashTable<Boolean> visited = new HashTable<>();
        Path startNode = minHeap.findMin();
        String startID = startNode.getDestinationHostID();

        while (minHeap.getSize() > 0) {
            Path currentPath = minHeap.deleteMin(); // Extract the node with the lowest current latency

            Host currentHost = currentPath.getDestinationHost();
            String currentHostID = currentHost.getHostID();

            //If we have already visited this host,any subsequent occurrences in the heap are longer paths.
            if (visited.containsID(currentHostID)) {
                continue;
            }
            visited.put(currentHostID, true);

            // Check if we have reached the destination host
            if (currentHostID.equals(destID)) {
                LinkedList<String> hostSequence = currentPath.getHostSequence();
                if (hostSequence.isEmpty()) {
                    return "";
                }

                // Build the path string
                String path = hostSequence.get(0);
                for (int i = 1; i < hostSequence.size(); i++) {
                    path += " -> " + hostSequence.get(i);
                }

                int totalLatency = currentPath.getTotalDynamicLatency();
                String sourceHost = hostSequence.getFirst();
                String destinationHost = hostSequence.getLast();

                return "Optimal route " + sourceHost+ " -> " + destinationHost +
                        ": " + path + " (Latency = " + totalLatency + "ms)";
            }

            LinkedList<Backdoor> neighbors = currentHost.getAdjacencyList();
            int currentSegmentCount = currentPath.getSegmentCount();

            // Iterate through all neighbors to explore potential next steps
            for (Backdoor backdoor : neighbors) {
                // Checking constraints
                if (backdoor.isSealed()) {
                    continue;
                }
                if (backdoor.getBandwidthCapacity() < minBandwidth) {
                    continue;
                }
                if (currentHost.getClearanceLevel() < backdoor.getFireSecurityLevel()) {
                    continue;
                }

                Host nextHost = backdoor.getBackdoorEnd(currentHost);
                if (nextHost == null) {
                    continue;
                }
                String nextHostID = nextHost.getHostID();

                // Skip if the neighbor is already finalized
                if (visited.containsID(nextHostID)) {
                    continue;
                }

                // Calculate the total latency to reach this neighbor by means of current path
                int newTotalLatency = currentPath.getTotalDynamicLatency() + backdoor.getBaseLatency();
                int newSegmentCount = currentSegmentCount + 1;

                // Check if we found a shorter path to nextHost than previously known
                Integer currentLatency = shortestLatency.get(nextHostID);

                if (currentLatency != null) {
                    // If the new path is longer or equal to what we already found, ignore it
                    if (newTotalLatency > currentLatency) {
                        continue;
                    }
                }

                // Update the shortest known latency and add the new path to the miminum heap
                shortestLatency.put(nextHostID, newTotalLatency);

                Path newPath = new Path(newTotalLatency, newSegmentCount, nextHost, currentPath);
                minHeap.insert(newPath);
            }
        }

        return "No route found from " + startID + " to " + destID;
    }

    /**
     * Implements modified Dijkstra's algorithm taking lambda  into account.
     * Latency increases based on the number of hops
     * @param pathHeap              The minimum heap of paths
     * @param destinationID         The target host ID.
     * @param minBandwidth          The minimum required bandwidth.
     * @param lambda The penalty added per hop.
     * @return A formatted string of the optimal path.
     */
    private String solveDijkstraWithLambda(MinimumHeap pathHeap, String destinationID, int minBandwidth, int lambda) {
        // In standard Dijkstra, we map HostID to an  integer.
        // Here, we map HostID to an int array.
        // Index of array shows how many links traversed.
        // Value at index means minimum latency found for that specific segment count.
        HashTable<int[]> visitedHostStateTable = new HashTable<>();

        Path startPath = pathHeap.findMin();
        // Assuming MinHeap is not empty initially
        Host startHost = startPath.getDestinationHost();
        String startID = startHost.getHostID();

        // Initialize state array for start node.
        // At Hop 0, the latency is 0. All other hop counts are initialized to Infinity.
        int[] initialLatencies = new int[20];
        for (int i = 0; i < 20; i++) {
            initialLatencies[i] = Integer.MAX_VALUE;
        }
        initialLatencies[0] = 0;
        visitedHostStateTable.put(startID, initialLatencies);

        while (pathHeap.getSize() > 0) {
            Path currentPath = pathHeap.deleteMin();

            Host currentHost = currentPath.getDestinationHost();
            String currentHostID = currentHost.getHostID();

            int currentTotalLatency = currentPath.getTotalDynamicLatency();
            int currentHopCount = currentPath.getSegmentCount();

            // Get the history of best latencies for this host
            int[] existingHopLatencies = visitedHostStateTable.get(currentHostID);
            boolean isBetterPathExist = false;

            // We determine if the currentPath is worth exploring further.
            // If we have previously reached this host with fewer hops
            // and that previous path had lower or equal latency, then currentPath is strictly worse.
            if (existingHopLatencies != null) {
                int comparisonLimit;
                // Determine loop bounds safely based on array size
                if (currentHopCount < existingHopLatencies.length) {
                    comparisonLimit = currentHopCount;
                }
                else {
                    comparisonLimit = existingHopLatencies.length - 1;
                }

                // Check against all paths with fewer hops
                for (int i = 0; i < comparisonLimit; i++) {
                    if (existingHopLatencies[i] <= currentTotalLatency) {
                        isBetterPathExist = true; // A better path exists, ignore current.
                        break;
                    }
                }

                // Also check if we already found a better latency for the same hop count
                if (!isBetterPathExist) {
                    if (currentHopCount < existingHopLatencies.length) {
                        if (existingHopLatencies[currentHopCount] < currentTotalLatency) {
                            isBetterPathExist = true;
                        }
                    }
                }
            }

            if (isBetterPathExist) {
                continue;
            }


            if (currentHostID.equals(destinationID)) {
                LinkedList<String> hostSequence = currentPath.getHostSequence();

                if (hostSequence.isEmpty()) {
                    return "";
                }

                // Reconstruct the path string manually
                String outputString = hostSequence.get(0);
                for (int i = 1; i < hostSequence.size(); i++) {
                    outputString += " -> " + hostSequence.get(i);
                }

                int totalLatency = currentPath.getTotalDynamicLatency();

                String sourceHost = hostSequence.getFirst();
                String destinationHost = hostSequence.getLast();

                String finalResult = "Optimal route " + sourceHost + " -> " + destinationHost+ ": " + outputString+ " (Latency = " + totalLatency + "ms)";
                return finalResult;
            }

            // Explore neighbors
            LinkedList<Backdoor> neighbors = currentHost.getAdjacencyList();

            for (Backdoor backdoor : neighbors) {
                // Apply constraints
                if (backdoor.isSealed()) {
                    continue;
                }
                if (backdoor.getBandwidthCapacity() < minBandwidth) {
                    continue;
                }
                if (currentHost.getClearanceLevel() < backdoor.getFireSecurityLevel()) {
                    continue;
                }

                Host nextHost = backdoor.getBackdoorEnd(currentHost);
                if (nextHost == null) {
                    continue;
                }
                String nextHostID = nextHost.getHostID();

                int newHopCount = currentHopCount + 1;

                // Calculate dynamic latency

                int effectiveLatency = backdoor.getBaseLatency() + (lambda * (newHopCount - 1));
                int newTotalLatency = currentTotalLatency + effectiveLatency;

                // Do dynamic array resizing
                int[] neighborHopLatencies = visitedHostStateTable.get(nextHostID);

                if (neighborHopLatencies == null) {
                    // First time visiting this neighbor
                    // Create new array
                    int initialSize = 20;
                    if (newHopCount + 1 > initialSize) {
                        initialSize = newHopCount + 1;
                    }
                    neighborHopLatencies = new int[initialSize];

                    for (int k = 0; k < neighborHopLatencies.length; k++) {
                        neighborHopLatencies[k] = Integer.MAX_VALUE;
                    }
                    visitedHostStateTable.put(nextHostID, neighborHopLatencies);

                }
                else if (newHopCount >= neighborHopLatencies.length) {
                    // Array is too small for current hop count
                    // Resize
                    int newSize = neighborHopLatencies.length * 2;
                    if (newHopCount + 1 > newSize) {
                        newSize = newHopCount + 1;
                    }

                    int[] expandedArray = new int[newSize];
                    for (int i = 0; i < expandedArray.length; i++) {
                        if (i < neighborHopLatencies.length) {
                            expandedArray[i] = neighborHopLatencies[i];
                        } else {
                            expandedArray[i] = Integer.MAX_VALUE;
                        }
                    }
                    neighborHopLatencies = expandedArray;
                    visitedHostStateTable.put(nextHostID, neighborHopLatencies);
                }

                // Before adding to the heap, check if this new path is the best amongst the existing entries
                boolean isPathEfficient = true;

                // Loop through all hop counts smaller than or equal to newHopCount
                for (int i = 0; i <= newHopCount; i++) {
                    if (neighborHopLatencies[i] <= newTotalLatency) {
                        isPathEfficient = false; // Found a path that is faster
                        break;
                    }
                }

                if (isPathEfficient) {
                    // Update the state table with the new best latency for this hop count
                    neighborHopLatencies[newHopCount] = newTotalLatency;
                    pathHeap.insert(new Path(newTotalLatency, newHopCount, nextHost, currentPath));
                }
            }
        }

        return "No route found from " + startID + " to " + destinationID;
    }


    /**
     * Counts the number of connected components in the graph using BFS.
     * Simulates the removal of a host for articulation point detection.
     * @param currentHostTable The table of hosts to analyze.
     * @param excludedHost     A host to ignore during traversal
     * @return The number of connected components.
     */
    public int countConnectedComponents(HostTable<Host> currentHostTable, Host excludedHost) {
        LinkedList<Host> allHosts = currentHostTable.getAllUsers();
        HashTable<Boolean> visited = new HashTable<>();

        // If excludedHost is provided, mark it as visited upfront.
        // This removes it from the graph traversal.
        if(excludedHost != null) {
            visited.put(excludedHost.getHostID(), true);
        }

        int componentCount = 0;

        for (Host host : allHosts) {
            String hostID = host.getHostID();

            // Ensure we don't start traversal from the removed host
            if (excludedHost != null && hostID.equals(excludedHost.getHostID())) {
                continue;
            }

            // If node is unvisited, it's the start of a new disconnected component
            if (visited.get(hostID) == null || !visited.get(hostID)) {
                componentCount++;

                // Start BFS traversal for this component
                LinkedList<Host> queue = new LinkedList<>();
                queue.add(host);
                visited.put(hostID, true);

                while (!queue.isEmpty()) {
                    Host currentHost = queue.removeFirst();
                    for (Backdoor backdoor : currentHost.getAdjacencyList()) {

                        if (!backdoor.isSealed()) {
                            Host neighborHost = backdoor.getBackdoorEnd(currentHost);
                            String neighborID = neighborHost.getHostID();

                            // Only visit neighbor if it's not the excluded host and not already visited
                            if ((excludedHost == null || !neighborID.equals(excludedHost.getHostID())) &&
                                    (visited.get(neighborID) == null || !visited.get(neighborID))) {
                                visited.put(neighborID, true);
                                queue.add(neighborHost);
                            }
                        }
                    }
                }
            }
        }

        // Determine the number of valid hosts remaining in the graph
        int activeHostCount = allHosts.size();

        if(excludedHost != null) {
            activeHostCount--;
        }

        // If 0 or 1 host remains, the component count is just the host count.
        if (activeHostCount <= 1) {
            return activeHostCount;
        }

        return componentCount;
    }
}