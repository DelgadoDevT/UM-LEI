package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.List;
import java.util.Objects;

/**
 * Represents a line item in an order.
 * Each line contains an item (product or menu pack), quantity, price, and optional notes.
 */
public class LinhaDePedido {
    private int id;
    private int idPedido;
    private int quantidade;
    private double preco;
    private ArtigoVenda artigo;
    private String nota;

    /**
     * Default constructor. Creates an order line with empty values.
     */
    public LinhaDePedido() {
        this.id = 0;
        this.idPedido = 0;
        this.quantidade = 0;
        this.preco = 0.0;
        this.artigo = null;
        this.nota = "";
    }

    /**
     * Parameterized constructor.
     * @param id Line ID
     * @param idPedido Associated order ID
     * @param quantidade Quantity ordered
     * @param preco Line price
     * @param artigo Item being ordered
     * @param nota Optional notes for customization
     */
    public LinhaDePedido(int id, int idPedido, int quantidade, double preco, ArtigoVenda artigo, String nota) {
        this.id = id;
        this.idPedido = idPedido;
        this.quantidade = quantidade;
        this.preco = preco;
        this.artigo = artigo;
        this.nota = nota;
    }

    /**
     * Copy constructor.
     * @param lp Order line to copy
     */
    public LinhaDePedido(LinhaDePedido lp) {
        this.id = lp.getId();
        this.idPedido = lp.getIdPedido();
        this.quantidade = lp.getQuantidade();
        this.preco = lp.getPreco();
        this.artigo = lp.getArtigo();
        this.nota = lp.getNota();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public ArtigoVenda getArtigo() {
        return artigo;
    }

    public void setArtigo(ArtigoVenda artigo) {
        this.artigo = artigo;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LinhaDePedido that = (LinhaDePedido) o;
        return id == that.id && idPedido == that.idPedido && quantidade == that.quantidade && Double.compare(preco, that.preco) == 0 && Objects.equals(artigo, that.artigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idPedido, quantidade, preco, artigo);
    }

    @Override
    public String toString() {
        return "LinhaDePedido{" +
                "id=" + id +
                ", idPedido=" + idPedido +
                ", quantidade=" + quantidade +
                ", preco=" + preco +
                ", artigo=" + artigo +
                '}';
    }

    @Override
    public LinhaDePedido clone() {
        return new LinhaDePedido(this);
    }
}