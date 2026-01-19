package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a customer order in the restaurant system.
 * Orders contain multiple order lines, have a state, and are associated with a restaurant.
 */
public class Pedido {
    private int id;
    private Estado estado;
    private Tipo tipoConsumo;
    private int idRestaurante;
    private double valorTotal;
    private List<LinhaDePedido> linhas;
    private Pagamento pagamento;

    /**
     * Default constructor. Creates an order with initial state REGISTADO.
     */
    public Pedido() {
        this.id = 0;
        this.estado = Estado.REGISTADO;
        this.tipoConsumo = null;
        this.idRestaurante = 0;
        this.valorTotal = 0.0;
        this.linhas = new ArrayList<>();
        this.pagamento = null;
    }

    /**
     * Parameterized constructor.
     * @param id Order ID
     * @param idRestaurante Restaurant ID where the order was placed
     * @param tipoConsumo Consumption type (e.g., "Dine-in", "Takeaway")
     * @param valorTotal Total order value
     * @param linhas List of order lines
     * @param pagamento Payment information
     */
    public Pedido(int id, int idRestaurante, Tipo tipoConsumo, double valorTotal, List<LinhaDePedido> linhas, Pagamento pagamento) {
        this.id = id;
        this.idRestaurante = idRestaurante;
        this.tipoConsumo = tipoConsumo;
        this.estado = Estado.REGISTADO;
        this.valorTotal = valorTotal;
        this.linhas = linhas;
        this.pagamento = pagamento;
    }

    /**
     * Copy constructor.
     * @param p Order to copy
     */
    public Pedido(Pedido p) {
        this.id = p.getId();
        this.estado = p.getEstado();
        this.tipoConsumo = p.getTipoConsumo();
        this.idRestaurante = p.getIdRestaurante();
        this.valorTotal = p.getValorTotal();
        this.linhas = p.getLinhas();
        this.pagamento = p.getPagamento();
    }

    public int getId() {
        return id;
    }

    public Estado getEstado() {
        return this.estado;
    }

    public Tipo getTipoConsumo() {
        return this.tipoConsumo;
    }

    public int getIdRestaurante() {
        return this.idRestaurante;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public List<LinhaDePedido> getLinhas() {
        return linhas;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setTipoConsumo(Tipo tipoConsumo) {
        this.tipoConsumo = tipoConsumo;
    }

    public void setIdRestaurante(int idRestaurante) {
        this.idRestaurante = idRestaurante;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setLinhas(List<LinhaDePedido> linhas) {
        this.linhas = linhas;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id && idRestaurante == pedido.idRestaurante && Double.compare(valorTotal, pedido.valorTotal) == 0 && estado == pedido.estado && Objects.equals(tipoConsumo, pedido.tipoConsumo) && Objects.equals(linhas, pedido.linhas) && Objects.equals(pagamento, pedido.pagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, estado, tipoConsumo, idRestaurante, valorTotal, linhas, pagamento);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", estado=" + estado +
                ", tipoConsumo='" + tipoConsumo + '\'' +
                ", idRestaurante=" + idRestaurante +
                ", valorTotal=" + valorTotal +
                ", linhas=" + linhas +
                ", pagamento=" + pagamento +
                '}';
    }

    @Override
    public Pedido clone() {
        return new Pedido(this);
    }
}