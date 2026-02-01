import java.util.LinkedList;

/**
 * Gives the outline of a route in a pathfinding algorithm.
 * Stores the cost, distance, and history to reach a specific Host.
 */
public class Path {

    private final int totalDynamicLatency; // The accumulated latency to reach this point
    private final int segmentCount; // The number of hops  taken so far
    private final Host destinationHost; // The current host at the end of this path segment
    private final Path previousPath; // The previous path node which is used to backtrack the full route
    private LinkedList<String> hostSequence = null; // A list of the full path IDs

    /**
     * Constructor to create a new Path state.
     * @param totalDynamicLatency Total cost calculated so far.
     * @param segmentCount Total hops taken so far.
     * @param destinationHost The host we just reached.
     * @param previousPath The path object leading to this one.
     */
    public Path(int totalDynamicLatency, int segmentCount, Host destinationHost, Path previousPath) {
        this.totalDynamicLatency = totalDynamicLatency;
        this.segmentCount = segmentCount;
        this.destinationHost = destinationHost;
        this.previousPath = previousPath;
    }

    /**
     * @return The total dynamic latency.
     */
    public int getTotalDynamicLatency() {
        return totalDynamicLatency;
    }

    /**
     * Returns the number of hops in this path.
     * @return The segment count.
     */
    public int getSegmentCount() {
        return segmentCount;
    }

    /**
     * @return The host ID string.
     */
    public String getDestinationHostID() {
        return destinationHost.getHostID();
    }

    /**
     * @return The destination Host.
     */
    public Host getDestinationHost() {
        return destinationHost;
    }

    /**
     * @return The parent Path object.
     */
    public Path getPreviousPath() {
        return previousPath;
    }

    /**
     * Reconstructs the full sequence of Host IDs from debut to end.
     * Uses backtracking to build the list.
     * @return A list of host IDs ordered from debut to end.
     */
    public LinkedList<String> getHostSequence() {
        // If the list is already calculated, return the saved one
        if (hostSequence != null) {
            return hostSequence;
        }

        LinkedList<String> sequence = new LinkedList<>(); // Create a new list
        Path current = this; // Start from the current position

        // Loop backwards through the path history
        while (current != null) {
            sequence.addFirst(current.getDestinationHost().getHostID()); // Add current ID to the head
            current = current.getPreviousPath(); // Constructing sequence by moving back one by one
        }
        this.hostSequence = sequence; // Store the result for future operations
        return sequence;
    }
}