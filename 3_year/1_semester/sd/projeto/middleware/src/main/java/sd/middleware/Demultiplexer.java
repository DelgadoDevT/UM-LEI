package sd.middleware;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Closes the underlying {@link TaggedConnection} and the associated resources
 * used by this {@code Demultiplexer}.
 *
 * This method ensures that any open resources related to the demultiplexing process
 * are cleaned up and properly released. It notifies all waiting threads that the
 * {@code Demultiplexer} is closed and prevents further operations by terminating
 * the active connection.
 *
 * @throws IOException if an I/O error occurs during the closing of the connection
 */
public class Demultiplexer implements AutoCloseable {

    /**
     * Represents a connection resource used to send and receive tagged messages.
     * This variable holds a final reference to a {@link TaggedConnection} object,
     * ensuring a consistent communication protocol over a network socket.
     * The connection facilitates the demultiplexing mechanism by
     * associating messages with unique integer tags.
     * It is shared across multiple operations for synchronized interactions
     * and is required to be properly closed to release underlying resources.
     */
    private final TaggedConnection conn;

    /**
     * A synchronization mechanism used to control concurrent access
     * to critical sections in the {@code Demultiplexer} class.
     * The lock ensures thread-safe operations when modifying shared
     * resources or processing frames.
     *
     * This lock is used throughout the demultiplexing process, including
     * handling tagged queues, receiving, and notifying threads awaiting data.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * A thread-safe map that associates integer tags with {@link Entry} objects.
     *
     * The {@code buf} variable serves as a shared data structure within the {@code Demultiplexer}
     * for storing and retrieving {@code Entry} instances based on their corresponding tags.
     * Each entry maintains a queue for storing messages and a condition variable for thread
     * synchronization.
     *
     * This map provides the necessary mechanism to manage tagged messages and ensure proper
     * coordination between producer and consumer threads in a concurrent environment. It is
     * primarily accessed and modified through synchronized methods to ensure thread safety.
     */
    private final Map<Integer, Entry> buf = new HashMap<>();

    /**
     * Stores an instance of an {@link IOException} that is meant to represent
     * an error encountered during operations involving the associated
     * {@link TaggedConnection}. This variable is used to propagate exceptions
     * encountered in background threads to the main logic of the
     * {@link Demultiplexer}.
     *
     * Specifically, it is updated when an {@link IOException} occurs while
     * receiving data in the background thread of the {@link Demultiplexer}.
     * Once assigned, it signals all waiting threads and prevents further
     * operations by throwing the stored exception.
     *
     * Default value is {@code null}, indicating no errors have occurred yet.
     */
    private IOException exception = null;

    /**
     * Represents an internal structure used to manage a queue of byte arrays and a condition for synchronization.
     *
     * This class is used by the {@code Demultiplexer} to store and manage tagged frames of data.
     * Each instance of {@code Entry} is associated with a specific tag and provides:
     * - A queue for storing frames of data associated with that tag.
     * - A condition variable to facilitate thread synchronization for data retrieval.
     *
     * Instances of this class are managed internally by the {@code Demultiplexer} and are
     * accessed using a tagging mechanism implemented in the {@code get} method. The condition
     * variable is used for signaling threads waiting for data on a specific tag when new data
     * is added to the queue.
     */
    private class Entry {
        final Condition cond = lock.newCondition();
        final ArrayDeque<byte[]> queue = new ArrayDeque<>();
    }

    /**
     * Retrieves the {@code Entry} object associated with the specified tag. If no {@code Entry}
     * exists for the given tag, a new {@code Entry} is created, stored, and returned.
     * This ensures that every tag has a corresponding {@code Entry} in the buffer.
     *
     * @param tag the identifier used to locate or create the corresponding {@code Entry}
     * @return the {@code Entry} object associated with the specified tag
     */
    private Entry get(int tag) {
        Entry e = buf.get(tag);
        if (e == null) {
            e = new Entry();
            buf.put(tag, e);
        }
        return e;
    }

    /**
     * Creates a new instance of {@code Demultiplexer} with the provided {@link TaggedConnection}.
     *
     * The {@code Demultiplexer} facilitates the demultiplexing of messages received from a
     * {@link TaggedConnection}, allowing concurrent processing of tagged messages.
     *
     * @param conn The {@link TaggedConnection} instance used for sending and receiving messages.
     */
    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
    }

    /**
     * Starts a new thread that continuously listens and processes frames received
     * from the associated TaggedConnection. Each received frame is placed into a
     * queue corresponding to its tag, and threads waiting for data on that tag
     * are notified.
     *
     * The method locks a synchronization mechanism to safely modify shared data
     * structures and handles asynchronous communication.
     *
     * If an IOException occurs during the reception of a frame, it is stored,
     * and all waiting threads are notified of the exception.
     *
     * This method does not return a value and is intended to initialize the
     * demultiplexing process by starting a background thread.
     */
    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame frame = conn.receive();
                    lock.lock();
                    try {
                        Entry e = get(frame.tag);
                        e.queue.add(frame.data);
                        e.cond.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException e) {
                lock.lock();
                try {
                    exception = e;
                    buf.forEach((k, v) -> v.cond.signalAll());
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }

    /**
     * Sends a message with the specified tag and data through the underlying tagged connection.
     *
     * This method allows for sending data associated with a specific tag, ensuring that the
     * communication is properly categorized and processed by the receiver.
     *
     * @param tag the identifier used to categorize or label the message
     * @param data the byte array containing the message payload to be sent
     * @throws IOException if an I/O error occurs during the send operation
     */
    public void send(int tag, byte[] data) throws IOException {
        conn.send(tag, data);
    }

    /**
     * Receives a message associated with the specified tag.
     *
     * This method retrieves a byte array from the internal queue corresponding to the provided tag.
     * If no data is immediately available, the method blocks until data becomes available or an
     * exception is encountered. It ensures thread-safety by synchronizing access to shared resources.
     *
     * @param tag the identifier used to locate the message in the corresponding queue
     * @return the byte array containing the retrieved message associated with the specified tag
     * @throws IOException if an I/O error occurs during message retrieval
     * @throws InterruptedException if the thread is interrupted while waiting for data
     */
    public byte[] receive(int tag) throws IOException, InterruptedException {
        lock.lock();
        try {
            if (exception != null) throw exception;
            Entry e = get(tag);
            while (e.queue.isEmpty() && exception == null)
                e.cond.await();

            byte[] res = e.queue.poll();
            if (res != null) return res;
            else throw exception;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Closes the underlying TaggedConnection associated with this Demultiplexer.
     * This method ensures a proper release of resources and terminates the connection.
     *
     * @throws IOException if an I/O error occurs while closing the connection.
     */
    @Override
    public void close() throws IOException {
        conn.close();
    }
}