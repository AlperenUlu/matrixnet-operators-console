/**
 * Represents a specific key-value pair used within the Hash Table buckets.
 * @param <T> The type of the value stored in this entry.
 */
public class Entry<T> {

    private String key; // The unique key for this entry which is used for hashing.
    private T value;    // The actual value associated with the key.

    /**
     * Constructs a new Entry with the specified key and value.
     * @param key   The key string.
     * @param value The value associated with the key.
     */
    public Entry(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }
}


