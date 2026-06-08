package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Locale;

/**
 * DTO para a criação ou atualização de um Produto.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoRequestDto {

    /**
     * O identificador único do produto.
     */
    private String idProduto;

    /**
     * O código de barras do produto.
     */
    @NotBlank(message = "O código de barras não pode ser vazio")
    private String codigoBarras;

    /**
     * O nome do produto.
     */
    @NotBlank(message = "O nome não pode ser vazio")
    private String nome;

    /**
     * A descrição detalhada do produto.
     */
    @NotBlank(message = "A descrição não pode ser vazia")
    private String descricao;

    /**
     * O preço de custo unitário.
     */
    @NotNull(message = "O preço de custo não pode ser nulo")
    @PositiveOrZero(message = "O preço de custo deve ser positivo ou zero")
    private Double precoCusto;

    /**
     * O preço de venda ao público.
     */
    @NotNull(message = "O preço de venda não pode ser nulo")
    @PositiveOrZero(message = "O preço de venda deve ser positivo ou zero")
    private Double precoVenda;

    /**
     * A representação textual da taxa de IVA.
     */
    @NotBlank(message = "A taxa de IVA não pode ser vazia")
    private String taxaIva;

    /**
     * A unidade de medida (ex.: UN, KG).
     */
    @NotBlank(message = "A unidade de medida não pode ser vazia")
    private String unidadeMedida;

    /**
     * O estado atual do produto (ex.: ATIVO, INATIVO).
     */
    private String estado;

    /**
     * Obtém o identificador do produto.
     *
     * @return o identificador do produto
     */
    public String getIdProduto() {
        return idProduto;
    }

    /**
     * Define o identificador do produto.
     *
     * @param idProduto o identificador do produto a definir
     */
    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    /**
     * Obtém o código de barras do produto.
     *
     * @return o código de barras do produto
     */
    public String getCodigoBarras() {
        return codigoBarras;
    }

    /**
     * Define o código de barras do produto.
     *
     * @param codigoBarras o código de barras do produto a definir
     */
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    /**
     * Obtém o nome do produto.
     *
     * @return o nome do produto
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do produto.
     *
     * @param nome o nome do produto a definir
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém a descrição do produto.
     *
     * @return a descrição do produto
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição do produto.
     *
     * @param descricao a descrição do produto a definir
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Obtém o preço de custo do produto.
     *
     * @return o preço de custo do produto
     */
    public Double getPrecoCusto() {
        return precoCusto;
    }

    /**
     * Define o preço de custo do produto.
     *
     * @param precoCusto o preço de custo do produto a definir
     */
    public void setPrecoCusto(Double precoCusto) {
        this.precoCusto = precoCusto;
    }

    /**
     * Obtém o preço de venda do produto.
     *
     * @return o preço de venda do produto
     */
    public Double getPrecoVenda() {
        return precoVenda;
    }

    /**
     * Define o preço de venda do produto.
     *
     * @param precoVenda o preço de venda do produto a definir
     */
    public void setPrecoVenda(Double precoVenda) {
        this.precoVenda = precoVenda;
    }

    /**
     * Obtém a taxa de IVA do produto.
     *
     * @return a taxa de IVA do produto
     */
    public String getTaxaIva() {
        return taxaIva;
    }

    /**
     * Define a taxa de IVA do produto.
     *
     * @param taxaIva a taxa de IVA do produto a definir
     */
    public void setTaxaIva(String taxaIva) {
        this.taxaIva = taxaIva;
    }

    /**
     * Obtém a unidade de medida do produto.
     *
     * @return a unidade de medida do produto
     */
    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    /**
     * Define a unidade de medida do produto.
     *
     * @param unidadeMedida a unidade de medida do produto a definir
     */
    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    /**
     * Obtém o estado do produto.
     *
     * @return o estado do produto
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define o estado do produto.
     *
     * @param estado o estado do produto a definir
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Converte este DTO para um objeto de domínio Produto.
     *
     * @return o objeto de domínio Produto correspondente
     */
    public Produto paraDominio() {
        return new Produto(
                idProduto,
                codigoBarras,
                nome,
                descricao,
                precoCusto != null ? precoCusto : 0.0,
                precoVenda != null ? precoVenda : 0.0,
                resolverTaxaIva(),
                unidadeMedida,
                estado
        );
    }

    /**
     * Resolve a taxa de IVA a partir da representação textual.
     *
     * @return a taxa de IVA correspondente
     * @throws IllegalArgumentException se a taxa de IVA for inválida ou irreconhecível
     */
    private TaxaIva resolverTaxaIva() {
        if (taxaIva == null || taxaIva.isBlank()) {
            return TaxaIva.NORMAL_23;
        }

        String normalizado = taxaIva.trim().toUpperCase(Locale.ROOT);
        try {
            return TaxaIva.valueOf(normalizado);
        } catch (IllegalArgumentException ignored) {
            try {
                return TaxaIva.fromValor(Double.parseDouble(normalizado.replace(',', '.')));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Taxa IVA inválida: " + taxaIva, ex);
            }
        }
    }
}
