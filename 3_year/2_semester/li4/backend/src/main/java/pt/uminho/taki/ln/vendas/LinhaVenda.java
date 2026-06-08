package pt.uminho.taki.ln.vendas;

import pt.uminho.taki.ln.lojas.Produto;

import java.util.Objects;

/**
 * Representa uma linha de venda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LinhaVenda {
    private String idLinhaVenda;
    private Produto produto;
    private int quantidade;
    private double desconto; // Em percentagem (0 a 100)
    private double subtotal; // Preço base pós-desconto * quantidade
    private double totalImposto; // Imposto calculado sobre o subtotal
    private double totalFinal; // Subtotal + totalImposto

    /**
     * Construtor por omissão para LinhaVenda.
     */
    public LinhaVenda() {
        this.idLinhaVenda = java.util.UUID.randomUUID().toString();
        this.quantidade = 0;
        this.desconto = 0.0;
        this.subtotal = 0.0;
        this.totalImposto = 0.0;
        this.totalFinal = 0.0;
    }

    /**
     * Construtor parametrizado para LinhaVenda.
     *
     * @param produto o produto
     * @param quantidade a quantidade
     * @param desconto a percentagem de desconto
     */
    public LinhaVenda(Produto produto, int quantidade, double desconto) {
        this.idLinhaVenda = java.util.UUID.randomUUID().toString();
        this.produto = produto;
        this.quantidade = quantidade;
        this.desconto = desconto;
        recalcularValores();
    }

    /**
     * Construtor de cópia para LinhaVenda.
     *
     * @param l a LinhaVenda a copiar
     */
    public LinhaVenda(LinhaVenda l) {
        this.idLinhaVenda = l.idLinhaVenda;
        this.produto = l.produto != null ? l.produto.clone() : null;
        this.quantidade = l.quantidade;
        this.desconto = l.desconto;
        this.subtotal = l.subtotal;
        this.totalImposto = l.totalImposto;
        this.totalFinal = l.totalFinal;
    }

    /**
     * Recalcula os valores do subtotal, imposto total e total final.
     */
    public void recalcularValores() {
        if (this.produto == null || this.quantidade <= 0) {
            this.subtotal = 0.0;
            this.totalImposto = 0.0;
            this.totalFinal = 0.0;
            return;
        }

        double precoUnitario = this.produto.getPrecoVenda();
        double valorDescontoTotalUnitario = precoUnitario * (this.desconto / 100.0);
        double precoUnitarioComDesconto = precoUnitario - valorDescontoTotalUnitario;

        this.subtotal = precoUnitarioComDesconto * this.quantidade;
        this.totalImposto = this.subtotal * this.produto.getTaxaIvaValor();
        this.totalFinal = this.subtotal + this.totalImposto;
    }

    /**
     * Obtém o produto.
     *
     * @return o produto
     */
    public Produto getProduto() { return produto; }

    /**
     * Define o produto e recalcula os valores.
     *
     * @param produto o produto
     */
    public void setProduto(Produto produto) { 
        this.produto = produto; 
        recalcularValores();
    }

    /**
     * Obtém o identificador da linha de venda.
     *
     * @return o identificador da linha de venda
     */
    public String getIdLinhaVenda() { return idLinhaVenda; }

    /**
     * Define o identificador da linha de venda.
     *
     * @param idLinhaVenda o identificador da linha de venda
     */
    public void setIdLinhaVenda(String idLinhaVenda) { this.idLinhaVenda = idLinhaVenda; }

    /**
     * Obtém a quantidade.
     *
     * @return a quantidade
     */
    public int getQuantidade() { return quantidade; }

    /**
     * Define a quantidade e recalcula os valores.
     *
     * @param quantidade a quantidade
     */
    public void setQuantidade(int quantidade) { 
        this.quantidade = quantidade; 
        recalcularValores();
    }

    /**
     * Obtém a percentagem de desconto.
     *
     * @return a percentagem de desconto
     */
    public double getDesconto() { return desconto; }

    /**
     * Define a percentagem de desconto e recalcula os valores.
     *
     * @param desconto a percentagem de desconto
     */
    public void setDesconto(double desconto) { 
        this.desconto = desconto; 
        recalcularValores();
    }

    /**
     * Obtém o valor do subtotal.
     *
     * @return o valor do subtotal
     */
    public double getSubtotal() { return subtotal; }

    /**
     * Obtém o valor total do imposto.
     *
     * @return o valor total do imposto
     */
    public double getTotalImposto() { return totalImposto; }

    /**
     * Obtém o valor total final.
     *
     * @return o valor total final
     */
    public double getTotalFinal() { return totalFinal; }

    /**
     * Verifica se esta instância é igual a outro objeto.
     *
     * @param o o objeto a comparar
     * @return true se forem iguais, false caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinhaVenda that = (LinhaVenda) o;
        return quantidade == that.quantidade &&
               Double.compare(that.desconto, desconto) == 0 &&
               Double.compare(that.subtotal, subtotal) == 0 &&
               Double.compare(that.totalImposto, totalImposto) == 0 &&
               Double.compare(that.totalFinal, totalFinal) == 0 &&
               Objects.equals(produto, that.produto);
    }

    /**
     * Gera um código hash para esta instância.
     *
     * @return o código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(produto, quantidade, desconto, subtotal, totalImposto, totalFinal);
    }

    /**
     * Clona esta linha de venda.
     *
     * @return uma nova instância clonada de LinhaVenda
     */
    @Override
    public LinhaVenda clone() {
        return new LinhaVenda(this);
    }
}
