/**
 * Represents a connection link between two Hosts.
 * Stores details about bandwidth capacity, latency, and security.
 */
public class Backdoor {
    private final Host firstHost; // The first endpoint of the connection
    private final Host secondHost; // The second endpoint of the connection
    private final int bandwidthCapacity;
    private final int baseLatency;
    private final int fireSecurityLevel;
    private boolean isSealed; // Status flag indicating if the link is blocked

    /**
     * Constructor to initialize the connection details.
     * @param firstHost One end of the link.
     * @param secondHost The other end of the link.
     * @param baseLatency The delay value.
     * @param bandwidthCapacity The data capacity.
     * @param fireSecurityLevel The firewall level.
     */
    Backdoor(Host firstHost , Host secondHost, int baseLatency , int bandwidthCapacity, int fireSecurityLevel){
        this.firstHost = firstHost;
        this.secondHost = secondHost;
        this.bandwidthCapacity = bandwidthCapacity;
        this.baseLatency = baseLatency;
        this.fireSecurityLevel = fireSecurityLevel;
        this.isSealed = false; // Initialize as open (not sealed) by default
    }
    /**
     * Returns the bandwidth capacity.
     * @return The capacity value.
     */
    public int getBandwidthCapacity() {
        return bandwidthCapacity;
    }

    /**
     * @return The latency value.
     */
    public int getBaseLatency() {
        return baseLatency;
    }

    /**
     * @return The security level.
     */
    public int getFireSecurityLevel() {
        return fireSecurityLevel;
    }

    /**
     * @return True if sealed, false otherwise.
     */
    public boolean isSealed() {
        return isSealed;
    }

    /**
     * Updates the sealed status of the connection.
     * @param sealed The new status (true to block, false to open).
     */
    public void setSealed(boolean sealed) {
        isSealed = sealed; // Update the sealed flag
    }

    /**
     * Returns the neighbor host given one end of the connection.
     * @param firstHost The host we are looking from.
     * @return The other host connected to this backdoor.
     */
    public Host getBackdoorEnd(Host firstHost) {
        if (this.firstHost.equals(firstHost)) {
            return this.secondHost; // If input is first, return the second host
        } else if (this.secondHost.equals(firstHost)) {
            return this.firstHost; // If input is second, return the first host
        }
        return null; // Return null if the input host is not part of this connection
    }
}