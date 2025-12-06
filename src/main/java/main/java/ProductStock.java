package main.java;

public class ProductStock {

    private final String productId;
    private String location;       // e.g., "WH-1-A3"
    private int onHand;            // physical units stored
    private int reserved;          // units reserved for orders
    private int reorderThreshold;  // when available < threshold → reorder needed
    private int maxCapacity;       // max units this location can store

    /**
     * Creates a ProductStock instance with basic inventory info.
     *
     * @param productId        unique ID of the product (must not be null/blank)
     * @param location         storage location code (must not be null/blank)
     * @param initialOnHand    initial on-hand quantity (>= 0)
     * @param reorderThreshold threshold for triggering reorder (>= 0)
     * @param maxCapacity      maximum capacity of this location (> 0)
     */
    public ProductStock(String productId,
                        String location,
                        int initialOnHand,
                        int reorderThreshold,
                        int maxCapacity) {

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location must not be null or blank");
        }
        if (initialOnHand < 0) {
            throw new IllegalArgumentException("initialOnHand must be >= 0");
        }
        if (reorderThreshold < 0) {
            throw new IllegalArgumentException("reorderThreshold must be >= 0");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be > 0");
        }
        if (initialOnHand > maxCapacity) {
            throw new IllegalArgumentException("initialOnHand exceeds maxCapacity");
        }

        this.productId = productId;
        this.location = location;
        this.onHand = initialOnHand;
        this.reserved = 0;
        this.reorderThreshold = reorderThreshold;
        this.maxCapacity = maxCapacity;
    }

    // ---------- Getters ----------

    public String getProductId() {
        return productId;
    }

    public String getLocation() {
        return location;
    }

    public int getOnHand() {
        return onHand;
    }

    public int getReserved() {
        return reserved;
    }

    /**
     * Available stock = onHand - reserved.
     */
    public int getAvailable() {
        return onHand - reserved;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    // ---------- Mutating operations with business rules ----------

    /**
     * Change physical location of the stock.
     */
    public void changeLocation(String newLocation) {
        if (newLocation == null || newLocation.isBlank()) {
            throw new IllegalArgumentException("newLocation must not be null or blank");
        }
        this.location = newLocation;
    }

    /**
     * Adds stock to on-hand quantity. Fails if amount is not positive
     * or if the operation would exceed maxCapacity.
     */
    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        if (onHand + amount > maxCapacity) {
            throw new IllegalStateException("Cannot add stock beyond maxCapacity");
        }
        onHand += amount;
    }

    /**
     * Removes stock from on-hand as damaged/expired.
     * Cannot remove more than onHand.
     */
    public void removeDamaged(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to remove must be positive");
        }
        if (amount > onHand) {
            throw new IllegalStateException("Cannot remove more than on-hand quantity");
        }
        onHand -= amount;
        // reserved is unchanged here
        if (reserved > onHand) {
            // Safety: never allow reserved > onHand
            reserved = onHand;
        }
    }

    /**
     * Reserves stock for a customer order.
     * Cannot reserve more than available.
     */
    public void reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reserve must be positive");
        }
        if (amount > getAvailable()) {
            throw new IllegalStateException("Insufficient available stock to reserve");
        }
        reserved += amount;
    }

    /**
     * Releases (un-reserves) previously reserved stock.
     * Cannot release more than currently reserved.
     */
    public void releaseReservation(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to release must be positive");
        }
        if (amount > reserved) {
            throw new IllegalStateException("Cannot release more than reserved");
        }
        reserved -= amount;
    }

    /**
     * Confirms shipment: removes stock from on-hand and reserved at the same time.
     * This assumes the amount was previously reserved.
     */
    public void shipReserved(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to ship must be positive");
        }
        if (amount > reserved) {
            throw new IllegalStateException("Cannot ship more than reserved");
        }
        if (amount > onHand) {
            // Should not happen if invariant reserved <= onHand holds,
            // but we guard anyway.
            throw new IllegalStateException("On-hand quantity is not enough to ship");
        }

        reserved -= amount;
        onHand -= amount;
    }

    /**
     * Returns true if available stock is below reorder threshold.
     */
    public boolean isReorderNeeded() {
        return getAvailable() < reorderThreshold;
    }

    /**
     * Updates the reorder threshold, must be >= 0 and <= maxCapacity.
     */
    public void updateReorderThreshold(int newThreshold) {
        if (newThreshold < 0) {
            throw new IllegalArgumentException("reorderThreshold must be >= 0");
        }
        if (newThreshold > maxCapacity) {
            throw new IllegalArgumentException("reorderThreshold cannot exceed maxCapacity");
        }
        this.reorderThreshold = newThreshold;
    }

    /**
     * Updates max capacity. Cannot be less than current onHand.
     */
    public void updateMaxCapacity(int newMaxCapacity) {
        if (newMaxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be > 0");
        }
        if (newMaxCapacity < onHand) {
            throw new IllegalStateException("New maxCapacity is less than current onHand");
        }
        this.maxCapacity = newMaxCapacity;
        if (reorderThreshold > maxCapacity) {
            reorderThreshold = maxCapacity;
        }
    }

    @Override
    public String toString() {
        return "ProductStock{" +
                "productId='" + productId + '\'' +
                ", location='" + location + '\'' +
                ", onHand=" + onHand +
                ", reserved=" + reserved +
                ", available=" + getAvailable() +
                ", reorderThreshold=" + reorderThreshold +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}
