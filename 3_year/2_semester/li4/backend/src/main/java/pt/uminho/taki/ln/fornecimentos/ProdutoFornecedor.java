package pt.uminho.taki.ln.fornecimentos;

import java.util.Objects;

/**
 * Representa a associação entre um Produto e um Fornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoFornecedor implements Cloneable {
    private String idProduto;
    private String idFornecedor;
    private double precoCusto;
    private boolean preferencial;

    /**
     * Constrói um novo ProdutoFornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     */
    public ProdutoFornecedor(String idProduto, String idFornecedor, double precoCusto) {
        this.idProduto = idProduto;
        this.idFornecedor = idFornecedor;
        this.precoCusto = precoCusto;
        this.preferencial = false;
    }

    /**
     * Constrói um novo ProdutoFornecedor com estatuto preferencial.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @param preferencial se é preferencial
     */
    public ProdutoFornecedor(String idProduto, String idFornecedor, double precoCusto, boolean preferencial) {
        this.idProduto = idProduto;
        this.idFornecedor = idFornecedor;
        this.precoCusto = precoCusto;
        this.preferencial = preferencial;
    }

    /**
     * Obtém o identificador do produto.
     *
     * @return o identificador do produto
     */
    public String getIdProduto() { return idProduto; }

    /**
     * Obtém o identificador do fornecedor.
     *
     * @return o identificador do fornecedor
     */
    public String getIdFornecedor() { return idFornecedor; }

    /**
     * Obtém o preço de custo.
     *
     * @return o preço de custo
     */
    public double getPrecoCusto() { return precoCusto; }

    /**
     * Define o preço de custo.
     *
     * @param precoCusto o preço de custo a definir
     */
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

    /**
     * Verifica se a associação é preferencial.
     *
     * @return verdadeiro se for preferencial, falso caso contrário.
     */
    public boolean isPreferencial() { return preferencial; }

    /**
     * Define o estatuto preferencial.
     *
     * @param preferencial o estatuto preferencial a definir
     */
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial; }

    /**
     * Constrói um novo ProdutoFornecedor através da cópia de outro.
     *
     * @param outro o ProdutoFornecedor a copiar
     */
    public ProdutoFornecedor(ProdutoFornecedor outro) {
        this.idProduto = outro.idProduto;
        this.idFornecedor = outro.idFornecedor;
        this.precoCusto = outro.precoCusto;
        this.preferencial = outro.preferencial;
    }

    /**
     * Verifica se este objeto é igual a outro.
     *
     * @param o o objeto a comparar
     * @return verdadeiro se forem iguais, falso caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdutoFornecedor that = (ProdutoFornecedor) o;
        return Objects.equals(idProduto, that.idProduto) &&
               Objects.equals(idFornecedor, that.idFornecedor);
    }

    /**
     * Retorna o código hash.
     *
     * @return o código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(idProduto, idFornecedor);
    }

    /**
     * Clona este objeto.
     *
     * @return o objeto clonado
     */
    @Override
    public ProdutoFornecedor clone() {
        return new ProdutoFornecedor(this);
    }
}
