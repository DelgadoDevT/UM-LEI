package sd.series;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe container that aggregates all sales events for a specific product.
 * It uses a ReentrantReadWriteLock to allow multiple concurrent readers (calculating stats)
 * while ensuring exclusive access for writers (adding new events).
 */
public class ProductEvent {
    private final List<SalesEvent> events;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    /**
     * Initializes an empty ProductEvent container with fair locking mechanisms.
     */
    public ProductEvent() {
        this.events = new ArrayList<>();
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
    }

    /**
     * Adds a new sales event to the list in a thread-safe manner.
     * Acquires the write lock to ensure exclusivity.
     *
     * @param event The sales event to add.
     */
    public void addEvent(SalesEvent event) {
        writeLock.lock();
        try {
            events.add(event);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Retrieves a defensive copy of the list of events.
     * Acquires the read lock to allow concurrent access.
     *
     * @return A new ArrayList containing all sales events.
     */
    public List<SalesEvent> getEvent() {
        readLock.lock();
        try {
            return new ArrayList<>(events);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if there are any events recorded for this product.
     * Thread-safe (Read Lock).
     *
     * @return True if the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        readLock.lock();
        try {
            return events.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Calculates the total quantity of items sold for this product.
     * Iterates through all events under a read lock.
     *
     * @return The sum of quantities.
     */
    public int getTotalQuantity() {
        readLock.lock();
        try {
            int sum = 0;
            for (SalesEvent e : events) {
                sum += e.getQuantity();
            }
            return sum;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Calculates the total monetary volume accumulated by this product.
     * Iterates through all events under a read lock.
     *
     * @return The sum of total values of all events.
     */
    public double getTotalVolume() {
        readLock.lock();
        try {
            double sum = 0;
            for (SalesEvent e : events) {
                sum += e.getTotalValue();
            }
            return sum;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Finds the maximum price per unit recorded for this product.
     * Thread-safe (Read Lock).
     *
     * @return The maximum price found, or -1.0 if no events exist.
     */
    public double getMaxPrice() {
        readLock.lock();
        try {
            double max = -1.0;
            for (SalesEvent e : events) {
                if (e.getPrice() > max) {
                    max = e.getPrice();
                }
            }
            return max;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Serializes all events associated with this product.
     *
     * @param out The output stream.
     * @throws IOException If an I/O error occurs.
     */
    protected void serialize(DataOutputStream out) throws IOException {
        readLock.lock();
        try {
            out.writeInt(events.size());
            for (SalesEvent se : events) {
                se.serialize(out, false);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Deserializes a ProductEvent and its contained sales events.
     *
     * @param in The input stream.
     * @return A populated ProductEvent object.
     * @throws IOException If an I/O error occurs.
     */
    protected static ProductEvent deserialize(DataInputStream in) throws IOException {
        ProductEvent productEvent = new ProductEvent();
        int num_events = in.readInt();

        for (int i = 0; i < num_events; i++) {
            SalesEvent event = SalesEvent.deserialize(in, null, false);
            productEvent.addEvent(event);
        }

        return productEvent;
    }
}