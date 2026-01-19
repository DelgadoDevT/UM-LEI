package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

/**
 * Interface representing sellable items in the restaurant system.
 * Implemented by products and menu packs.
 */
public interface ArtigoVenda {
    /**
     * Gets the unique identifier of the item.
     * @return Item ID
     */
    public int getId();

    /**
     * Gets the name of the item.
     * @return Item name
     */
    public String getNome();

    /**
     * Gets the price of the item.
     * @return Item price
     */
    public double getPreco();
}