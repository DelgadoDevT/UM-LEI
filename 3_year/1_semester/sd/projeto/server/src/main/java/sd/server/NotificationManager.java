package sd.server;

import sd.series.SalesEvent;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages blocking notifications for specific sales events (simultaneous and consecutive).
 * Thread-safe implementation using ReentrantLock and Condition variables.
 */
public class NotificationManager {
    private final ReentrantLock lock;

    // Sets of products sold in the current day to check for simultaneous sales
    private final Set<String> productsSoldToday;
    // Conditions for clients waiting for a specific pair of products
    private final Map<Set<String>, Condition> simulConditions;

    // State for tracking consecutive sales of a single product
    private String lastProductSold;
    private int currentConsecutiveCount;
    // Conditions for clients waiting for 'n' consecutive sales of a product
    private final Map<String, List<Condition>> consecConditions;
    private final Map<Condition, Integer> consecThresholds;

    // Epoch to handle the "End of Day" logic, ensuring waiting threads are released
    private int currentDayEpoch;

    public NotificationManager() {
        this.lock = new ReentrantLock();
        this.productsSoldToday = new HashSet<>();
        this.simulConditions = new HashMap<>();
        this.consecConditions = new HashMap<>();
        this.consecThresholds = new HashMap<>();
        this.lastProductSold = null;
        this.currentConsecutiveCount = 0;
        this.currentDayEpoch = 0;
    }

    /**
     * Registers a sale and updates the internal state for notifications.
     * Signals waiting threads if conditions (simultaneous or consecutive) are met.
     *
     * @param event The sales event that just occurred.
     */
    public void registerSale(SalesEvent event) {
        lock.lock();
        try {
            String product = event.getName();

            // 1. Handle Simultaneous Sales
            productsSoldToday.add(product);
            for (Map.Entry<Set<String>, Condition> entry : simulConditions.entrySet()) {
                Set<String> pair = entry.getKey();
                // If both products in the pair have been sold today, wake up waiting threads
                if (productsSoldToday.containsAll(pair)) {
                    entry.getValue().signalAll();
                }
            }

            // 2. Handle Consecutive Sales
            if (product.equals(lastProductSold)) {
                currentConsecutiveCount++;
            } else {
                lastProductSold = product;
                currentConsecutiveCount = 1;
            }

            if (consecConditions.containsKey(product)) {
                List<Condition> conditions = consecConditions.get(product);
                for (Condition cond : conditions) {
                    Integer n = consecThresholds.get(cond);
                    // Signal if the consecutive count reaches the client's requested threshold
                    if (n != null && currentConsecutiveCount >= n) {
                        cond.signalAll();
                    }
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Blocks until two specific products (p1 and p2) are sold in the current day.
     *
     * @param p1 Name of the first product.
     * @param p2 Name of the second product.
     * @return true if both were sold today; false if the day ended before that happened.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public boolean waitForSimultaneous(String p1, String p2) throws InterruptedException {
        lock.lock();
        try {
            int entryEpoch = this.currentDayEpoch;

            Set<String> pair = new HashSet<>();
            pair.add(p1);
            pair.add(p2);

            // Check if condition is already met
            if (productsSoldToday.containsAll(pair)) {
                return true;
            }

            // Create or get the condition for this pair
            Condition cond = simulConditions.computeIfAbsent(pair, k -> lock.newCondition());

            // Wait until products are sold OR the day changes (epoch mismatch)
            while (!productsSoldToday.containsAll(pair) && entryEpoch == currentDayEpoch) {
                cond.await();
            }

            // If the day changed while waiting, the condition failed
            if (entryEpoch != currentDayEpoch) {
                return false;
            }

            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Blocks until 'n' consecutive sales of 'product' occur.
     *
     * @param product The product name to monitor.
     * @param n The number of consecutive sales required.
     * @return The product name if successful; null if the day ended before the condition was met.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public String waitForConsecutive(String product, int n) throws InterruptedException {
        lock.lock();
        try {
            int entryEpoch = this.currentDayEpoch;

            // Check if condition is already met
            if (product.equals(lastProductSold) && currentConsecutiveCount >= n) {
                return product;
            }

            // Register a new condition for this specific request
            Condition cond = lock.newCondition();
            consecConditions.computeIfAbsent(product, k -> new ArrayList<>()).add(cond);
            consecThresholds.put(cond, n);

            // Wait until threshold is reached OR the day changes
            while (entryEpoch == currentDayEpoch && !(product.equals(lastProductSold) && currentConsecutiveCount >= n)) {
                cond.await();
            }

            // Cleanup condition registration
            consecThresholds.remove(cond);
            if (consecConditions.containsKey(product)) {
                consecConditions.get(product).remove(cond);
            }

            // If the day changed, return null
            if (entryEpoch != currentDayEpoch) {
                return null;
            }

            return product;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Resets the state for a new day and wakes up all waiting threads.
     * Waiting threads will return false/null as the day epoch has changed.
     */
    public void startNewDay() {
        lock.lock();
        try {
            this.currentDayEpoch++;

            // Wake up everyone waiting for simultaneous sales
            for (Condition c : simulConditions.values()) c.signalAll();
            // Wake up everyone waiting for consecutive sales
            for (List<Condition> list : consecConditions.values()) {
                for (Condition c : list) c.signalAll();
            }

            // Reset state
            productsSoldToday.clear();
            simulConditions.clear();

            lastProductSold = null;
            currentConsecutiveCount = 0;
            consecConditions.clear();
            consecThresholds.clear();

        } finally {
            lock.unlock();
        }
    }
}