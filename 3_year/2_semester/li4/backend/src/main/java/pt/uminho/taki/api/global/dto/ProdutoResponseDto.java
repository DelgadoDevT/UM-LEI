package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.lojas.Produto;

/**
 * DTO para o retorno de um Produto.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoResponseDto {

    private final String idProduto;
    private final String codigoBarras;
    private final String nome;
    private final String descricao;
    private final double precoCusto;
    private final double precoVenda;
    private final String taxaIva;
    private final String unidadeMedida;
    private final String estado;

    /**
     * Constrói uma nova ProdutoResponseDto.
     *
     * @param idProduto o identificador do produto
     * @param codigoBarras o código de barras do produto
     * @param nome o nome do produto
     * @param descricao a descrição do produto
     * @param precoCusto o preço de custo do produto
     * @param precoVenda o preço de venda do produto
     * @param taxaIva a taxa de IVA do produto
     * @param unidadeMedida a unidade de medida do produto
     * @param estado o estado do produto
     */
    public ProdutoResponseDto(
            String idProduto,
            String codigoBarras,
            String nome,
            String descricao,
            double precoCusto,
            double precoVenda,
            String taxaIva,
            String unidadeMedida,
            String estado
    ) {
        this.idProduto = idProduto;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.descricao = descricao;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.taxaIva = taxaIva;
        this.unidadeMedida = unidadeMedida;
        this.estado = estado;
    }

    /**
     * Cria uma ProdutoResponseDto a partir de um objeto de domínio Produto.
     *
     * @param produto o objeto de domínio Produto
     * @return a ProdutoResponseDto correspondente
     */
    public static ProdutoResponseDto from(Produto produto) {
        return new ProdutoResponseDto(
                produto.getIdProduto(),
                produto.getCodigoBarras(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPrecoCusto(),
                produto.getPrecoVenda(),
                produto.getTaxaIva() != null ? produto.getTaxaIva().name() : null,
                produto.getUnidadeMedida(),
                produto.getEstado()
        );
    }

    /**
     * Obtém o identificador do produto.
     *
     * @return o identificador do produto
     */
    public String getIdProduto() {
        return idProduto;
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
     * Obtém o nome do produto.
     *
     * @return o nome do produto
     */
    public String getNome() {
        return nome;
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
     * Obtém o preço de custo do produto.
     *
     * @return o preço de custo do produto
     */
    public double getPrecoCusto() {
        return precoCusto;
    }

    /**
     * Obtém o preço de venda do produto.
     *
     * @return o preço de venda do produto
     */
    public double getPrecoVenda() {
        return precoVenda;
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
     * Obtém a unidade de medida do produto.
     *
     * @return a unidade de medida do produto
     */
    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    /**
     * Obtém o estado do produto.
     *
     * @return o estado do produto
     */
    public String getEstado() {
        return estado;
    }
}
