package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.lojas.Categoria;
import jakarta.validation.constraints.NotBlank;

/**
 * Objeto de Transferência de Dados (DTO) para pedidos de categoria.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CategoriaRequestDto {

    /**
     * O identificador único da categoria.
     */
    private String idCategoria;

    /**
     * A designação ou nome da categoria.
     */
    @NotBlank(message = "A designação não pode ser vazia")
    private String designacao;

    /**
     * O identificador da categoria pai, se existir.
     */
    private String idCategoriaPai;

    /**
     * Obtém o identificador da categoria.
     *
     * @return o identificador da categoria
     */
    public String getIdCategoria() {
        return idCategoria;
    }

    /**
     * Define o identificador da categoria.
     *
     * @param idCategoria o identificador da categoria
     */
    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * Obtém a designação da categoria.
     *
     * @return a designação da categoria
     */
    public String getDesignacao() {
        return designacao;
    }

    /**
     * Define a designação da categoria.
     *
     * @param designacao a designação da categoria
     */
    public void setDesignacao(String designacao) {
        this.designacao = designacao;
    }

    /**
     * Obtém o identificador da categoria pai.
     *
     * @return o identificador da categoria pai
     */
    public String getIdCategoriaPai() {
        return idCategoriaPai;
    }

    /**
     * Define o identificador da categoria pai.
     *
     * @param idCategoriaPai o identificador da categoria pai
     */
    public void setIdCategoriaPai(String idCategoriaPai) {
        this.idCategoriaPai = idCategoriaPai;
    }

    /**
     * Converte este DTO num objeto de domínio.
     *
     * @return o objeto de domínio da categoria
     */
    public Categoria paraDominio() {
        return new Categoria(idCategoria, designacao, normalizarOpcional(idCategoriaPai));
    }

    /**
     * Normaliza um valor de texto opcional.
     *
     * @param valor o texto a normalizar
     * @return o texto sem espaços ou uma string vazia se for nulo ou apenas espaços
     */
    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return valor.trim();
    }
}
