package dss.cadeiaRestaurantesLN;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.LinhaDePedido;
import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Pagamento;
import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Tipo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Main business logic interface for the Restaurant Chain Management System.
 * Provides operations for managing restaurants, employees, orders, and performance indicators.
 */
public interface ICadeiaRestaurantesLN {
    /**
     * Sends a message to specific positions in selected restaurants.
     * @param idFuncionario ID of the employee sending the message
     * @param mensagem Message content
     * @param postos List of position types to receive the message
     * @param idRestaurantes List of restaurant IDs to send the message to
     */
    void enviarMensagemParaRestaurantes(Integer idFuncionario, String mensagem, List<String> postos, List<Integer> idRestaurantes);

    /**
     * Retrieves performance indicators for restaurants within a date range.
     * @param dataInicio Start date for the report
     * @param dataFim End date for the report
     * @param idFuncionario ID of the employee requesting the report
     * @param restaurantesId List of restaurant IDs to include in the report
     * @return List of formatted performance indicator strings
     */
    List<String> consultarIndicadoresDesempenho(LocalDate dataInicio, LocalDate dataFim, Integer idFuncionario, List<Integer> restaurantesId);

    /**
     * Checks if an employee exists in the system.
     * @param idFuncionario Employee ID to check
     * @return true if employee exists, false otherwise
     */
    boolean existeFuncionario(Integer idFuncionario);

    /**
     * Checks if an employee has a specific role.
     * @param idFuncionario Employee ID to check
     * @param role Role name to verify
     * @return true if employee has the specified role, false otherwise
     */
    boolean isFuncionarioRole(Integer idFuncionario, String role);

    /**
     * Checks if an employee is a general manager.
     * @param idFuncionario Employee ID to check
     * @return true if employee is a general manager, false otherwise
     */
    boolean isGestorGeral(Integer idFuncionario);

    /**
     * Gets the names of restaurants accessible to an employee.
     * @param idFuncionario Employee ID
     * @return List of restaurant names
     */
    List<String> getNomesRestaurantes(Integer idFuncionario);

    /**
     * Gets valid position types that a manager can send messages to.
     * @param idFuncionario Manager's employee ID
     * @return Set of valid position type names
     */
    Set<String> getTiposPostoValidosParaGestor(Integer idFuncionario);

    /**
     * Retrieves messages for an employee's position.
     * @param idFuncionario Employee ID
     * @return List of messages for the employee's position
     */
    List<String> getMensagensPosto(Integer idFuncionario);

    /**
     * Gets an employee's profile information.
     * @param idFuncionario Employee ID
     * @return List of profile information strings
     */
    List<String> getPerfilFuncionario(Integer idFuncionario);

    /**
     * Retrieves the kitchen queue for a cook's restaurant.
     * @param idFuncionario Cook's employee ID
     * @return List of orders in the kitchen queue
     */
    List<String> consultarFilaCozinha(Integer idFuncionario);

    /**
     * Initiates preparation of an order.
     * @param idPedido Order ID to start preparing
     */
    void iniciarPreparacaoPedido(Integer idPedido);

    /**
     * Marks an order as ready/completed.
     * @param idPedido Order ID to mark as ready
     */
    void concluirPreparacaoPedido(Integer idPedido);

    /**
     * Cancels an order with a reason.
     * @param idPedido Order ID to cancel
     * @param motivo Reason for cancellation
     */
    void cancelarPedido(Integer idPedido, String motivo);

    /**
     * Gets orders ready for delivery at an attendant's restaurant.
     * @param idFuncionario Attendant's employee ID
     * @return List of orders ready for delivery
     */
    List<String> consultarPedidosParaEntrega(Integer idFuncionario);

    /**
     * Registers the delivery of an order to a customer.
     * @param idPedido Order ID to mark as delivered
     */
    void registarEntregaPedido(Integer idPedido);

    /**
     * Creates a new order in the system.
     * @param idRestaurante Restaurant ID where the order is placed
     * @param linhas List of order lines containing items and quantities
     * @param tipoConsumo Consumption type (e.g., "Dine-in", "Takeaway")
     * @param pagamento Payment information
     */
    void criarPedido(int idRestaurante, List<LinhaDePedido> linhas, Tipo tipoConsumo, Pagamento pagamento);
}