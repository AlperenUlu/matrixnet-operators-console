import java.util.LinkedList;

/**
 * A specialized Hash Table to store and manage Host objects.
 * Keeps a separate list of all users for quick operations.
 * @param <T> A generic type that extends the Host class.
 */
public class HostTable<T extends Host> {

    private int capacity; // The fixed size of the hash table array
    private int size; // The current count of hosts in the table
    private LinkedList<T>[] hashTable; // The array of buckets where hosts are stored
    private LinkedList<T> allUsersList; // A separate list to keep track of all added users when needed.

    /**
     * Constructor to initialize the HostTable.
     * Sets up the internal storage arrays.
     */
    HostTable() {
        this.capacity = 200017; // Using a large prime number for capacity to reduce collisions
        this.hashTable = new LinkedList[this.capacity];
        this.allUsersList = new LinkedList<>();
        this.size = 0;
    }

    /**
     * Adds a Host to the table and the global list.
     * @param host The host object to add.
     */
    public void put(T host) {
        // Calculate the index
        String userID = host.getHostID();
        int hashcode = hashcodeGenerator(userID);
        int position = hashcode % capacity;

        // If the bucket is empty, create a new list
        if (hashTable[position] == null) {
            hashTable[position] = new LinkedList<>();
        }

        LinkedList<T> userList = hashTable[position];
        userList.add(host); // Add the host to the specific bucket

        // Add to the linked list to get all users faster
        allUsersList.add(host);
        size++;
    }

    /**
     * Gives a Host object using its unique ID.
     * @param userID The ID of the host to find.
     * @return The Host object if found, null otherwise.
     */
    public T get(String userID) {
        // Calculate the index
        int hashcode = hashcodeGenerator(userID);
        int position = hashcode % capacity;

        // If the bucket is empty, the user is not here
        if (hashTable[position] == null) return null;

        LinkedList<T> userList = hashTable[position];
        // Iterate through the bucket to find the matching ID
        for (T user : userList) {
            if (user.getHostID().equals(userID)) {
                return user; // Return the matching host
            }
        }
        return null; // Return null if not found in the bucket
    }

    /**
     * Checks if a Host with the given ID exists in the table.
     * @param userID The ID to check.
     * @return True if exists, false otherwise.
     */
    public boolean containsID(String userID) {
        // Calculate the index
        int hashcode = hashcodeGenerator(userID);
        int position = hashcode % capacity;

        // If the bucket is empty, it definitely doesn't exist
        if (hashTable[position] == null) return false;

        // Check the list in this bucket
        for (T user : hashTable[position]) {
            if (user.getHostID().equals(userID)) {
                return true; // ID found
            }
        }
        return false; // ID not found
    }

    /**
     * @return The size count.
     */
    public int getSize() {
        return size;
    }

    /**
     * Generates a valid array index from the ID.
     * @param ID The string key.
     * @return A positive integer hash code.
     */
    public int hashcodeGenerator(String ID) {
        int hashcode = ID.hashCode();

        // Handle edge case for minimum integer value
        if (hashcode == Integer.MIN_VALUE) {
            return 0;
        }
        return Math.abs(hashcode); // Ensure the result is positive
    }

    /**
     * Returns the linear list of all users.
     * @return A LinkedList containing all hosts.
     */
    public LinkedList<T> getAllUsers() {
        return allUsersList;
    }
}