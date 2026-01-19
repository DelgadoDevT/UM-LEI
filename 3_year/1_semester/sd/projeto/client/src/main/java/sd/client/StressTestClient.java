package sd.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for stress testing a sales tracking server with heavy data workloads.
 * Performs throughput and cache persistence benchmarks.
 */
public class StressTestClient {

    // Configuration constants for test parameters
    private static final int NUM_RUNS = 10;
    private static final int CONCURRENCY_OPS = 100_000;
    private static final int PERSISTENCE_CYCLES = 50;
    private static final int EVENTS_PER_DAY = 20_000;

    // Test user credentials
    private static final String TEST_USER = "tester";
    private static final String TEST_PASS = "123";

    // Server connection details
    private final String host;
    private final int port;

    /**
     * Constructs a stress test client with specified server connection parameters.
     *
     * @param host the server hostname or IP address
     * @param port the server port number
     */
    public StressTestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Executes the complete stress test suite including throughput and cache performance tests.
     * Generates a summary table of results after multiple test runs.
     */
    public void start() {
        System.out.println(">>> STARTING OPTIMIZED TEST SUITE (HEAVY DATA) <<<");

        seedHeavyHistory();

        List<String> results = new ArrayList<>();
        results.add(String.format("%-5s | %-15s | %-12s | %-12s | %-12s | %-10s",
                "Run", "Throughput", "Latency", "Miss(Disk)", "Hit(RAM)", "Gain"));
        results.add("-----------------------------------------------------------------------------------------");

        for (int i = 1; i <= NUM_RUNS; i++) {
            System.out.println("\n=== RUN " + i + "/" + NUM_RUNS + " ===");

            double[] throughputRes = runConcurrencyTest(i);

            try { Thread.sleep(500); } catch (InterruptedException e) {}

            double[] persistenceRes = runPersistenceTest(i);

            double gain = (persistenceRes[1] > 0) ? (persistenceRes[0] / persistenceRes[1]) : 0;

            String line = String.format("%-5d | %8.1f ops/s | %8.5f ms | %8.3f ms | %8.3f ms | %8.1fx",
                    i, throughputRes[0], throughputRes[1], persistenceRes[0], persistenceRes[1], gain);

            results.add(line);
            System.out.println("Result: " + line);

            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }

        System.out.println("\n\n>>> FINAL RESULTS TABLE <<<");
        for (String line : results) {
            System.out.println(line);
        }
    }

    /**
     * Populates the server with a large volume of historical data to create heavy disk load conditions.
     * Inserts multiple days worth of sales events to simulate real-world data volume.
     */
    private void seedHeavyHistory() {
        System.out.print("Generating HEAVY history (" + EVENTS_PER_DAY + " ev/day)... ");
        SalesClient client = new SalesClient();
        try {
            client.connect(host, port);
            prepareSession(client);

            for (int d = 0; d < 3; d++) {
                long start = System.nanoTime();

                System.out.print("[Day " + d + "] ");
                for (int i = 0; i < EVENTS_PER_DAY; i++) {
                    client.addEvent("ProdA", 1, 10.0);
                    client.addEvent("ProdB", 1, 10.0);
                    if (i % 5000 == 0) System.out.print(".");
                }
                client.startNewDay();
            }
            System.out.println(" [DONE]");
        } catch (Exception e) {
            System.out.println("\n[Warning] " + e.getMessage() + " (Assuming data exists)");
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Measures system throughput by executing a high volume of concurrent operations.
     *
     * @param runId the current test run identifier
     * @return array containing throughput (ops/s) and average latency (ms)
     */
    private double[] runConcurrencyTest(int runId) {
        System.out.print(" -> Throughput Test (100k ops)... ");
        SalesClient client = new SalesClient();
        double throughput = 0;
        double latency = 0;

        try {
            client.connect(host, port);
            prepareSession(client);

            long start = System.nanoTime();

            Thread t1 = new Thread(() -> {
                for (int i = 0; i < CONCURRENCY_OPS; i++) {
                    try { client.addEvent("ProdConc", 1, 10.0); } catch (Exception ignored) {}
                }
            });

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < CONCURRENCY_OPS; i++) {
                    try { client.getVolume("ProdConc", 5); } catch (Exception ignored) {}
                }
            });

            t1.start(); t2.start();
            t1.join(); t2.join();

            long durationNs = System.nanoTime() - start;
            double totalTimeMs = durationNs / 1_000_000.0;
            double totalOps = CONCURRENCY_OPS * 2.0;

            throughput = (totalOps * 1000.0) / totalTimeMs;
            latency = totalTimeMs / totalOps;

            System.out.printf("Done. (%.1f ops/s)\n", throughput);

        } catch (Exception e) {
            System.out.println("Fail: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
        return new double[]{throughput, latency};
    }

    /**
     * Compares disk access latency (cache miss) against RAM access latency (cache hit).
     *
     * @param runId the current test run identifier
     * @return array containing average miss latency (ms) and hit latency (ms)
     */
    private double[] runPersistenceTest(int runId) {
        System.out.print(" -> Cache Test (" + PERSISTENCE_CYCLES + " heavy cycles)... ");
        SalesClient client = new SalesClient();

        double avgMiss = 0;
        double avgHit = 0;

        try {
            client.connect(host, port);
            prepareSession(client);

            client.getVolume("ProdA", 1);
            client.getVolume("ProdB", 1);

            double totalMissNs = 0;
            double totalHitNs = 0;
            int count = 0;

            for (int i = 0; i < PERSISTENCE_CYCLES; i++) {
                try {
                    client.getVolume("ProdB", 1);

                    Thread.yield();

                    long t1 = System.nanoTime();
                    client.getVolume("ProdA", 1);
                    long t2 = System.nanoTime();

                    long t3 = System.nanoTime();
                    client.getVolume("ProdA", 1);
                    long t4 = System.nanoTime();

                    totalMissNs += (t2 - t1);
                    totalHitNs += (t4 - t3);
                    count++;
                } catch (Exception e) {
                    try {
                        client.close(); client = new SalesClient(); client.connect(host, port); prepareSession(client);
                    } catch (Exception fatal) { break; }
                }
            }

            if (count > 0) {
                avgMiss = (totalMissNs / count) / 1_000_000.0;
                avgHit = (totalHitNs / count) / 1_000_000.0;
                System.out.printf("Done. (Miss: %.3f ms | Hit: %.3f ms)\n", avgMiss, avgHit);
            }

        } catch (Exception e) {
            System.out.println("Fail: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
        return new double[]{avgMiss, avgHit};
    }

    /**
     * Establishes a session with the server by registering and logging in a test user.
     *
     * @param client the SalesClient instance to prepare
     * @throws IOException if connection or login fails
     * @throws InterruptedException if thread is interrupted during operation
     */
    private void prepareSession(SalesClient client) throws IOException, InterruptedException {
        try { client.register(TEST_USER, TEST_PASS); } catch (Exception ignored) {}
        if (!client.login(TEST_USER, TEST_PASS)) throw new IOException("Login failed");
    }

    /**
     * Entry point for the stress test client application.
     *
     * @param args command-line arguments: [host] [port] (default: localhost:12345)
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        new StressTestClient(host, port).start();
    }
}