package sd.series;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Represents the complete set of sales data for a specific day (LocalDate).
 * It maps product names to their respective ProductEvent containers.
 * This class handles the daily aggregation logic and supports both full and filtered serialization.
 */
public class TimeSeries {
    private final Map<String, ProductEvent> events;
    private final LocalDate date;
    private final ReentrantLock lock;
    private boolean isCurrentDay;

    /**
     * Constructs a TimeSeries for a specific date.
     *
     * @param date         The date this series represents.
     * @param isCurrentDay Flag indicating if this is the currently active day (allowing updates).
     */
    public TimeSeries(LocalDate date, boolean isCurrentDay) {
        this.events = new HashMap<>();
        this.date = date;
        this.lock = new ReentrantLock();
        this.isCurrentDay = isCurrentDay;
    }

    /**
     * Gets the date associated with this time series.
     *
     * @return The LocalDate.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Checks if this series represents the current day.
     * Thread-safe.
     *
     * @return True if it is the current day, false otherwise.
     */
    public boolean isCurrentDay() {
        lock.lock();
        try {
            return isCurrentDay;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates the status of whether this is the current day.
     *
     * @param currentDay The new status.
     */
    public void setCurrentDay(boolean currentDay) {
        lock.lock();
        try {
            this.isCurrentDay = currentDay;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a sales event to the appropriate product container.
     * If the product doesn't exist, it creates a new entry.
     *
     * @param event The sales event to add.
     */
    public void addEvent(SalesEvent event) {
        String product = event.getName();
        ProductEvent pe = getOrCreateProductEvent(product);
        pe.addEvent(event);
    }

    /**
     * Retrieves a flattened list of all events across all products.
     * Thread-safe.
     *
     * @return A list of all SalesEvents in this series.
     */
    public List<SalesEvent> getAllEvents() {
        lock.lock();
        try {
            return events.values().stream().
                    flatMap(pe -> pe.getEvent().stream())
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if a specific product exists in this series and has events.
     *
     * @param product The product name to check.
     * @return True if the product exists and is not empty.
     */
    public boolean hasProduct(String product) {
        ProductEvent pe = getProductEvent(product);
        return pe != null && !pe.isEmpty();
    }

    /**
     * Retrieves a set of all product names recorded in this series.
     * Thread-safe.
     *
     * @return A Set of strings representing product names.
     */
    public Set<String> getAllProductNames() {
        lock.lock();
        try {
            return new HashSet<>(events.keySet());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the total quantity sold for a specific product.
     *
     * @param product The product name.
     * @return The total quantity, or 0 if the product doesn't exist.
     */
    public int getQuantity(String product) {
        ProductEvent pe = getProductEvent(product);
        return (pe != null) ? pe.getTotalQuantity() : 0;
    }

    /**
     * Gets the total monetary value for a specific product.
     *
     * @param product The product name.
     * @return The total value, or 0.0 if the product doesn't exist.
     */
    public double getTotalValue(String product) {
        ProductEvent pe = getProductEvent(product);
        return (pe != null) ? pe.getTotalVolume() : 0.0;
    }

    /**
     * Gets the maximum price recorded for a specific product.
     *
     * @param product The product name.
     * @return The max price, or -1.0 if the product doesn't exist.
     */
    public double getMaxPrice(String product) {
        ProductEvent pe = getProductEvent(product);
        return (pe != null) ? pe.getMaxPrice() : -1.0;
    }

    /**
     * Helper method to retrieve or create a ProductEvent.
     * Uses the class lock to ensure thread safety during map modification.
     *
     * @param product The product name.
     * @return The existing or newly created ProductEvent.
     */
    private ProductEvent getOrCreateProductEvent(String product) {
        lock.lock();
        try {
            ProductEvent pe = events.get(product);
            if (pe != null) {
                return pe;
            }

            pe = new ProductEvent();
            events.put(product, pe);
            return pe;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Helper method to retrieve a ProductEvent without creating it.
     * Thread-safe.
     *
     * @param product The product name.
     * @return The ProductEvent or null if not found.
     */
    private ProductEvent getProductEvent(String product) {
        lock.lock();
        try { return events.get(product); }
        finally { lock.unlock(); }
    }

    /**
     * Serializes the entire TimeSeries object.
     * Writes the map size, all product entries, the date, and the current day flag.
     *
     * @param out The output stream.
     * @throws IOException If an I/O error occurs.
     */
    protected void serialize(DataOutputStream out) throws IOException {
        lock.lock();
        try {
            out.writeInt(events.size());
            for (Map.Entry<String, ProductEvent> entry : events.entrySet()) {
                out.writeUTF(entry.getKey());
                entry.getValue().serialize(out);
            }

            out.writeLong(date.toEpochDay());
            out.writeBoolean(isCurrentDay);
        } finally {
            lock.unlock();
        }
    }

    // Point 4 - Person D (Nelson) - Kept as requested
    /**
     * Serializes only events matching the provided product filter using a compacted format.
     * Creates a dictionary of products to replace string names with short IDs in the output.
     *
     * @param productFilter Set of product names to include.
     * @param out           Output stream.
     * @throws IOException If an I/O error occurs.
     */
    public void serializeFilteredEventsCompacted(Set<String> productFilter, DataOutputStream out) throws IOException {
        lock.lock();
        try {
            List<SalesEvent> filteredEvents = new ArrayList<>();

            for (Map.Entry<String, ProductEvent> entry : events.entrySet()) {
                String productName = entry.getKey();
                if (productFilter.contains(productName)) {
                    filteredEvents.addAll(entry.getValue().getEvent());
                }
            }

            Map<String, Integer> productDictionary = new HashMap<>();
            List<String> uniqueProducts = new ArrayList<>();

            for (SalesEvent event : filteredEvents) {
                String productName = event.getName();
                if (!productDictionary.containsKey(productName)) {
                    productDictionary.put(productName, uniqueProducts.size());
                    uniqueProducts.add(productName);
                }
            }

            out.writeInt(uniqueProducts.size());
            for (String product : uniqueProducts) {
                out.writeUTF(product);
            }

            out.writeInt(filteredEvents.size());
            for (SalesEvent event : filteredEvents) {
                int productId = productDictionary.get(event.getName());
                out.writeShort(productId);
                event.serialize(out, true);
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Deserializes a TimeSeries object from a stream.
     *
     * @param in The input stream.
     * @return The reconstructed TimeSeries.
     * @throws IOException If an I/O error occurs.
     */
    protected static TimeSeries deserialize(DataInputStream in) throws IOException {
        int numProducts = in.readInt();
        Map<String, ProductEvent> eventsMap = new HashMap<>();

        for (int i = 0; i < numProducts; i++) {
            String product_name = in.readUTF();
            ProductEvent productEvent = ProductEvent.deserialize(in);
            eventsMap.put(product_name, productEvent);
        }

        LocalDate date = LocalDate.ofEpochDay(in.readLong());
        boolean isCurrentDay = in.readBoolean();

        TimeSeries timeSeries = new TimeSeries(date, isCurrentDay);
        timeSeries.events.putAll(eventsMap);

        return timeSeries;
    }

    /**
     * Deserializes the compacted event list format created by serializeFilteredEventsCompacted.
     *
     * @param in The input stream.
     * @return A list of SalesEvents.
     * @throws IOException If an I/O error occurs or if the product ID is invalid.
     */
    public static List<SalesEvent> deserializeAllEventsCompacted(DataInputStream in) throws IOException {
        int numProducts = in.readInt();
        String[] productDictionary = new String[numProducts];
        for (int i = 0; i < numProducts; i++) {
            productDictionary[i] = in.readUTF();
        }

        int totalEvents = in.readInt();
        List<SalesEvent> events = new ArrayList<>(totalEvents);

        for (int i = 0; i < totalEvents; i++) {
            short productId = in.readShort();
            if (productId < 0 || productId >= productDictionary.length) {
                throw new IOException("Invalid product ID: " + productId);
            }
            String productName = productDictionary[productId];
            SalesEvent event = SalesEvent.deserialize(in, productName, true);
            events.add(event);
        }

        return events;
    }
}