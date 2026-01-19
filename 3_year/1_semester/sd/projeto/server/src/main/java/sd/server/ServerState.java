package sd.server;

import sd.series.SalesEvent;
import sd.series.TimeSeries;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages the global state of the server, including the current time series and caching for aggregations.
 * Handles concurrency using ReadWriteLock to allow multiple readers (aggregations) and single writer (addEvent).
 */
public class ServerState {
    private final ReadWriteLock rwLock;
    private final Lock readLock;
    private final Lock writeLock;

    private final NotificationManager notificationManager;
    private final ServerCache cache;

    private TimeSeries currentSeries;
    private LocalDate currentDate;
    private int maxDays;

    public ServerState(NotificationManager notificationManager, ServerCache cache, int maxDays, LocalDate initialDate) {
        this.notificationManager = notificationManager;
        this.cache = cache;
        this.maxDays = maxDays;

        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();

        this.currentDate = initialDate;

        TimeSeries loaded = null;
        try {
            loaded = cache.getSeries(currentDate);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loaded != null) {
            this.currentSeries = loaded;
            this.currentSeries.setCurrentDay(true);
        } else {
            this.currentSeries = new TimeSeries(currentDate, true);
            try {
                cache.putSeries(currentDate, currentSeries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addEvent(String name, int quantity, double price) {
        if (name == null) {
            throw new IllegalArgumentException("Argumentos inválidos");
        }

        LocalDateTime logicalDate = LocalDateTime.of(currentDate, java.time.LocalTime.now());
        SalesEvent event = new SalesEvent(name, quantity, price, logicalDate);

        writeLock.lock();
        try {
            currentSeries.addEvent(event);
            cache.markModified(currentDate);
        } finally {
            writeLock.unlock();
        }
        notificationManager.registerSale(event);
    }

    /**
     * Advances the server date to the next day.
     * Clears aggregation caches as they might depend on the definition of "previous days".
     * Triggers notification manager to unblock waiting clients.
     */
    public void startNewDay() throws IOException {
        writeLock.lock();
        try {
            currentSeries.setCurrentDay(false);
            cache.putSeries(currentDate, currentSeries);

            maxDays++;

            currentDate = currentDate.plusDays(1);

            currentSeries = new TimeSeries(currentDate, true);
            cache.putSeries(currentDate, currentSeries);

            // Invalidate cache as relative time windows (e.g., "last 3 days") shift
            cache.clearAggregations();

            notificationManager.startNewDay();
            System.out.println("Avançando dia. Novo dia: " + currentDate.toString());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Performs an aggregation operation on a product over the last 'd' days.
     * Uses a cache to store results of expensive calculations.
     *
     * @param type The type of aggregation: 1=Quantity, 2=Volume, 3=Average, 4=Max Price.
     * @param product The product name.
     * @param days The number of previous days to include (excluding current day).
     * @return The result of the aggregation.
     * @throws IOException If loading series from disk fails.
     */
    public double getAggregation(int type, String product, int days) throws IOException {
        int actualDays = Math.min(days, maxDays);

        if (actualDays == 0) {
            return 0.0;
        }

        String cacheKey = type + "_" + product + "_" + days;

        // Fast-path: Check cache first (double-check locking pattern)
        Double cachedResult = cache.getAggregation(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        double result = 0;
        int count = 0;
        LocalDate targetDate;

        readLock.lock();
        try {
            targetDate = currentDate.minusDays(1);
        } finally {
            readLock.unlock();
        }

        for (int i = 0; i < days; i++) {
            TimeSeries ts = cache.getSeries(targetDate);

            if (ts != null) {
                switch (type) {
                    case 1:
                        result += ts.getQuantity(product);
                        break;
                    case 2:
                        result += ts.getTotalValue(product);
                        break;
                    case 3:
                        int q = ts.getQuantity(product);
                        if (q > 0) {
                            result += ts.getTotalValue(product);
                            count += q;
                        }
                        break;
                    case 4:
                        double max = ts.getMaxPrice(product);
                        if (max > result) {
                            result = max;
                        }
                        break;
                }
            }
            targetDate = targetDate.minusDays(1);
        }

        if (type == 3 && count > 0) {
            result = result / count;
        }

        cache.putAggregation(cacheKey, result);
        return result;
    }

    public void serializeFilteredEvents(LocalDate date, Set<String> products, DataOutputStream out) throws IOException {
        if (date == null || products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Argumentos inválidos");
        }

        if (date.equals(currentDate) || date.isAfter(currentDate)) {
            throw new IllegalArgumentException("Só pode filtrar dias históricos");
        }

        if (date.isBefore(currentDate.minusDays(maxDays))) {
            throw new IllegalArgumentException("Data fora do histórico disponível");
        }

        TimeSeries series = cache.getSeries(date);
        if (series != null) {
            series.serializeFilteredEventsCompacted(products, out);
        } else {
            out.writeInt(0);
            out.writeInt(0);
        }
    }

    public LocalDate getCurrentDate() {
        readLock.lock();
        try {
            return currentDate;
        } finally {
            readLock.unlock();
        }
    }
}