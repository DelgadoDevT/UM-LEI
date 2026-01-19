package sd.series;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Handles the persistence layer for TimeSeries objects.
 * Responsible for reading from and writing to binary files on the disk, organized by date.
 * File pattern: "series_YYYY-MM-DD.dat".
 */
public final class SeriesFileManager {
    private final Path dataDirectory;

    /**
     * Default constructor. Uses the standard relative path for data storage.
     *
     * @throws IOException If the directory cannot be created.
     */
    public SeriesFileManager() throws IOException {
        this("../../../../data/timeseries");
    }

    /**
     * Constructor allowing a custom base path.
     * Creates the directory if it does not exist.
     *
     * @param basePath The path string to the data directory.
     * @throws IOException If the directory cannot be created.
     */
    public SeriesFileManager(String basePath) throws IOException {
        this.dataDirectory = Paths.get(basePath);
        Files.createDirectories(dataDirectory);
    }

    /**
     * Generates the file path for a specific date.
     *
     * @param date The date to generate the filename for.
     * @return The Path object representing the file location.
     */
    private Path getFilePath(LocalDate date) {
        String filename = String.format("series_%04d-%02d-%02d.dat", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return dataDirectory.resolve(filename);
    }

    /**
     * Saves a TimeSeries object to disk.
     * Uses a BufferedOutputStream for performance optimization.
     *
     * @param series The TimeSeries object to save.
     * @param date   The date associated with the series.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void saveTimeSeries(TimeSeries series, LocalDate date) throws IOException {
        Path filePath = getFilePath(date);
        // Uses BufferedOutputStream for performance
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath)))) {
            series.serialize(out);
        }
    }

    /**
     * Loads a TimeSeries object from disk if it exists.
     *
     * @param date The date to load.
     * @return The deserialized TimeSeries, or null if the file does not exist.
     * @throws IOException If an I/O error occurs during reading.
     */
    public TimeSeries loadTimeSeries(LocalDate date) throws IOException {
        Path filePath = getFilePath(date);

        if (!Files.exists(filePath)) {
            return null;
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
            return TimeSeries.deserialize(in);
        }
    }

    /**
     * Checks if a time series file exists for the given date.
     *
     * @param date The date to check.
     * @return True if the file exists, false otherwise.
     */
    public boolean existsTimeSeries(LocalDate date) {
        return Files.exists(getFilePath(date));
    }

    /**
     * Counts how many historical data files exist in the data directory.
     * Matches files against the "series_*.dat" pattern.
     *
     * @return The count of files found.
     * @throws IOException If an I/O error occurs during directory listing.
     */
    public int countHistoricalDays() throws IOException {
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDirectory, "series_*.dat")) {
            for (Path ignored : stream) {
                count++;
            }
        }
        return count;
    }

    /**
     * Scans the directory to find the most recent date saved on disk.
     * Parses filenames to determine the dates. Handles parsing errors gracefully.
     *
     * @return The latest LocalDate found, or null if no valid files exist or an error occurs.
     */
    public LocalDate getLastSavedDate() {
        LocalDate maxDate = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDirectory, "series_*.dat")) {
            for (Path entry : stream) {
                String filename = entry.getFileName().toString();
                if (filename.length() >= 17) {
                    try {
                        String dateStr = filename.substring(7, 17);
                        LocalDate date = LocalDate.parse(dateStr);
                        if (maxDate == null || date.isAfter(maxDate)) {
                            maxDate = date;
                        }
                    } catch (DateTimeParseException | IndexOutOfBoundsException e) {
                        // Ignores poorly formatted files
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return maxDate;
    }
}