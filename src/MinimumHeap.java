import java.util.LinkedList;

/**
 * A Min-Heap data structure specialized for Path objects.
 * Used to efficiently give the path with the highes latency value.
 */
public class MinimumHeap {

    private int capacity;
    private int size;
    private Path[] minHeap; // The array storing the heap

    /**
     * Constructor to initialize the heap.
     * Sets a default large capacity.
     */
    MinimumHeap() {
        this.capacity = 100000;
        this.size = 0;
        this.minHeap = new Path[capacity + 1]; // +1 because index 0 is unused (asked in exam)
    }

    /**
     * Adds a new Path to the heap.
     * Maintains the min-heap property by bubbling the new item up.
     * @param path The path object to add.
     */
    public void insert(Path path) {
        // Check if the array is full and double if it is full
        if (size == minHeap.length - 1) {
            enlargeArray(minHeap.length * 2 + 1);
        }

        // Add the new path to the end and increment size
        minHeap[++size] = path;

        // Move the new item up to its correct position
        percolateUp(size);
    }

    /**
     * Returns the Path with the highest latency without removing it.
     * @return The minimum Path, or null if empty.
     */
    public Path findMin() {
        if (size == 0) {
            return null;
        }
        return minHeap[1];
    }

    /**
     * Removes and returns the Path with the lowest cost.
     * Reorders the heap to maintain structure.
     * @return The minimum Path.
     */
    public Path deleteMin() {
        if (size == 0) {
            return null;
        }

        Path minPath = findMin();
        minHeap[1] = minHeap[size--]; // Move the last item to the root and decrease size

        if (size > 0) {
            percolateDown(1); // Push the new root down to its correct position
        }

        return minPath;
    }

    /**
     * Moves an item up the tree until order is restored.
     * Used during insertion.
     * @param hole The index where the item is currently located.
     */
    private void percolateUp(int hole) {
        Path value = minHeap[hole]; // The item to move

        // Loop while the item is smaller than its parent
        while (hole > 1 && compare(value, minHeap[hole / 2]) < 0) {
            minHeap[hole] = minHeap[hole / 2]; // Move parent down
            hole /= 2; // Move index up
        }

        minHeap[hole] = value;
    }

    /**
     * Moves an item down the tree until order is restored.
     * Used after deletion.
     * @param hole The index where the item starts.
     */
    private void percolateDown(int hole) {
        int child;
        Path temporaryPath = minHeap[hole]; // The item to move

        // Loop while there are children
        while (hole * 2 <= size) {
            child = hole * 2; // Left child index

            // Check if right child exists and is smaller than left child
            if (child != size && compare(minHeap[child + 1], minHeap[child]) < 0) {
                child++; // Select the right child
            }

            // If the child is smaller than the item, swap
            if (compare(minHeap[child], temporaryPath) < 0) {
                minHeap[hole] = minHeap[child]; // Move child up
                hole = child; // Move index down
            }
            else {
                break;
            }
        }
        minHeap[hole] = temporaryPath;
    }

    /**
     * Resizes the internal array when it gets full.
     * @param newSize The new capacity.
     */
    private void enlargeArray(int newSize) {
        Path[] oldHeap = minHeap;
        minHeap = new Path[newSize]; // Create larger array
        // Copy all elements
        for (int i = 0; i < oldHeap.length; i++) {
            minHeap[i] = oldHeap[i];
        }
    }

    /**
     * Compares two Path objects to determine priority.
     * Priority Order:
     * 1. Total Latency (Lower is better)
     * 2. Number of Hops (Lower is better)
     * 3. Host ID Sequence (Alphabetical order)
     * @param firstPath The first path.
     * @param secondPath The second path.
     * @return -1 if first is smaller, 1 if larger, 0 if equal.
     */
    public int compare(Path firstPath, Path secondPath) {
        // Compare by Latency
        int firstLatency = firstPath.getTotalDynamicLatency();
        int secondLatency = secondPath.getTotalDynamicLatency();
        int latencyComparison = compareInteger(firstLatency, secondLatency);

        if (latencyComparison != 0) {
            return latencyComparison;
        }

        // Compare by hops
        int firstCount = firstPath.getSegmentCount();
        int secondCount = secondPath.getSegmentCount();
        int segmentComparison = compareInteger(firstCount, secondCount);

        if (segmentComparison != 0) {
            return segmentComparison;
        }

        // Compare by Host Sequence
        LinkedList<String> firstHostSequence = firstPath.getHostSequence();
        LinkedList<String> secondHostSequence = secondPath.getHostSequence();
        int minSize = Math.min(firstHostSequence.size(), secondHostSequence.size());

        // Compare each ID in the sequence strings
        for (int i = 0; i < minSize; i++) {
            int comparisonResult = compareString(firstHostSequence.get(i), secondHostSequence.get(i));
            if (comparisonResult != 0) return comparisonResult;
        }

        // If one sequence is a part of the other, the shorter is the one we want
        return compareInteger(firstHostSequence.size(), secondHostSequence.size());
    }

    /**
     * Helper to compare two strings.
     * @param firstSequence String 1.
     * @param secondSequence String 2.
     * @return comparison result.
     */
    private int compareString(String firstSequence, String secondSequence) {
        return firstSequence.compareTo(secondSequence);
    }

    /**
     * Helper to compare two integers.
     * @param firstInteger Int 1.
     * @param secondInteger Int 2.
     * @return -1, 0, or 1.
     */
    private int compareInteger(int firstInteger, int secondInteger) {
        if (firstInteger < secondInteger) {
            return -1;
        }
        else if (firstInteger > secondInteger) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the current number of elements.
     * @return The size.
     */
    public int getSize() {
        return size;
    }
}