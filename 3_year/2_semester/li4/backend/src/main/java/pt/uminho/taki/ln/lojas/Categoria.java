package pt.uminho.taki.ln.lojas;

import java.util.Objects;

/**
 * Representa uma Categoria.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Categoria {
    private String idCategoria;
    private String designacao;
    private String idCategoriaPai;

    /**
     * Construtor por omissão.
     */
    public Categoria() {
        this.idCategoria = "";
        this.designacao = "";
        this.idCategoriaPai = "";
    }

    /**
     * Construtor parametrizado.
     *
     * @param idCategoria o identificador da categoria
     * @param designacao a designação da categoria
     * @param idCategoriaPai o identificador da categoria pai
     */
    public Categoria(String idCategoria, String designacao, String idCategoriaPai) {
        this.idCategoria = idCategoria;
        this.designacao = designacao;
        this.idCategoriaPai = idCategoriaPai;
    }

    /**
     * Construtor de cópia.
     *
     * @param c a categoria a copiar
     */
    public Categoria(Categoria c) {
        this.idCategoria = c.getIdCategoria();
        this.designacao = c.getDesignacao();
        this.idCategoriaPai = c.getIdCategoriaPai();
    }

    /**
     * Obtém o identificador da categoria.
     *
     * @return o identificador da categoria
     */
    public String getIdCategoria() { return idCategoria; }
    /**
     * Define o identificador da categoria.
     *
     * @param idCategoria o identificador da categoria
     */
    public void setIdCategoria(String idCategoria) { this.idCategoria = idCategoria; }
    /**
     * Obtém a designação da categoria.
     *
     * @return a designação da categoria
     */
    public String getDesignacao() { return designacao; }
    /**
     * Define a designação da categoria.
     *
     * @param designacao a designação da categoria
     */
    public void setDesignacao(String designacao) { this.designacao = designacao; }
    /**
     * Obtém o identificador da categoria pai.
     *
     * @return o identificador da categoria pai
     */
    public String getIdCategoriaPai() { return idCategoriaPai; }
    /**
     * Define o identificador da categoria pai.
     *
     * @param idCategoriaPai o identificador da categoria pai
     */
    public void setIdCategoriaPai(String idCategoriaPai) { this.idCategoriaPai = idCategoriaPai; }

    /**
     * Verifica se esta categoria é igual a outro objeto.
     *
     * @param o o objeto a comparar
     * @return true se igual, false caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(idCategoria, categoria.idCategoria) &&
               Objects.equals(designacao, categoria.designacao) &&
               Objects.equals(idCategoriaPai, categoria.idCategoriaPai);
    }

    /**
     * Calcula o código hash.
     *
     * @return o código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(idCategoria, designacao, idCategoriaPai);
    }

    /**
     * Retorna uma representação em cadeia de caracteres.
     *
     * @return a representação em cadeia de caracteres
     */
    @Override
    public String toString() {
        return "Categoria{" +
               "idCategoria='" + idCategoria + "'" +
               ", designacao='" + designacao + "'" +
               ", idCategoriaPai='" + idCategoriaPai + "'" +
               "}";
    }

    /**
     * Clona a categoria.
     *
     * @return um clone desta categoria
     */
    @Override
    public Categoria clone() {
        return new Categoria(this);
    }
}
