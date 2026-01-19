package sd.server;

import sd.series.SeriesFileManager;
import sd.series.TimeSeries;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implements an LRU (Least Recently Used) cache strategy for TimeSeries objects using LinkedHashMap.
 * It manages memory usage by keeping frequently accessed days in memory and evicting older ones.
 * Crucially, it handles data persistence: when a modified series is evicted from the cache,
 * it is automatically saved to disk via the SeriesFileManager.
 */
public class ServerCache {
    private final int maxSize;
    private final int aggrLimit;
    private final SeriesFileManager fileManager;
    private final Set<LocalDate> modifiedSeries;

    private final LinkedHashMap<LocalDate, TimeSeries> seriesCache;
    private final LinkedHashMap<String, Double> aggregationCache;

    private final ReentrantReadWriteLock rwLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    /**
     * Initializes the server cache.
     *
     * @param maxSize     The maximum number of TimeSeries objects to keep in memory.
     * @param fileManager The file manager used for persisting evicted or saved series.
     */
    public ServerCache(int maxSize, SeriesFileManager fileManager) {
        this.maxSize = maxSize;
        this.aggrLimit = maxSize * 100;
        this.fileManager = fileManager;
        this.modifiedSeries = new HashSet<>();

        // accessOrder = true is fundamental for LRU to work
        this.seriesCache = new LinkedHashMap<LocalDate, TimeSeries>(this.maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<LocalDate, TimeSeries> eldest) {
                if (size() > maxSize) {
                    try {
                        // If the element being removed has unsaved changes, write to disk.
                        // Thanks to the 'trick' in markModified, the 'eldest' will almost never be the current day.
                        if (modifiedSeries.contains(eldest.getKey())) {
                            fileManager.saveTimeSeries(eldest.getValue(), eldest.getKey());
                            modifiedSeries.remove(eldest.getKey());
                        }
                        return true;
                    } catch (IOException e) {
                        System.err.println("ERRO CRÍTICO: Não foi possível persistir série " + eldest.getKey() + ": " + e.getMessage());
                        System.err.println("⚠️  Série mantida em cache para prevenir perda de dados.");
                        return false;
                    }
                }
                return false;
            }
        };

        this.aggregationCache = new LinkedHashMap<String, Double>(aggrLimit, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Double> eldest) {
                return size() > aggrLimit;
            }
        };

        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = this.rwLock.readLock();
        this.writeLock = this.rwLock.writeLock();
    }

    /**
     * Retrieves a TimeSeries from the cache.
     * If missing from the cache, attempts to load it from disk using the FileManager.
     * Acquires a write lock because LinkedHashMap with accessOrder=true modifies its internal
     * structure on retrieval, which is not thread-safe.
     *
     * @param date The date of the series to retrieve.
     * @return The TimeSeries object, or null if it cannot be found or loaded.
     * @throws IOException If an error occurs loading from disk.
     */
    public TimeSeries getSeries(LocalDate date) throws IOException {
        // IMPORTANT: With accessOrder=true, get() alters the map structure.
        // Therefore, we are forced to use WriteLock, otherwise memory corruption occurs under concurrency.
        writeLock.lock();
        try {
            TimeSeries series = seriesCache.get(date);
            if (series != null) {
                return series;
            }

            series = fileManager.loadTimeSeries(date);
            if (series != null) {
                seriesCache.put(date, series);
            }
            return series;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Puts a TimeSeries into the cache and immediately persists it to disk.
     * Used when a series is newly created or fully overwritten.
     *
     * @param date   The date associated with the series.
     * @param series The TimeSeries object.
     * @throws IOException If an error occurs saving to disk.
     */
    public void putSeries(LocalDate date, TimeSeries series) throws IOException {
        writeLock.lock();
        try {
            fileManager.saveTimeSeries(series, date);
            seriesCache.put(date, series);
            modifiedSeries.remove(date);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Marks a specific date's series as modified in memory.
     * This ensures it will be saved to disk if it is later evicted from the cache.
     * Also refreshes the entry's position in the LRU cache (moves to newest) by accessing it.
     *
     * @param date The date to mark as modified.
     */
    public void markModified(LocalDate date) {
        writeLock.lock();
        try {
            modifiedSeries.add(date);
            // When marking as modified, it also becomes the most recent (start of the LinkedHashMap)
            seriesCache.get(date);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Retrieves a cached aggregation value.
     * Thread-safe (Read Lock).
     *
     * @param key The aggregation key.
     * @return The cached value, or null if not present.
     */
    public Double getAggregation(String key) {
        readLock.lock();
        try {
            return aggregationCache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Caches an aggregation value.
     * Thread-safe (Write Lock).
     *
     * @param key   The aggregation key.
     * @param value The value to cache.
     */
    public void putAggregation(String key, double value) {
        writeLock.lock();
        try {
            aggregationCache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clears the cache.
     * Before clearing, iterates through all modified series and forces them to be saved to disk.
     * Typically used during server shutdown or reset.
     *
     * @throws IOException If an error occurs saving modified series.
     */
    public void clear() throws IOException {
        writeLock.lock();
        try {
            for (LocalDate date : modifiedSeries) {
                TimeSeries series = seriesCache.get(date);
                if (series != null) {
                    fileManager.saveTimeSeries(series, date);
                }
            }

            seriesCache.clear();
            aggregationCache.clear();
            modifiedSeries.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clears only the aggregation cache.
     * Useful when data changes invalidating previous aggregations.
     */
    public void clearAggregations() {
        writeLock.lock();
        try {
            aggregationCache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the current number of series in the cache.
     *
     * @return The cache size.
     */
    public int size() {
        readLock.lock();
        try {
            return seriesCache.size();
        } finally {
            readLock.unlock();
        }
    }
}