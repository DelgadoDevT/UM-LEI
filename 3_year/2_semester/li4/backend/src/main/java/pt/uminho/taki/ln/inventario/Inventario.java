package pt.uminho.taki.ln.inventario;

import java.util.Objects;

/**
 * Entidade que representa o Inventario (Stock) de um produto numa loja.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Inventario {
    private String id;
    private double quantidade;
    private double quantidadeMinima;
    private int idLoja;
    private String idProduto;

    /**
     * Construtor vazio para a classe Inventario.
     */
    public Inventario() {
        this.id = "";
        this.quantidade = 0.0;
        this.quantidadeMinima = 0.0;
        this.idLoja = 0;
        this.idProduto = "";
    }

    /**
     * Construtor completo para a classe Inventario.
     * @param id o id do inventario
     * @param quantidade a quantidade em stock
     * @param quantidadeMinima o limite minimo de segurança
     * @param idLoja o identificador da loja
     * @param idProduto o identificador do produto
     */
    public Inventario(String id, double quantidade, double quantidadeMinima, int idLoja, String idProduto) {
        this.id = id;
        this.quantidade = quantidade;
        this.quantidadeMinima = quantidadeMinima;
        this.idLoja = idLoja;
        this.idProduto = idProduto;
    }

    /**
     * Construtor de cópia para a classe Inventario.
     * @param i o inventario a copiar
     */
    public Inventario(Inventario i) {
        this.id = i.getId();
        this.quantidade = i.getQuantidade();
        this.quantidadeMinima = i.getQuantidadeMinima();
        this.idLoja = i.getIdLoja();
        this.idProduto = i.getIdProduto();
    }

    /**
     * Obtém o identificador do inventário.
     * @return o identificador
     */
    public String getId() { return id; }
    /**
     * Define o identificador do inventário.
     * @param id o novo identificador
     */
    public void setId(String id) { this.id = id; }
    /**
     * Obtém a quantidade atual em stock.
     * @return a quantidade
     */
    public double getQuantidade() { return quantidade; }
    /**
     * Define a quantidade atual em stock.
     * @param quantidade a nova quantidade
     */
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    /**
     * Obtém o limite mínimo de segurança para o stock.
     * @return a quantidade mínima
     */
    public double getQuantidadeMinima() { return quantidadeMinima; }
    /**
     * Define o limite mínimo de segurança para o stock.
     * @param quantidadeMinima a nova quantidade mínima
     */
    public void setQuantidadeMinima(double quantidadeMinima) { this.quantidadeMinima = quantidadeMinima; }
    /**
     * Obtém o identificador da loja.
     * @return o identificador da loja
     */
    public int getIdLoja() { return idLoja; }
    /**
     * Define o identificador da loja.
     * @param idLoja o novo identificador da loja
     */
    public void setIdLoja(int idLoja) { this.idLoja = idLoja; }
    /**
     * Obtém o identificador do produto associado.
     * @return o identificador do produto
     */
    public String getIdProduto() { return idProduto; }
    /**
     * Define o identificador do produto associado.
     * @param idProduto o novo identificador do produto
     */
    public void setIdProduto(String idProduto) { this.idProduto = idProduto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventario that = (Inventario) o;
        return Double.compare(that.quantidade, quantidade) == 0 &&
               Double.compare(that.quantidadeMinima, quantidadeMinima) == 0 &&
               idLoja == that.idLoja &&
               Objects.equals(id, that.id) &&
               Objects.equals(idProduto, that.idProduto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantidade, quantidadeMinima, idLoja, idProduto);
    }

    @Override
    public String toString() {
        return "Inventario{" +
               "id='" + id + '\'' +
               ", quantidade=" + quantidade +
               ", quantidadeMinima=" + quantidadeMinima +
               ", idLoja=" + idLoja +
               ", idProduto='" + idProduto + '\'' +
               '}';
    }

    @Override
    public Inventario clone() {
        return new Inventario(this);
    }
}
