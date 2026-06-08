package pt.uminho.taki.ln.vendas;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Promoção.
 * @author TakiLN Team
 * @since 1.0
 */
public class Promocao {
    private String idPromocao;
    private String designacao;
    private double desconto;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String estado;
    private Integer idLoja;
    private Set<String> produtos;
    private Set<String> categorias;

    /**
     * Construtor para Promocao.
     */
    public Promocao() {
        this.produtos = new HashSet<>();
        this.categorias = new HashSet<>();
    }

    /**
     * Construtor para Promocao.
     * @param idPromocao o identificador da promoção
     * @param designacao a designação
     * @param desconto o desconto
     * @param dataInicio a data de início
     * @param dataFim a data de fim
     * @param estado o estado
     * @param idLoja o identificador da loja
     */
    public Promocao(String idPromocao, String designacao, double desconto, LocalDateTime dataInicio, LocalDateTime dataFim, String estado, Integer idLoja) {
        this.idPromocao = idPromocao;
        this.designacao = designacao;
        this.desconto = desconto;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.estado = estado;
        this.idLoja = idLoja;
        this.produtos = new HashSet<>();
        this.categorias = new HashSet<>();
    }

    // Getters and Setters
    /**
     * Obtém o identificador da promoção.
     * @return o identificador da promoção
     */
    public String getIdPromocao() {
        return idPromocao;
    }

    /**
     * Define o identificador da promoção.
     * @param idPromocao o identificador da promoção
     */
    public void setIdPromocao(String idPromocao) {
        this.idPromocao = idPromocao;
    }

    /**
     * Obtém a designação.
     * @return a designação
     */
    public String getDesignacao() {
        return designacao;
    }

    /**
     * Define a designação.
     * @param designacao a designação
     */
    public void setDesignacao(String designacao) {
        this.designacao = designacao;
    }

    /**
     * Obtém o desconto.
     * @return o desconto
     */
    public double getDesconto() {
        return desconto;
    }

    /**
     * Define o desconto.
     * @param desconto o desconto
     */
    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    /**
     * Obtém a data de início.
     * @return a data de início
     */
    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    /**
     * Define a data de início.
     * @param dataInicio a data de início
     */
    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    /**
     * Obtém a data de fim.
     * @return a data de fim
     */
    public LocalDateTime getDataFim() {
        return dataFim;
    }

    /**
     * Define a data de fim.
     * @param dataFim a data de fim
     */
    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    /**
     * Obtém o estado.
     * @return o estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define o estado.
     * @param estado o estado
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtém o identificador da loja.
     * @return o identificador da loja
     */
    public Integer getIdLoja() {
        return idLoja;
    }

    /**
     * Define o identificador da loja.
     * @param idLoja o identificador da loja
     */
    public void setIdLoja(Integer idLoja) {
        this.idLoja = idLoja;
    }

    /**
     * Obtém os produtos.
     * @return os produtos
     */
    public Set<String> getProdutos() {
        return produtos;
    }

    /**
     * Define os produtos.
     * @param produtos os produtos
     */
    public void setProdutos(Set<String> produtos) {
        this.produtos = produtos;
    }

    /**
     * Obtém as categorias.
     * @return as categorias
     */
    public Set<String> getCategorias() {
        return categorias;
    }

    /**
     * Define as categorias.
     * @param categorias as categorias
     */
    public void setCategorias(Set<String> categorias) {
        this.categorias = categorias;
    }
}
