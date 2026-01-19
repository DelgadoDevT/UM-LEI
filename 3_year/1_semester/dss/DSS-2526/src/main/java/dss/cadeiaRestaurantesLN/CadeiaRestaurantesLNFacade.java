package dss.cadeiaRestaurantesLN;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;
import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Main facade for the business logic layer of the Restaurant Chain Management System.
 * Coordinates operations between the restaurant management and menu/orders subsystems.
 * Implements the ICadeiaRestaurantesLN interface to provide a unified API for the UI layer.
 */
public class CadeiaRestaurantesLNFacade implements ICadeiaRestaurantesLN {
    private final IGestRestaurantes restaurantesFacade;
    private final IGestMenuPedidos menuPedidosFacade;

    /**
     * Default constructor. Initializes both subsystem facades.
     */
    public CadeiaRestaurantesLNFacade() {
        this.restaurantesFacade = new RestaurantesFacade();
        this.menuPedidosFacade = new MenuPedidosFacade();
    }

    /**
     * Constructor with specific facade instances.
     * @param restaurantesFacade Restaurant management facade
     * @param menuPedidosFacade Menu and orders facade
     */
    public CadeiaRestaurantesLNFacade(RestaurantesFacade restaurantesFacade, MenuPedidosFacade menuPedidosFacade) {
        this.restaurantesFacade = restaurantesFacade;
        this.menuPedidosFacade = menuPedidosFacade;
    }

    /**
     * Constructor with interface-based facade instances.
     * @param restaurantesFacade Restaurant management facade interface
     * @param menuPedidosFacade Menu and orders facade interface
     */
    public CadeiaRestaurantesLNFacade(IGestRestaurantes restaurantesFacade, IGestMenuPedidos menuPedidosFacade) {
        this.restaurantesFacade = restaurantesFacade;
        this.menuPedidosFacade = menuPedidosFacade;
    }

    /**
     * Copy constructor.
     * @param cr Facade instance to copy
     */
    public CadeiaRestaurantesLNFacade(CadeiaRestaurantesLNFacade cr) {
        this.restaurantesFacade = cr.getRestaurantesFacade();
        this.menuPedidosFacade = cr.getMenuPedidosFacade();
    }

    public IGestRestaurantes getRestaurantesFacade() {
        return restaurantesFacade;
    }

    public IGestMenuPedidos getMenuPedidosFacade() {
        return menuPedidosFacade;
    }

    public boolean isGestorGeral(Integer idFuncionario) {
        return restaurantesFacade.isGestorGeral(idFuncionario);
    }

    public void enviarMensagemParaRestaurantes(Integer idFuncionario, String mensagem, List<String> postos, List<Integer> idRestaurantes) {
        restaurantesFacade.enviarMensagem(idFuncionario, mensagem, postos, idRestaurantes);
    }

    public List<String> consultarIndicadoresDesempenho(LocalDate dataInicio, LocalDate dataFim, Integer idFuncionario, List<Integer> restaurantesId) {
        return restaurantesFacade.consultarIndicadoresDesempenho(dataInicio, dataFim, idFuncionario, restaurantesId);
    }

    public boolean existeFuncionario(Integer idFuncionario) {
        return restaurantesFacade.existeFuncionario(idFuncionario);
    }

    public boolean isFuncionarioRole(Integer idFuncionario, String role) {
        return restaurantesFacade.isFuncionarioRole(idFuncionario, role);
    }

    public List<String> getNomesRestaurantes(Integer idFuncionario) {
        return restaurantesFacade.getNomesRestaurantes(idFuncionario);
    }

    public Set<String> getTiposPostoValidosParaGestor(Integer idFuncionario) {
        return restaurantesFacade.getTiposPostoValidosParaGestor(idFuncionario);
    }

    public List<String> getMensagensPosto(Integer idFuncionario) {
        return restaurantesFacade.getMensagensPosto(idFuncionario);
    }

    public List<String> getPerfilFuncionario(Integer idFuncionario) {
        return restaurantesFacade.getPerfilFuncionario(idFuncionario);
    }

    /**
     * Gets the restaurant ID where an employee works.
     * @param idFuncionario Employee ID
     * @return Restaurant ID, or null if employee is a general manager
     */
    private Integer getRestauranteFuncionario(Integer idFuncionario) {
        return restaurantesFacade.getRestauranteIdPorFuncionario(idFuncionario);
    }

    @Override
    public List<String> consultarFilaCozinha(Integer idFuncionario) {
        Integer idRestaurante = getRestauranteFuncionario(idFuncionario);
        if (idRestaurante != null) {
            return menuPedidosFacade.consultarFilaPedidos(idRestaurante);
        }
        return List.of();
    }


    @Override
    public void iniciarPreparacaoPedido(Integer idPedido) {
        menuPedidosFacade.iniciarPreparacaoPedido(idPedido);
    }

    @Override
    public void concluirPreparacaoPedido(Integer idPedido) {
        menuPedidosFacade.concluirPreparacaoPedido(idPedido);
    }

    @Override
    public void cancelarPedido(Integer idPedido, String motivo) {
        menuPedidosFacade.cancelarPedido(idPedido, motivo);
    }

    @Override
    public List<String> consultarPedidosParaEntrega(Integer idFuncionario) {
        Integer idRestaurante = getRestauranteFuncionario(idFuncionario);
        if (idRestaurante != null) {
            return menuPedidosFacade.consultarPedidosProntos(idRestaurante);
        }
        return List.of();
    }

    @Override
    public void registarEntregaPedido(Integer idPedido) {
        menuPedidosFacade.entregarPedido(idPedido);
    }

    @Override
    public void criarPedido(int idRestaurante, List<LinhaDePedido> linhas, Tipo tipoConsumo, Pagamento pagamento) {
        this.menuPedidosFacade.criarPedido(idRestaurante, linhas, tipoConsumo, pagamento);
    }

    public List<String> getNomesRestaurantesPublico() {
        return this.restaurantesFacade.getNomesTodosRestaurantes();
    }

    public List<Ingrediente> getIngredientesDisponiveis() {
        return this.menuPedidosFacade.getIngredientesDisponiveis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CadeiaRestaurantesLNFacade that = (CadeiaRestaurantesLNFacade) o;
        return Objects.equals(restaurantesFacade, that.restaurantesFacade) && Objects.equals(menuPedidosFacade, that.menuPedidosFacade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantesFacade, menuPedidosFacade);
    }

    @Override
    public String toString() {
        return "CadeiaRestaurantesLNFacade{" +
                "restaurantesFacade=" + restaurantesFacade +
                ", menuPedidosFacade=" + menuPedidosFacade +
                '}';
    }

    @Override
    public CadeiaRestaurantesLNFacade clone() {
        return new CadeiaRestaurantesLNFacade(this);
    }
}