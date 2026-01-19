package sd.server;

import sd.series.SeriesFileManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;

/**
 * Main entry point for the distributed sales management server.
 * This class handles server initialization, configuration, and graceful shutdown.
 * It manages the server lifecycle including:
 * - Command-line argument parsing for cache size and port configuration
 * - Initialization of core components (UserManager, SeriesFileManager, ServerCache, NotificationManager)
 * - Temporal simulation restoration from persisted data
 * - Client connection acceptance and worker thread spawning
 * - Graceful shutdown with data persistence
 *
 * The server uses a multi-threaded architecture where each client connection is handled
 * by a dedicated ServerWorker thread. A shutdown hook ensures proper data persistence
 * when the server is terminated.
 *
 * Command-line usage: java ServerMain <cache_size_S> [port]
 * - cache_size_S: Maximum number of TimeSeries objects to keep in memory (required)
 * - port: Server listening port (optional, defaults to 12345)
 */
public class ServerMain {
    /**
     * Server-wide cache instance for managing TimeSeries objects with LRU eviction policy.
     * This cache is shared across all worker threads and handles automatic persistence
     * of modified series when they are evicted from memory.
     */
    private static ServerCache serverCache;

    /**
     * User authentication and registration manager.
     * Handles user credentials and provides thread-safe access to user data
     * with automatic persistence to disk.
     */
    private static UserManager userManager;

    /**
     * Main server entry point.
     * Initializes all server components and enters the main accept loop for client connections.
     *
     * The initialization process includes:
     * 1. Parsing command-line arguments (cache size and optional port)
     * 2. Creating UserManager and SeriesFileManager instances
     * 3. Determining the system date (either current date or restored from last saved date)
     * 4. Calculating historical days available (D) and validating against cache size (S)
     * 5. Initializing NotificationManager and ServerCache
     * 6. Creating ServerState with all components
     * 7. Registering a shutdown hook for graceful termination
     * 8. Starting the main accept loop to handle incoming client connections
     *
     * Each accepted client connection spawns a new ServerWorker thread to handle
     * the client's requests independently.
     *
     * @param args Command-line arguments: args[0] = cache_size_S (required), args[1] = port (optional)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java ServerMain <cache_size_S> [port]");
            System.exit(1);
        }

        int cacheSize = Integer.parseInt(args[0]);
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 12345;

        try {
            System.out.println("Servidor a iniciar na porta " + port + "...");
            System.out.println("Use Ctrl+C para encerrar");
            System.out.println("Cache size (S): " + cacheSize);
            System.out.println("Data inicial do sistema: " + LocalDate.now());

            userManager = new UserManager();
            SeriesFileManager fileManager = new SeriesFileManager("data/timeseries");

            int maxDays = fileManager.countHistoricalDays();

            LocalDate systemDate = LocalDate.now();
            LocalDate lastSavedDate = fileManager.getLastSavedDate();

            if (lastSavedDate != null && lastSavedDate.isAfter(systemDate)) {
                systemDate = lastSavedDate;
                System.out.println("Simulação temporal restaurada em: " + systemDate);
            } else {
                System.out.println("Iniciando na data atual: " + systemDate);
            }

            System.out.println("Dias históricos disponíveis (D): " + maxDays);

            if (cacheSize >= maxDays) {
                System.out.println("Aviso: S (" + cacheSize + ") >= D (" + maxDays + ")");
            }

            NotificationManager notificationManager = new NotificationManager();
            serverCache = new ServerCache(cacheSize, fileManager);
            ServerState serverState = new ServerState(notificationManager, serverCache, maxDays, systemDate);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Servidor a encerrar...");
                shutdown();
            }));

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Novo cliente conectou-se!");
                    ServerWorker worker = new ServerWorker(clientSocket, userManager, serverState, notificationManager);
                    Thread workerThread = new Thread(worker);
                    workerThread.start();
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro: cache_size_S deve ser um número inteiro");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Performs graceful shutdown of the server.
     * This method is called by the shutdown hook when the JVM is terminating.
     *
     * It ensures that:
     * 1. All cached TimeSeries are persisted to disk (via serverCache.clear())
     * 2. User data is saved to the users.dat file
     *
     * Any exceptions during shutdown are caught and logged to prevent cascading failures.
     * This method guarantees data consistency even during unexpected termination (e.g., Ctrl+C).
     */
    private static void shutdown() {
        try {
            if (serverCache != null) {
                System.out.println("Persistindo séries em cache...");
                serverCache.clear();
            }

            if (userManager != null) {
                System.out.println("Persistindo utilizadores...");
                userManager.saveUsers();
            }

            System.out.println("Encerramento concluído.");
        } catch (Exception e) {
            System.err.println("Erro durante encerramento: " + e.getMessage());
        }
    }
}