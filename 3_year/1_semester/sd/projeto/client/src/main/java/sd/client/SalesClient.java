package sd.client;

import sd.common.Tag;
import sd.middleware.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The SalesClient class provides methods to interact with a sales tracking server.
 *
 * This class is responsible for connecting to the server, managing communication,
 * and performing operations such as user registration, authentication, and querying data
 * related to sales events. It supports retrieving and aggregating sales data, as well as
 * subscribing to specific notifications concerning sales activity.
 */
public class SalesClient {

    /**
     * Represents a private instance of the {@link Demultiplexer} class, used to handle
     * the transmission and reception of tagged messages over a {@link TaggedConnection}.
     *
     * This object is responsible for enabling asynchronous communication by managing
     * multiple queues of messages associated with unique integer tags. Message
     * routing is implemented internally to correlate responses for specific operations.
     *
     * Within the {@code SalesClient} class, this variable is used to coordinate
     * communication with a server, facilitating operations such as event registration,
     * user login, data aggregation, and notifications about specific conditions.
     */
    private Demultiplexer dm;

    public void connect(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        TaggedConnection conn = new TaggedConnection(socket);
        this.dm = new Demultiplexer(conn);
        this.dm.start();
    }

    /**
     * Closes the current connection associated with this client.
     * <br>
     * If an active connection exists, this method ensures its proper termination
     * to release associated resources.
     * <br>
     * Throws an {@link IOException} if an I/O error occurs during the process of closing the connection.
     *
     * @throws IOException if an error occurs while closing the connection.
     */
    public void close() throws IOException {
        if (dm != null) dm.close();
    }

    /**
     * Serializes data using the provided {@link IOContextWriter}.
     *
     * This method writes data to a {@link DataOutputStream} wrapped in a {@link ByteArrayOutputStream}.
     * The data is written using the logic defined in the provided {@code IOContextWriter} implementation.
     *
     * @param writer The {@link IOContextWriter} responsible for writing data to the stream.
     * @return A byte array containing the serialized data produced by the writer.
     * @throws IOException If an I/O error occurs during the serialization process.
     */
    private byte[] serialize(IOContextWriter writer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            writer.write(dos);
        }
        return baos.toByteArray();
    }

    private interface IOContextWriter {
        void write(DataOutputStream dos) throws IOException;
    }

    /**
     * Retrieves an aggregated value for a specified product over a given number of days.
     *
     * This method sends a request to the server to perform an aggregation based on the provided product
     * and time period, and waits for the server's response. The server performs the aggregation operation
     * and sends back the result, which is then returned to the caller.
     *
     * @param tag the identifier used to tag the message for communication with the server
     * @param product the name of the product for which the aggregation is to be calculated
     * @param days the number of days over which the aggregation is computed
     * @return the aggregated value calculated for the specified product and time period
     * @throws IOException if an I/O error occurs during the operation
     * @throws InterruptedException if the thread executing the operation is interrupted
     */
    private double getAggregation(int tag, String product, int days) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(product);
            dos.writeInt(days);
        });
        dm.send(tag, data);
        byte[] response = dm.receive(tag);
        return new DataInputStream(new ByteArrayInputStream(response)).readDouble();
    }

    /**
     * Registers a user with the given username and password by sending the information
     * to the server and receiving a confirmation response.
     *
     * @param username the unique username to be registered
     * @param password the associated password for the username
     * @return true if the registration is successful, false otherwise
     * @throws IOException if an I/O error occurs during sending or receiving data
     * @throws InterruptedException if the thread is interrupted while waiting for a response
     */
    public boolean register(String username, String password) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(username);
            dos.writeUTF(password);
        });
        dm.send(Tag.REGISTER, data);
        byte[] response = dm.receive(Tag.REGISTER);
        return new DataInputStream(new ByteArrayInputStream(response)).readBoolean();
    }

    /**
     * Attempts to log in using the provided username and password.
     *
     * This method serializes the user credentials, sends them to the server
     * using the LOGIN tag, and processes the server's response to determine
     * the success or failure of the login attempt.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password associated with the username.
     * @return {@code true} if the login attempt was successful, {@code false} otherwise.
     * @throws IOException If an error occurs during communication with the server.
     * @throws InterruptedException If the thread executing the method is interrupted.
     */
    public boolean login(String username, String password) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(username);
            dos.writeUTF(password);
        });
        dm.send(Tag.LOGIN, data);
        byte[] response = dm.receive(Tag.LOGIN);
        return new DataInputStream(new ByteArrayInputStream(response)).readBoolean();
    }

    /**
     * Adds a new sales event to the system by sending product details, quantity, and price to the server.
     *
     * This method serializes the specified event details into a byte array and sends it to the server
     * using the {@code ADD_EVENT} tag. It then waits for the server to process and acknowledge the event.
     *
     * @param product The name of the product for which the event is being added.
     * @param qty The quantity of the product involved in the sales event.
     * @param price The price of the product in the sales event.
     * @throws IOException If an I/O error occurs during communication with the server.
     * @throws InterruptedException If the thread is interrupted while waiting for the server response.
     */
    public void addEvent(String product, int qty, double price) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(product);
            dos.writeInt(qty);
            dos.writeDouble(price);
        });
        dm.send(Tag.ADD_EVENT, data);
        dm.receive(Tag.ADD_EVENT);
    }

    /**
     * Retrieves the total quantity of a specified product sold over a given number of days.
     *
     * @param product The name of the product for which the quantity is to be calculated.
     * @param days The number of days over which to calculate the quantity.
     * @return The total quantity of the specified product sold over the given time period.
     * @throws IOException If an I/O error occurs during the operation.
     * @throws InterruptedException If the thread executing the method is interrupted.
     */
    public double getQuantity(String product, int days) throws IOException, InterruptedException {
        return getAggregation(Tag.AG_QUANTITY, product, days);
    }

    /**
     * Retrieves the total sales volume for a given product over a specified number of days.
     *
     * @param product The name of the product for which the sales volume is to be calculated.
     * @param days The number of days over which to calculate the sales volume.
     * @return The total sales volume for the specified product and time period.
     * @throws IOException If an I/O error occurs during the operation.
     * @throws InterruptedException If the thread executing the method is interrupted.
     */
    public double getVolume(String product, int days) throws IOException, InterruptedException {
        return getAggregation(Tag.AG_VOLUME, product, days);
    }

    /**
     * Calculates the average value of a specified product's sales over a given number of days.
     *
     * @param product the name of the product for which the average is to be calculated
     * @param days the number of days over which the average is computed
     * @return the calculated average value of the product's sales over the specified number of days
     * @throws IOException if an I/O error occurs during the operation
     * @throws InterruptedException if the thread executing the operation is interrupted
     */
    public double getAverage(String product, int days) throws IOException, InterruptedException {
        return getAggregation(Tag.AG_AVG, product, days);
    }

    /**
     * Retrieves the maximum value for a specified product over a given number of days.
     *
     * @param product the name of the product for which the maximum value is to be calculated
     * @param days the number of days over which the maximum value is to be determined
     * @return the maximum value of the specified product over the given number of days
     * @throws IOException if an I/O error occurs during the aggregation process
     * @throws InterruptedException if the operation is interrupted
     */
    public double getMax(String product, int days) throws IOException, InterruptedException {
        return getAggregation(Tag.AG_MAX, product, days);
    }

    /**
     * Subscribes to notifications for simultaneous sales of two specified products.
     *
     * This method sends a subscription request to the server, specifying the two products
     * of interest. The server processes the request and responds with a boolean indicating
     * the success or failure of the subscription process.
     *
     * @param p1 The name of the first product to subscribe to for simultaneous sales notifications.
     * @param p2 The name of the second product to subscribe to for simultaneous sales notifications.
     * @return {@code true} if the server successfully processed the subscription,
     *         {@code false} otherwise.
     * @throws IOException If an I/O error occurs during communication with the server.
     * @throws InterruptedException If the thread is interrupted while waiting for the server response.
     */
    public boolean subscribeSimultaneous(String p1, String p2) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(p1);
            dos.writeUTF(p2);
        });
        dm.send(Tag.SIMUL_SALES, data);
        byte[] response = dm.receive(Tag.SIMUL_SALES);
        return new DataInputStream(new ByteArrayInputStream(response)).readBoolean();
    }

    /**
     * Subscribes to notifications for consecutive sales of a specified product
     * with a minimum number of occurrences.
     *
     * @param product the name of the product to subscribe to
     * @param n the minimum number of consecutive sales required to trigger a notification
     * @return a response message from the server indicating the subscription status
     * @throws IOException if an I/O error occurs during communication with the server
     * @throws InterruptedException if the thread is interrupted while waiting for the server response
     */
    public String subscribeConsecutive(String product, int n) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeUTF(product);
            dos.writeInt(n);
        });
        dm.send(Tag.CONSEC_SALES, data);
        byte[] response = dm.receive(Tag.CONSEC_SALES);
        return new DataInputStream(new ByteArrayInputStream(response)).readUTF();
    }

    /**
     * Initiates a new day in the system by sending a request to the server with the NEW_DAY tag.
     * Waits for the server's acknowledgment before proceeding.
     *
     * This method allows the system to transition to a new operational day, resetting or preparing
     * the necessary parts as dictated by the server.
     *
     * @throws IOException if an I/O error occurs during communication with the server.
     * @throws InterruptedException if the thread is interrupted while waiting for the server's acknowledgment.
     */
    public void startNewDay() throws IOException, InterruptedException {
        dm.send(Tag.NEW_DAY, new byte[0]);
        dm.receive(Tag.NEW_DAY);
    }

    public List<String> filterEvents(int daysBack, Set<String> products) throws IOException, InterruptedException {
        byte[] data = serialize(dos -> {
            dos.writeInt(daysBack);
            dos.writeInt(products.size());
            for (String product : products) {
                dos.writeUTF(product);
            }
        });

        dm.send(Tag.FILTER_EVENTS, data);
        byte[] response = dm.receive(Tag.FILTER_EVENTS);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(response));
        List<String> events = new ArrayList<>();

        int numProducts = dis.readInt();
        String[] productDict = new String[numProducts];

        for (int i = 0; i < numProducts; i++) {
            productDict[i] = dis.readUTF();
        }

        int totalEvents = dis.readInt();

        for (int i = 0; i < totalEvents; i++) {
            short productId = dis.readShort();
            int quantity = dis.readInt();
            double price = dis.readDouble();
            long epochSecond = dis.readLong();
            int nano = dis.readInt();

            String productName = productDict[productId];
            double totalValue = price * quantity;

            LocalDateTime date = LocalDateTime.ofEpochSecond(epochSecond, nano, ZoneOffset.UTC);
            String dayOnly = date.toLocalDate().toString();

            String formatted = String.format("%s - %dun x %.2f€ = %.2f€ (%s)",
                    productName, quantity, price, totalValue, dayOnly);

            events.add(formatted);
        }

        return events;
    }
}