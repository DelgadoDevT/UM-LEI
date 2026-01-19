package dss.cadeiaRestaurantesUI;

import dss.cadeiaRestaurantesLN.CadeiaRestaurantesLNFacade;
import dss.cadeiaRestaurantesLN.ICadeiaRestaurantesLN;
import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Controller for the Restaurant Chain Management System UI.
 * Manages user authentication and delegates business logic operations to the model.
 * Acts as an intermediary between the view and the business logic layer.
 */
public class CadeiaRestaurantesController {
    private final ICadeiaRestaurantesLN model;
    private Integer funcionarioAutenticadoId;

    /**
     * Constructor with business logic model.
     * @param model Business logic facade
     */
    public CadeiaRestaurantesController(ICadeiaRestaurantesLN model) {
        this.model = model;
        this.funcionarioAutenticadoId = null;
    }

    /**
     * Authenticates an employee by ID.
     * @param idFuncionario Employee ID
     * @return true if authentication successful, false otherwise
     */
    public boolean autenticarFuncionario(int idFuncionario) {
        if (model.existeFuncionario(idFuncionario)) {
            this.funcionarioAutenticadoId = idFuncionario;
            return true;
        }
        return false;
    }

    /**
     * Logs out the authenticated employee.
     */
    public void logout() {
        this.funcionarioAutenticadoId = null;
    }

    /**
     * Sends a message to restaurants.
     * @param mensagem Message content
     * @param postos List of postos
     * @param idRestaurantes List of restaurant IDs
     */
    public void enviarMensagemParaRestaurantes(String mensagem, List<String> postos, List<Integer> idRestaurantes) {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        model.enviarMensagemParaRestaurantes(funcionarioAutenticadoId, mensagem, postos, idRestaurantes);
    }

    /**
     * Consults performance indicators for the authenticated employee.
     * @param dataInicio Start date for the consultation
     * @param dataFim End date for the consultation
     * @param restaurantesId List of restaurant IDs
     * @return List of performance indicators
     */
    public List<String> consultarIndicadoresDesempenho(LocalDate dataInicio, LocalDate dataFim, List<Integer> restaurantesId) {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        return model.consultarIndicadoresDesempenho(dataInicio, dataFim, funcionarioAutenticadoId, restaurantesId);
    }

    /**
     * Checks if an employee is authenticated.
     * @return true if authenticated, false otherwise
     */
    public boolean isAutenticado() {
        return funcionarioAutenticadoId != null;
    }

    /**
     * Checks if the authenticated employee is a manager.
     * @return true if the employee is a manager, false otherwise
     */
    public boolean isGestor() {
        if (funcionarioAutenticadoId == null) return false;
        return model.isFuncionarioRole(funcionarioAutenticadoId, "GESTOR");
    }

    /**
     * Checks if the authenticated employee is a general manager.
     * @return true if the employee is a general manager, false otherwise
     */
    public boolean isGestorGeral() {
        if (funcionarioAutenticadoId == null) return false;
        return model.isGestorGeral(funcionarioAutenticadoId);
    }

    /**
     * Checks if the authenticated employee is a cook.
     * @return true if the employee is a cook, false otherwise
     */
    public boolean isCozinheiro() {
        if (funcionarioAutenticadoId == null) return false;
        return model.isFuncionarioRole(funcionarioAutenticadoId, "COZINHEIRO");
    }

    /**
     * Checks if the authenticated employee is a waiter.
     * @return true if the employee is a waiter, false otherwise
     */
    public boolean isAtendente() {
        if (funcionarioAutenticadoId == null) return false;
        return model.isFuncionarioRole(funcionarioAutenticadoId, "ATENDENTE");
    }

    /**
     * Retrieves the list of restaurants with their names for the authenticated employee.
     * @return List of restaurant names
     */
    public List<String> getRestaurantesComNomes() {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        return model.getNomesRestaurantes(funcionarioAutenticadoId);
    }

    /**
     * Retrieves the valid posto types for a manager.
     * @return Set of valid posto types
     */
    public Set<String> getTiposPostoValidosParaGestor() {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        return model.getTiposPostoValidosParaGestor(funcionarioAutenticadoId);
    }

    /**
     * Retrieves the messages for the posto of the authenticated employee.
     * @return List of posto messages
     */
    public List<String> getMensagensPosto() {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        return model.getMensagensPosto(funcionarioAutenticadoId);
    }

    /**
     * Retrieves the profile information of the authenticated employee.
     * @return List of profile information strings
     */
    public List<String> getPerfilFuncionario() {
        if (funcionarioAutenticadoId == null) {
            throw new IllegalStateException("Funcionário não autenticado");
        }
        return model.getPerfilFuncionario(funcionarioAutenticadoId);
    }

    /**
     * Retrieves the kitchen orders for the authenticated cook.
     * @return List of kitchen orders
     */
    public List<String> getPedidosCozinha() {
        if (!isCozinheiro()) {
            throw new IllegalStateException("Apenas cozinheiros podem ver a fila de cozinha.");
        }
        return model.consultarFilaCozinha(funcionarioAutenticadoId);
    }


    /**
     * Starts the preparation of an order.
     * @param idPedido Order ID
     */
    public void iniciarPreparacaoPedido(int idPedido) {
        if (!isCozinheiro()) {
            throw new IllegalStateException("Apenas cozinheiros podem atualizar pedidos.");
        }
        model.iniciarPreparacaoPedido(idPedido);
    }

    /**
     * Completes the preparation of an order.
     * @param idPedido Order ID
     */
    public void concluirPreparacaoPedido(int idPedido) {
        if (!isCozinheiro()) {
            throw new IllegalStateException("Apenas cozinheiros podem atualizar pedidos.");
        }
        model.concluirPreparacaoPedido(idPedido);
    }

    /**
     * Cancels an order.
     * @param idPedido Order ID
     * @param motivo Cancellation reason
     */
    public void cancelarPedido(int idPedido, String motivo) {
        if (!isCozinheiro()) {
            throw new IllegalStateException("Apenas cozinheiros podem cancelar pedidos.");
        }
        model.cancelarPedido(idPedido, motivo);
    }

    // UC: Entregar Pedido (Atendente)
    /**
     * Retrieves the orders pending delivery for the authenticated waiter.
     * @return List of orders pending delivery
     */
    public List<String> getPedidosParaEntrega() {
        if (!isAtendente()) {
            throw new IllegalStateException("Apenas atendentes podem ver entregas.");
        }
        return model.consultarPedidosParaEntrega(funcionarioAutenticadoId);
    }

    /**
     * Registers the delivery of an order.
     * @param idPedido Order ID
     */
    public void registarEntrega(int idPedido) {
        if (!isAtendente()) {
            throw new IllegalStateException("Apenas atendentes podem registar entregas.");
        }
        model.registarEntregaPedido(idPedido);
    }

    /**
     * Retrieves the list of public restaurants.
     * @return List of public restaurant names
     */
    public List<String> getRestaurantesPublico() {
        return ((CadeiaRestaurantesLNFacade) model).getNomesRestaurantesPublico();
    }

    /**
     * Creates a new order.
     * @param idRestaurante Restaurant ID
     * @param linhas List of order lines
     * @param tipoConsumo Consumption type
     * @param pagamento Payment information
     */
    public void criarPedido(int idRestaurante, List<LinhaDePedido> linhas, Tipo tipoConsumo, Pagamento pagamento) {
        if (linhas == null || linhas.isEmpty()) {
            throw new IllegalArgumentException("O pedido tem de ter linhas.");
        }
        model.criarPedido(idRestaurante, linhas, tipoConsumo, pagamento);
    }

    /**
     * Retrieves the sales catalog.
     * @return List of sale articles
     */
    public List<ArtigoVenda> getCatalogoVenda() {
        return ((CadeiaRestaurantesLNFacade) model).getMenuPedidosFacade().getCatalogoVenda();
    }

    /**
     * Retrieves the available ingredients.
     * @return List of available ingredients
     */
    public List<Ingrediente> getIngredientesDisponiveis() {
        return ((CadeiaRestaurantesLNFacade) model).getIngredientesDisponiveis();
    }
}
