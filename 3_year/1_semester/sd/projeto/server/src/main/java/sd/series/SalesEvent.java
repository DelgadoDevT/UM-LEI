package sd.series;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single atomic sales transaction.
 * This class is immutable and holds details such as the product name, quantity, price, and timestamp.
 * It includes custom binary serialization logic for efficient storage and network transfer.
 */
public class SalesEvent {
    private final String name;
    private final int quantity;
    private final double price;
    private final LocalDateTime date;

    /**
     * Constructs a new SalesEvent.
     *
     * @param name     The name of the product sold.
     * @param quantity The number of units sold.
     * @param price    The price per unit.
     * @param date     The timestamp of the sale.
     */
    public SalesEvent(String name, int quantity, double price, LocalDateTime date) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    /**
     * Gets the name of the product.
     *
     * @return The product name.
     */
    public String getName() { return this.name; }

    /**
     * Gets the quantity sold.
     *
     * @return The quantity.
     */
    public int getQuantity() { return this.quantity; }

    /**
     * Gets the price per unit.
     *
     * @return The price.
     */
    public double getPrice() { return this.price; }

    /**
     * Gets the timestamp of the sale.
     *
     * @return The date and time of the event.
     */
    public LocalDateTime getDate() { return this.date; }

    /**
     * Calculates the total monetary value of this specific transaction (price * quantity).
     *
     * @return The total value as a double.
     */
    public double getTotalValue() {
        return price * quantity;
    }

    /**
     * Serializes the event data into a DataOutputStream.
     *
     * @param out       The output stream to write to.
     * @param compacted If true, the product name is omitted (assumed to be known by context) to save space.
     * @throws IOException If an I/O error occurs.
     */
    protected void serialize(DataOutputStream out, boolean compacted) throws IOException {
        if (!compacted) {
            out.writeUTF(name);
        }
        out.writeInt(quantity);
        out.writeDouble(price);
        out.writeLong(date.toEpochSecond(ZoneOffset.UTC));
        out.writeInt(date.getNano());
    }

    /**
     * Deserializes a SalesEvent from a DataInputStream.
     *
     * @param in          The input stream to read from.
     * @param productName The name of the product (used only if compacted is true).
     * @param compacted   If true, the product name is not read from the stream but taken from the argument.
     * @return The reconstructed SalesEvent.
     * @throws IOException If an I/O error occurs.
     */
    protected static SalesEvent deserialize(DataInputStream in, String productName, boolean compacted) throws IOException {
        String name = compacted ? productName : in.readUTF();
        int quantity = in.readInt();
        double price = in.readDouble();
        long epochSecond = in.readLong();
        int nano = in.readInt();

        LocalDateTime date = LocalDateTime.ofEpochSecond(epochSecond, nano, ZoneOffset.UTC);
        return new SalesEvent(name, quantity, price, date);
    }
}