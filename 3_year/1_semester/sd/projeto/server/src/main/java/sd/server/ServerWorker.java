package sd.server;

import sd.common.Tag;
import sd.middleware.TaggedConnection;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * The ServerWorker class is responsible for handling client-server interactions.
 * It establishes a network connection with a client and manages operations such as user authentication,
 * event management, notification handling, and more.
 *
 * Fields:
 * - socket: Represents the client socket used for network communication.
 * - userManager: Facilitates user authentication and registration.
 * - serverState: Tracks the state and data of the server.
 * - notificationManager: Handles notifications related to server events.
 *
 * This class is a Runnable, allowing instances to be executed on separate threads to handle
 * concurrent client interactions.
 */
public class ServerWorker implements Runnable {

    /**
     * Represents the socket connection associated with this worker.
     * This socket is used for communication between the server and the connected client.
     *
     * The socket is passed to the ServerWorker upon initialization and serves as
     * the primary interface for network I/O with the client.
     *
     * The socket is expected to be managed in the context of the ServerWorker's
     * execution lifecycle to handle client requests and send responses.
     *
     * It is a final variable to ensure that the associated connection remains
     * immutable during the lifecycle of the worker.
     */
    private final Socket socket;

    /**
     * Manages user-related authentication and registration operations for the server.
     * This instance provides mechanisms to securely manage user data, ensuring thread-safety
     * through read-write locks and persistence of user information on disk.
     *
     * The UserManager is initialized and maintained throughout the server's lifecycle.
     * It supports functionalities such as
     * - Registering new users.
     * - Authenticating existing users.
     * - Persisting and loading user data.
     *
     * Thread safety: This field is immutable and shared across multiple threads, leveraging
     * internal synchronization mechanisms provided by the UserManager class to ensure
     * consistent access to user data.
     */
    private final UserManager userManager;

    /**
     * Represents the server's global state, shared across multiple threads.
     * Provides access to server-level operations, global time series, and
     * aggregation caching mechanisms. It is essential for managing concurrency
     * when reading from or writing to the server state.
     *
     * This variable is immutable and ensures thread safety through its design.
     */
    private final ServerState serverState;

    /**
     * A thread-safe instance of {@link NotificationManager} used for managing notifications
     * related to sales events. It provides functionality for notifying and blocking threads
     * based on simultaneous sales of specific products or consecutive sales of a single product.
     *
     * This variable is initialized in the constructor of the containing {@code ServerWorker} class
     * and facilitates communication regarding sales conditions and events.
     *
     * @see NotificationManager
     * @see ServerWorker
     */
    private final NotificationManager notificationManager;

    /**
     * Constructs a ServerWorker instance to handle client-server interactions.
     *
     * @param socket the client socket used for network communication
     * @param userManager the user management service responsible for handling user authentication and registration
     * @param serverState the server state manager tracking the state and data of the server
     * @param notificationManager the notification manager for handling notifications related to server events
     */
    public ServerWorker(Socket socket, UserManager userManager, ServerState serverState, NotificationManager notificationManager) {
        this.socket = socket;
        this.userManager = userManager;
        this.serverState = serverState;
        this.notificationManager = notificationManager;
    }

    /**
     * Handles client requests and interactions with the server.
     * This method is responsible for continuously processing client commands
     * over a network connection, performing operations based on the tag of each
     * received frame, and sending appropriate responses back to the client.
     * <ul>
     *     <li>Supports user authentication and registration.</li>
     *     <li>Handles operations such as adding events, aggregating data,
     *         simulating sales, managing notifications, and more.</li>
     *     <li>Performs necessary verifications to ensure actions are authorized
     *         and valid, such as checking user authentication status.</li>
     *     <li>Logs any disconnect events or exceptions encountered during the
     *         interaction.</li>
     * </ul>
     * The method uses a {@link TaggedConnection}, which provides a mechanism
     * for handling framed data with accompanying tags that specify the operation
     * to be performed.
     *
     * Note:
     * - Persistent loops keep processing client messages until a disconnection
     *   occurs or an error is encountered.
     * - Proper serialization of data and exception handling mechanisms are
     *   implemented to ensure robust communication and error reporting.
     *
     * Throws:
     * - IOException if an I/O error occurs during data transmission or reception.
     */
    @Override
    public void run() {
        boolean isAuthenticated = false;
        String currentUsername = null;

        try (TaggedConnection conn = new TaggedConnection(socket)) {
            while (true) {
                TaggedConnection.Frame frame = conn.receive();

                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(frame.data))) {

                    byte[] responseData;

                    switch (frame.tag) {
                        case Tag.REGISTER:
                            String regUser = dis.readUTF();
                            String regPass = dis.readUTF();
                            boolean regSuccess = userManager.register(regUser, regPass);
                            conn.send(Tag.REGISTER, serializeBoolean(regSuccess));
                            break;

                        case Tag.LOGIN:
                            String logUser = dis.readUTF();
                            String logPass = dis.readUTF();
                            boolean success = userManager.authenticate(logUser, logPass);
                            if (success) {
                                isAuthenticated = true;
                                currentUsername = logUser;
                            }
                            conn.send(Tag.LOGIN, serializeBoolean(success));
                            break;

                        case Tag.ADD_EVENT:
                            if (!isAuthenticated) {
                                conn.send(Tag.ADD_EVENT, serializeString("Erro: Não autenticado"));
                                break;
                            }
                            String prod = dis.readUTF();
                            int qtd = dis.readInt();
                            double price = dis.readDouble();
                            serverState.addEvent(prod, qtd, price);
                            conn.send(Tag.ADD_EVENT, serializeString("Evento registado"));
                            break;

                        case Tag.AG_QUANTITY:
                        case Tag.AG_VOLUME:
                        case Tag.AG_AVG:
                        case Tag.AG_MAX:
                            if (!isAuthenticated) {
                                conn.send(frame.tag, serializeDouble(-1));
                                break;
                            }
                            String p = dis.readUTF();
                            int d = dis.readInt();

                            int type = 0;
                            if (frame.tag == Tag.AG_QUANTITY) type = 1;
                            else if (frame.tag == Tag.AG_VOLUME) type = 2;
                            else if (frame.tag == Tag.AG_AVG) type = 3;
                            else if (frame.tag == Tag.AG_MAX) type = 4;

                            double res = serverState.getAggregation(type, p, d);
                            conn.send(frame.tag, serializeDouble(res));
                            break;

                        case Tag.SIMUL_SALES:
                            if (!isAuthenticated) {
                                conn.send(Tag.SIMUL_SALES, serializeBoolean(false));
                                break;
                            }
                            String p1 = dis.readUTF();
                            String p2 = dis.readUTF();
                            try {
                                boolean resSimul = notificationManager.waitForSimultaneous(p1, p2);
                                conn.send(Tag.SIMUL_SALES, serializeBoolean(resSimul));
                            } catch (InterruptedException e) {
                                conn.send(Tag.SIMUL_SALES, serializeBoolean(false));
                            }
                            break;

                        case Tag.CONSEC_SALES:
                            if (!isAuthenticated) {
                                conn.send(Tag.CONSEC_SALES, serializeString("Erro"));
                                break;
                            }
                            String pc = dis.readUTF();
                            int n = dis.readInt();
                            try {
                                String resConsec = notificationManager.waitForConsecutive(pc, n);
                                conn.send(Tag.CONSEC_SALES, serializeString(resConsec == null ? "null" : resConsec));
                            } catch (InterruptedException e) {
                                conn.send(Tag.CONSEC_SALES, serializeString("null"));
                            }
                            break;

                        case Tag.NEW_DAY:
                            if (!isAuthenticated) {
                                conn.send(Tag.NEW_DAY, serializeString("Erro: Não autenticado"));
                                break;
                            }
                            serverState.startNewDay();
                            conn.send(Tag.NEW_DAY, serializeString("Novo dia iniciado com sucesso."));
                            break;

                        case Tag.FILTER_EVENTS:
                            if (!isAuthenticated) {
                                conn.send(Tag.FILTER_EVENTS, new byte[0]);
                                break;
                            }

                            int daysBack = dis.readInt();
                            int numProducts = dis.readInt();
                            Set<String> products = new HashSet<>();

                            for (int i = 0; i < numProducts; i++) {
                                products.add(dis.readUTF());
                            }

                            try {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(baos);

                                serverState.serializeFilteredEvents(serverState.getCurrentDate().minusDays(daysBack), products, dos);
                                dos.close();

                                conn.send(Tag.FILTER_EVENTS, baos.toByteArray());
                            } catch (Exception e) {
                                conn.send(Tag.FILTER_EVENTS, new byte[0]);
                            }
                            break;

                        default:
                            conn.send(Tag.NEW_DAY, serializeString("Comando desconhecido: " + frame.tag));
                            break;
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente " + (currentUsername != null ? currentUsername : "anónimo") + " desconectado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Serializes a boolean value into a byte array.
     *
     * @param b the boolean value to be serialized
     * @return a byte array representation of the boolean value
     * @throws IOException if an I/O error occurs during the serialization process
     */
    private byte[] serializeBoolean(boolean b) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeBoolean(b);
        }
        return baos.toByteArray();
    }

    /**
     * Serializes the given string into a byte array using UTF-8 encoding.
     *
     * @param s the string to be serialized
     * @return the serialized byte array representation of the string
     * @throws IOException if an I/O error occurs during serialization
     */
    private byte[] serializeString(String s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(s);
        }
        return baos.toByteArray();
    }

    /**
     * Serializes a double value into a sequence of bytes.
     *
     * @param d the double value to serialize
     * @return a byte array containing the serialized representation of the double
     * @throws IOException if an I/O error occurs during serialization
     */
    private byte[] serializeDouble(double d) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeDouble(d);
        }
        return baos.toByteArray();
    }
}