package pt.uminho.taki.ln.fornecimentos;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma linha de uma encomenda, contendo o detalhe do produto e quantidade.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LinhaEncomenda implements Cloneable {
    /** Identificador único da linha de encomenda (UUID). */
    private String idLinhaEncomenda;
    /** Identificador da encomenda associada (UUID). */
    private String idEncomenda;
    /** Identificador do produto (UUID). */
    private String idProduto;
    /** Quantidade encomendada. */
    private double quantidade;
    /** Preço de custo aplicado. */
    private double precoCustoAplicado;

    /**
     * Construtor completo com todos os campos, incluindo o ID gerado pela base de dados.
     *
     * @param idLinhaEncomenda o identificador único da linha de encomenda
     * @param idEncomenda o identificador da encomenda associada
     * @param idProduto o identificador do produto
     * @param quantidade a quantidade encomendada
     * @param precoCustoAplicado o preço de custo aplicado no momento da encomenda
     */
    public LinhaEncomenda(String idLinhaEncomenda, String idEncomenda, String idProduto, 
                          double quantidade, double precoCustoAplicado) {
        this.idLinhaEncomenda = idLinhaEncomenda;
        this.idEncomenda = idEncomenda;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.precoCustoAplicado = precoCustoAplicado;
    }

    /**
     * Construtor para novas entradas (gera um UUID aleatório para idLinhaEncomenda).
     *
     * @param idEncomenda o identificador da encomenda associada
     * @param idProduto o identificador do produto
     * @param quantidade a quantidade encomendada
     * @param precoCustoAplicado o preço de custo aplicado
     */
    public LinhaEncomenda(String idEncomenda, String idProduto, 
                          double quantidade, double precoCustoAplicado) {
        this.idLinhaEncomenda = UUID.randomUUID().toString();
        this.idEncomenda = idEncomenda;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.precoCustoAplicado = precoCustoAplicado;
    }

    /**
     * Construtor de cópia para a classe LinhaEncomenda.
     *
     * @param other a instância a copiar
     */
    public LinhaEncomenda(LinhaEncomenda other) {
        this.idLinhaEncomenda = other.idLinhaEncomenda;
        this.idEncomenda = other.idEncomenda;
        this.idProduto = other.idProduto;
        this.quantidade = other.quantidade;
        this.precoCustoAplicado = other.precoCustoAplicado;
    }

    // Getters
    /**
     * Obtém o identificador da linha de encomenda.
     *
     * @return o identificador da linha
     */
    public String getIdLinhaEncomenda() { return idLinhaEncomenda; }
    /**
     * Obtém o identificador da encomenda.
     *
     * @return o identificador da encomenda
     */
    public String getIdEncomenda() { return idEncomenda; }
    /**
     * Obtém o identificador do produto.
     *
     * @return o identificador do produto
     */
    public String getIdProduto() { return idProduto; }
    /**
     * Obtém a quantidade encomendada.
     *
     * @return a quantidade
     */
    public double getQuantidade() { return quantidade; }
    /**
     * Obtém o preço de custo aplicado.
     *
     * @return o preço de custo
     */
    public double getPrecoCustoAplicado() { return precoCustoAplicado; }
    
    // Setters
    /**
     * Define o identificador da linha de encomenda.
     *
     * @param idLinhaEncomenda o novo identificador
     */
    public void setIdLinhaEncomenda(String idLinhaEncomenda) { this.idLinhaEncomenda = idLinhaEncomenda; }
    /**
     * Define o identificador da encomenda.
     *
     * @param idEncomenda o identificador da encomenda
     */
    public void setIdEncomenda(String idEncomenda) { this.idEncomenda = idEncomenda; }
    /**
     * Define o identificador do produto.
     *
     * @param idProduto o identificador do produto
     */
    public void setIdProduto(String idProduto) { this.idProduto = idProduto; }
    /**
     * Define a quantidade encomendada.
     *
     * @param quantidade a quantidade
     */
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    /**
     * Define o preço de custo aplicado.
     *
     * @param precoCustoAplicado o preço de custo
     */
    public void setPrecoCustoAplicado(double precoCustoAplicado) { this.precoCustoAplicado = precoCustoAplicado; }

    /**
     * Calcula o subtotal da linha de encomenda (quantidade * preço).
     *
     * @return o valor subtotal
     */
    public double getSubTotal() {
        return quantidade * precoCustoAplicado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinhaEncomenda that = (LinhaEncomenda) o;
        // Use idLinhaEncomenda if present, otherwise use composite key
        if (idLinhaEncomenda != null && that.idLinhaEncomenda != null) {
            return Objects.equals(idLinhaEncomenda, that.idLinhaEncomenda);
        }
        return Objects.equals(idEncomenda, that.idEncomenda) &&
               Objects.equals(idProduto, that.idProduto);
    }

    @Override
    public int hashCode() {
        // Use idLinhaEncomenda if present, otherwise use composite key
        if (idLinhaEncomenda != null) {
            return Objects.hash(idLinhaEncomenda);
        }
        return Objects.hash(idEncomenda, idProduto);
    }

    @Override
    public LinhaEncomenda clone() {
        return new LinhaEncomenda(this);
    }
}
