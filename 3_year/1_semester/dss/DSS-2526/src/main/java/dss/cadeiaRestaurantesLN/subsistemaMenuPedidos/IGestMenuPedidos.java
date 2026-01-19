package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.List;

/**
 * Interface for managing menus and orders subsystem.
 * Handles order preparation, delivery, and catalog management.
 */
public interface IGestMenuPedidos {
    /**
     * Gets the complete catalog of items available for sale.
     * @return List of all sellable items (products and menu packs)
     */
    List<ArtigoVenda> getCatalogoVenda();

    /**
     * Retrieves the queue of orders for a specific restaurant's kitchen.
     * @param idRestaurante Restaurant ID
     * @return List of formatted order information strings
     */
    List<String> consultarFilaPedidos(int idRestaurante);

    /**
     * Initiates preparation of an order (changes state to IN_PREPARATION).
     * @param idPedido Order ID to start preparing
     */
    void iniciarPreparacaoPedido(int idPedido);

    /**
     * Marks an order as ready (changes state to READY).
     * @param idPedido Order ID to mark as ready
     */
    void concluirPreparacaoPedido(int idPedido);

    /**
     * Cancels an order with a reason.
     * @param idPedido Order ID to cancel
     * @param motivo Cancellation reason
     */
    void cancelarPedido(int idPedido, String motivo);

    /**
     * Gets all ready orders waiting for delivery at a restaurant.
     * @param idRestaurante Restaurant ID
     * @return List of formatted order information strings
     */
    List<String> consultarPedidosProntos(int idRestaurante);

    /**
     * Marks an order as delivered to the customer.
     * @param idPedido Order ID to mark as delivered
     */
    void entregarPedido(int idPedido);

    /**
     * Creates a new order in the system.
     * @param idRestaurante Restaurant ID where the order is placed
     * @param linhas List of order lines
     * @param tipoConsumo Consumption type (e.g., "Dine-in", "Takeaway")
     * @param pagamento Payment information
     */
    void criarPedido(int idRestaurante, List<LinhaDePedido> linhas, Tipo tipoConsumo, Pagamento pagamento);

    /**
     * Gets all available ingredients for product customization.
     * @return List of available ingredients
     */
    List<Ingrediente> getIngredientesDisponiveis();
}

