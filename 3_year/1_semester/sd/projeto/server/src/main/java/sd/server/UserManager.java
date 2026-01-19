package sd.server;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * Manages user authentication and registration for the sales management server.
 * This class provides thread-safe access to user credentials with automatic persistence to disk.
 *
 * Key features:
 * - Thread-safe operations using read-write locks for concurrent access
 * - Automatic loading of user data from disk on initialization
 * - Manual saving of user data via saveUsers() method
 * - Support for user registration with duplicate username prevention
 * - Secure password authentication
 *
 * The user data is stored in a binary format in the data/users.dat file:
 * - First integer: number of users
 * - For each user: UTF-8 encoded username, followed by UTF-8 encoded password
 *
 * Thread Safety:
 * - Read operations (authenticate) use read locks, allowing concurrent reads
 * - Write operations (register, saveUsers, loadUsers) use write locks for exclusive access
 */
public class UserManager {
    /**
     * Map storing username-password pairs.
     * Access is protected by read-write locks to ensure thread safety.
     */
    private final Map<String, String> users;

    /**
     * Read-write lock for coordinating concurrent access to user data.
     * Allows multiple concurrent readers or a single writer.
     */
    private final ReentrantReadWriteLock rwLock;

    /**
     * Read lock derived from rwLock.
     * Used for operations that only read user data (authentication).
     */
    private final Lock readLock;

    /**
     * Write lock derived from rwLock.
     * Used for operations that modify user data (registration, loading, saving).
     */
    private final Lock writeLock;

    /**
     * Path to the file where user data is persisted.
     * Typically data/users.dat in the server directory.
     */
    private final Path usersFile;

    /**
     * Initializes the UserManager and loads existing user data from disk.
     * Creates the data directory if it doesn't exist.
     * If the users.dat file exists, loads all user credentials from it.
     *
     * @throws IOException if there's an error creating the data directory or reading the users file
     */
    public UserManager() throws IOException {
        this.usersFile = Paths.get("data", "users.dat");
        Files.createDirectories(usersFile.getParent());
        this.users = new HashMap<>();
        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();

        loadUsers();
    }

    /**
     * Loads user data from the users.dat file into memory.
     * This method is called automatically during UserManager initialization.
     * If the users file doesn't exist, the method returns without error.
     *
     * File format:
     * - 4 bytes: integer representing the number of users
     * - For each user: username (UTF-8 string) + password (UTF-8 string)
     *
     * Uses write lock to ensure exclusive access during loading.
     *
     * @throws IOException if there's an error reading from the file
     */
    private void loadUsers() throws IOException {
        if (!Files.exists(usersFile)) return;

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(usersFile)))) {
            int numUsers = in.readInt();

            writeLock.lock();
            try {
                for (int i = 0; i < numUsers; i++) {
                    String user = in.readUTF();
                    String pass = in.readUTF();
                    users.put(user, pass);
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

    /**
     * Persists all user data to the users.dat file.
     * This method should be called when the server shuts down to ensure data persistence.
     *
     * File format:
     * - 4 bytes: integer representing the number of users
     * - For each user: username (UTF-8 string) + password (UTF-8 string)
     *
     * Uses read lock to allow saving while other threads are authenticating users.
     *
     * @throws IOException if there's an error writing to the file
     */
    public void saveUsers() throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(usersFile)))) {
            readLock.lock();
            try {
                out.writeInt(users.size());
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    out.writeUTF(entry.getKey());
                    out.writeUTF(entry.getValue());
                }
            } finally {
                readLock.unlock();
            }
        }
    }

    /**
     * Registers a new user with the provided username and password.
     * Prevents duplicate usernames by checking if the username already exists.
     *
     * Uses write lock to ensure exclusive access during registration.
     *
     * @param user The username for the new user
     * @param pass The password for the new user
     * @return true if registration was successful, false if the username already exists
     */
    public boolean register(String user, String pass) {
        writeLock.lock();
        try {
            if (users.containsKey(user)) {
                return false;
            }
            users.put(user, pass);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Authenticates a user by verifying the username and password.
     *
     * Uses read lock to allow concurrent authentication attempts from multiple threads.
     *
     * @param user The username to authenticate
     * @param pass The password to verify
     * @return true if the username exists and the password matches, false otherwise
     */
    public boolean authenticate(String user, String pass) {
        readLock.lock();
        try {
            String storedPass = users.get(user);
            return storedPass != null && storedPass.equals(pass);
        } finally {
            readLock.unlock();
        }
    }
}