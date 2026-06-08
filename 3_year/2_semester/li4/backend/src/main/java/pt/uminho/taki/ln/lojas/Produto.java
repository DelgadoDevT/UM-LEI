package pt.uminho.taki.ln.lojas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entidade que representa um Produto no sistema.
 * Contém informações sobre preços, impostos e estado de comercialização.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Produto {
    /** Identificador único do produto. */
    private String idProduto;
    /** Código de barras do produto. */
    private String codigoBarras;
    /** Nome do produto. */
    private String nome;
    /** Descrição detalhada do produto. */
    private String descricao;
    /** Preço de custo unitário. */
    private double precoCusto;
    /** Preço de venda ao público. */
    private double precoVenda;
    /** Taxa de IVA aplicável. */
    private TaxaIva taxaIva;
    /** Unidade de medida. */
    private String unidadeMedida;
    /** Estado de comercialização do produto. */
    private String estado;

    /**
     * Construtor padrão que inicializa um Produto com valores pré-definidos.
     */
    public Produto() {
        this.idProduto = "";
        this.codigoBarras = "";
        this.nome = "";
        this.descricao = "";
        this.precoCusto = 0.0;
        this.precoVenda = 0.0;
        this.taxaIva = TaxaIva.NORMAL_23;
        this.unidadeMedida = "unidade";
        this.estado = "Ativo";
    }

    /**
     * Construtor completo para a criação de um novo Produto.
     *
     * @param idProduto o identificador único do produto
     * @param codigoBarras o código de barras do produto
     * @param nome o nome do produto
     * @param descricao a descrição detalhada do produto
     * @param precoCusto o preço de custo unitário
     * @param precoVenda o preço de venda ao público
     * @param taxaIva a taxa de IVA aplicável
     * @param unidadeMedida a unidade de medida (ex: unidade, kg, l)
     * @param estado o estado do produto (ex: Ativo, Inativo)
     */
    public Produto(String idProduto, String codigoBarras, String nome, String descricao,
                   double precoCusto, double precoVenda, TaxaIva taxaIva,
                   String unidadeMedida, String estado) {
        this.idProduto = idProduto;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.descricao = descricao;
        setPrecoCusto(precoCusto);
        setPrecoVenda(precoVenda);
        this.taxaIva = taxaIva;
        this.unidadeMedida = unidadeMedida != null ? unidadeMedida : "unidade";
        this.estado = estado != null ? estado : "Ativo";
    }

    /**
     * Construtor de cópia.
     *
     * @param p o produto a copiar
     */
    public Produto(Produto p) {
        this.idProduto = p.getIdProduto();
        this.codigoBarras = p.getCodigoBarras();
        this.nome = p.getNome();
        this.descricao = p.getDescricao();
        this.precoCusto = p.getPrecoCusto();
        this.precoVenda = p.getPrecoVenda();
        this.taxaIva = p.getTaxaIva();
        this.unidadeMedida = p.getUnidadeMedida();
        this.estado = p.getEstado();
    }

    /**
     * Obtenção do identificador único do produto.
     * @return o identificador do produto
     */
    public String getIdProduto() { return idProduto; }
    /**
     * Definição do identificador único do produto.
     * @param idProduto o identificador do produto
     */
    public void setIdProduto(String idProduto) { this.idProduto = idProduto; }

    /**
     * Obtenção do código de barras.
     * @return o código de barras
     */
    public String getCodigoBarras() { return codigoBarras; }
    /**
     * Definição do código de barras.
     * @param codigoBarras o código de barras
     */
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    /**
     * Obtenção do nome do produto.
     * @return o nome do produto
     */
    public String getNome() { return nome; }
    /**
     * Definição do nome do produto.
     * @param nome o nome do produto
     */
    public void setNome(String nome) { this.nome = nome; }

    /**
     * Obtenção da descrição do produto.
     * @return a descrição do produto
     */
    public String getDescricao() { return descricao; }
    /**
     * Definição da descrição do produto.
     * @param descricao a descrição do produto
     */
    public void setDescricao(String descricao) { this.descricao = descricao; }

    /**
     * Obtenção do preço de custo.
     * @return o preço de custo
     */
    public double getPrecoCusto() { return precoCusto; }
    /**
     * Definição do preço de custo.
     * @param precoCusto o preço de custo
     */
    public void setPrecoCusto(double precoCusto) {
        validarFormatoPreco(precoCusto, "preço de custo");
        this.precoCusto = precoCusto;
    }

    /**
     * Obtenção do preço de venda.
     * @return o preço de venda
     */
    public double getPrecoVenda() { return precoVenda; }
    /**
     * Definição do preço de venda.
     * @param precoVenda o preço de venda
     */
    public void setPrecoVenda(double precoVenda) {
        validarFormatoPreco(precoVenda, "preço de venda");
        this.precoVenda = precoVenda;
    }

    /**
     * Obtenção da taxa de IVA.
     * @return a taxa de IVA
     */
    public TaxaIva getTaxaIva() { return taxaIva; }
    /**
     * Definição da taxa de IVA.
     * @param taxaIva a taxa de IVA
     */
    public void setTaxaIva(TaxaIva taxaIva) { this.taxaIva = taxaIva; }

    /**
     * Obtenção do valor numérico da taxa de IVA (ex: 0.23).
     * @return o valor da taxa de IVA
     */
    public double getTaxaIvaValor() {
        return this.taxaIva != null ? this.taxaIva.getValor() : 0.0;
    }

    /**
     * Obtenção da unidade de medida.
     * @return a unidade de medida
     */
    public String getUnidadeMedida() { return unidadeMedida; }
    /**
     * Definição da unidade de medida.
     * @param unidadeMedida a unidade de medida
     */
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }

    /**
     * Obtenção do estado do produto.
     * @return o estado do produto
     */
    public String getEstado() { return estado; }
    /**
     * Definição do estado do produto.
     * @param estado o estado do produto
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Verifica se o produto se encontra ativo.
     * @return true se o estado for "Ativo", false caso contrário
     */
    public boolean isAtivo() { return "Ativo".equals(this.estado); }

    /**
     * Calcula o preço de venda baseando-se no preço de custo e no imposto aplicado.
     *
     * @return o preço de venda calculado
     */
    public double calcularPrecoVenda() {
        return arredondarDuasCasas(this.precoCusto * (1 + getTaxaIvaValor()));
    }

    private static void validarFormatoPreco(double valor, String campo) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ".");
        }
        BigDecimal decimal = BigDecimal.valueOf(valor).stripTrailingZeros();
        if (decimal.scale() > 2) {
            throw new IllegalArgumentException("O " + campo + " deve ter no máximo 2 casas decimais.");
        }
    }

    private static double arredondarDuasCasas(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Double.compare(produto.precoCusto, precoCusto) == 0 &&
               Double.compare(produto.precoVenda, precoVenda) == 0 &&
               Objects.equals(idProduto, produto.idProduto) &&
               Objects.equals(codigoBarras, produto.codigoBarras) &&
               Objects.equals(nome, produto.nome) &&
               Objects.equals(descricao, produto.descricao) &&
               taxaIva == produto.taxaIva &&
               Objects.equals(unidadeMedida, produto.unidadeMedida) &&
               Objects.equals(estado, produto.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduto, codigoBarras, nome, descricao, precoCusto,
                           precoVenda, taxaIva, unidadeMedida, estado);
    }

    @Override
    public String toString() {
        return "Produto{" +
               "idProduto='" + idProduto + "'" +
               ", codigoBarras='" + codigoBarras + "'" +
               ", nome='" + nome + "'" +
               ", descricao='" + descricao + "'" +
               ", precoCusto=" + precoCusto +
               ", precoVenda=" + precoVenda +
               ", taxaIva=" + taxaIva +
               ", unidadeMedida='" + unidadeMedida + "'" +
               ", estado='" + estado + "'" +
               "}";
    }

    @Override
    public Produto clone() {
        return new Produto(this);
    }
}
