package sd.middleware;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A TaggedConnection represents a communication channel that sends and receives data
 * encapsulated in tagged frames. Each frame consists of a numerical tag and a data payload.
 * This class ensures thread-safe operations for sending and receiving data, enabling
 * structured and synchronized communication over a socket.
 *
 * Instances of this class are designed to work with Java's {@code AutoCloseable} interface,
 * meaning resources such as the underlying socket will be properly released when the
 * connection is closed.
 */
public class TaggedConnection implements AutoCloseable {

    /**
     * The underlying Socket used to establish the connection with the remote endpoint.
     * This socket provides the input and output streams necessary for sending and
     * receiving data, and it facilitates the communication between the local application
     * and a remote system.
     *
     * It is initialized during the construction of the {@code TaggedConnection} instance
     * and used internally to manage the data exchange process. Proper synchronization
     * and locking mechanisms are applied to ensure thread safety during send and
     * receive operations.
     *
     * The socket is closed when the {@code close()} method is called, ensuring that
     * all resources are properly released.
     */
    private final Socket s;

    /**
     * A {@link DataOutputStream} instance used for writing tagged data frames to the
     * underlying socket's output stream. This stream facilitates sending data with
     * a specific tag and size, ensuring that communication is structured and
     * synchronized.
     *
     * The {@code os} is wrapped in a {@link BufferedOutputStream} to optimize
     * writing operations, and its access is guarded using a {@link ReentrantLock}
     * to ensure thread-safe usage during data transmission.
     */
    private final DataOutputStream os;

    /**
     * A {@link DataInputStream} used to read binary data from the socket's input stream.
     * This stream is a part of the communication mechanism in the {@code TaggedConnection} class.
     * It reads inbound frames, which consist of a tag and corresponding data payload.
     */
    private final DataInputStream is;

    /**
     * A lock used to ensure thread-safe access to the output stream when sending data.
     * This prevents multiple threads from writing to the output stream simultaneously,
     * maintaining data integrity during write operations.
     */
    private final ReentrantLock sendlock = new ReentrantLock();

    /**
     * A lock used to enforce thread-safety when receiving data through the {@code receive} method.
     * Ensures that only one thread can access the underlying input stream at a time,
     * preventing potential data corruption or synchronization issues.
     */
    private final ReentrantLock receivelock = new ReentrantLock();

    /**
     * Represents a data frame that consists of an immutable identifier (tag) and a byte array payload.
     * Frames are used to encapsulate data with an associated tag for transmission or processing.
     */
    public static class Frame {
        /**
         * Represents a unique identifier or marker associated with a specific frame.
         * This variable is immutable and can be used to categorize or distinguish frames.
         */
        public final int tag;
        /**
         * A byte array representing the data payload associated with a specific frame.
         * This array holds the raw data content and may vary in size depending on the frame's context.
         * The value is immutable once the frame is created.
         */
        public final byte[] data;

        /**
         * Constructs a new Frame with the specified tag and data.
         *
         * @param tag  A unique identifier or marker associated with the frame.
         * @param data A byte array representing the data payload for the frame.
         */
        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }

    /**
     * Initializes a tagged connection using the given socket. The socket is used to manage
     * input and output streams for communicating with a remote endpoint.
     *
     * @param s the socket to be associated with this connection
     * @throws IOException if an I/O error occurs when creating input or output streams
     */
    public TaggedConnection(Socket s) throws IOException {
        this.s = s;
        this.os = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
        this.is = new DataInputStream(new BufferedInputStream(this.s.getInputStream()));
    }

    /**
     * Sends a tagged message through the connection.
     *
     * This method sends a message by writing a numerical tag, the length of the data,
     * and the data itself to the output stream associated with the connection.
     * It ensures thread-safety by locking during the send operation.
     *
     * @param tag the numerical identifier associated with the message
     * @param data the byte array containing the data to be sent
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    public void send(int tag, byte[] data) throws IOException {
        sendlock.lock();
        try {
            os.writeInt(tag);
            os.writeInt(data.length);
            os.write(data);
            os.flush();
        } finally {
            sendlock.unlock();
        }
    }

    /**
     * Receives a tagged frame from the input stream. The method reads the tag and
     * the length of the frame's data, followed by the data itself, and returns it
     * encapsulated in a Frame object. This method ensures thread-safety by utilizing
     * a lock during the read operation.
     *
     * @return a {@code Frame} object containing the tag and the associated data
     *         received from the input stream.
     * @throws IOException if an I/O error occurs while reading from the input stream.
     */
    public Frame receive() throws IOException {
        receivelock.lock();
        try {
            int tag = is.readInt();
            int len = is.readInt();
            byte[] data = new byte[len];
            is.readFully(data);
            return new Frame(tag, data);
        } finally {
            receivelock.unlock();
        }
    }

    /**
     * Closes the TaggedConnection along with its underlying socket.
     *
     * @throws IOException if an I/O error occurs while closing the connection
     */
    public void close() throws IOException {
        s.close();
    }
}