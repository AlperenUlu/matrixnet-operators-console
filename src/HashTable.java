import java.util.LinkedList;

/**
 * An implementation of a Hash Table data structure.
 * Stores values associated with unique String keys.
 * Uses separate chaining by usage of linked lists to handle collisions.
 * @param <V> The type of value to be stored.
 */
public class HashTable<V> {

    private int capacity; // The fixed size of the internal array
    private LinkedList<Entry<V>>[] table; // The array where each index holds a list of entries

    /**
     * Constructor to initialize the Hash Table.
     * Sets a fixed capacity and prepares the table.
     */
    public HashTable() {
        this.capacity = 50077; // Assigning a prime number size to reduce collisions
        this.table = new LinkedList[this.capacity];
    }

    /**
     * Adds a new key-value pair or updates an existing one.
     * @param key The unique identifier.
     * @param value The value associated with the key.
     */
    public void put(String key, V value) {
        // Calculate the index for the key
        int position = hashcodeGenerator(key) % capacity;

        // If the slot is empty, create a new linked list there
        if (table[position] == null) {
            table[position] = new LinkedList<>();
        }

        // Get the list at this position
        LinkedList<Entry<V>> entryList = table[position];

        // Check if the key already exists in the list
        for (Entry<V> entry : entryList) {
            if (entry.getKey().equals(key)) {
                entry.setValue(value); // Update the value if key exists
                return;
            }
        }

        // Create a new entry since the key is new
        Entry<V> newEntry = new Entry<>(key, value);
        entryList.add(newEntry);
    }

    /**
     * Gives the value associated with a specific key.
     * @param key The unique identifier to look for.
     * @return The value if found, or null if not found.
     */
    public V get(String key) {
        // Calculate the index
        int position = hashcodeGenerator(key) % capacity;

        // If the slot is empty, the key is not here
        if (table[position] == null) return null;

        LinkedList<Entry<V>> entryList = table[position];

        // Search through the list for the key
        for (Entry<V> entry : entryList) {
            if (entry.getKey().equals(key)) {
                return entry.getValue(); // Key found
            }
        }
        return null; // Key not found
    }

    /**
     * Checks if a key exists in the table.
     * @param key The unique identifier to check.
     * @return True if the key exists, false otherwise.
     */
    public boolean containsID(String key) {
        // Calculate the index
        int position = hashcodeGenerator(key) % capacity;

        // If slot is empty, key does not exist
        if (table[position] == null) return false;

        LinkedList<Entry<V>> entryList = table[position];

        // Search the list for the key
        for (Entry<V> entry : entryList) {
            if (entry.getKey().equals(key)) {
                return true; // Key found
            }
        }
        return false; // Key not found
    }

    /**
     * Generates a valid array index from the key.
     * Ensures the result is always positive.
     * @param ID The string key.
     * @return A positive integer hash code.
     */
    public int hashcodeGenerator(String ID) {
        int hashcode = ID.hashCode();

        // Handle the edge case where abs(MIN_VALUE) fails
        if (hashcode == Integer.MIN_VALUE) {
            return 0;
        }
        // Return the positive version of the hash
        return Math.abs(hashcode);
    }
}