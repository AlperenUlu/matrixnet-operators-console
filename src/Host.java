import java.util.LinkedList;

/**
 * Represents a node in the graph structure.
 * Contains identity, clearance level, and connections.
 */
public class Host {
    private final String hostID;
    private final int clearanceLevel;
    private LinkedList<Backdoor> adjacencyList; // List of connections with other hosts

    /**
     * Constructor to initialize the Host.
     * @param hostID The unique name/ID.
     * @param clearanceLevel The security rank.
     */
    Host(String hostID, int clearanceLevel) {
        this.hostID = hostID;
        this.clearanceLevel = clearanceLevel;
        this.adjacencyList = new LinkedList<>(); // Initialize the list to prevent null errors
    }

    /**
     * @return The host ID.
     */
    public String getHostID() {
        return hostID;
    }

    /**
     * @return The clearance level.
     */
    public int getClearanceLevel() {
        return clearanceLevel;
    }

    /**
     * Returns the list of connections.
     * @return The adjacency list.
     */
    public LinkedList<Backdoor> getAdjacencyList() {
        return adjacencyList;
    }
}