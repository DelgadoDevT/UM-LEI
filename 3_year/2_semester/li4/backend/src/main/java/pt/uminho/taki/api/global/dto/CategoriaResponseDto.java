package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.lojas.Categoria;

/**
 * Objeto de Transferência de Dados (DTO) para respostas de categoria.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CategoriaResponseDto {

    private final String idCategoria;
    private final String designacao;
    private final String idCategoriaPai;

    /**
     * Constrói uma nova instância de CategoriaResponseDto.
     *
     * @param idCategoria o identificador da categoria
     * @param designacao a designação da categoria
     * @param idCategoriaPai o identificador da categoria pai
     */
    public CategoriaResponseDto(String idCategoria, String designacao, String idCategoriaPai) {
        this.idCategoria = idCategoria;
        this.designacao = designacao;
        this.idCategoriaPai = idCategoriaPai;
    }

    /**
     * Cria um DTO de resposta a partir de um objeto de domínio.
     *
     * @param categoria o objeto de domínio da categoria
     * @return o DTO de resposta
     */
    public static CategoriaResponseDto from(Categoria categoria) {
        return new CategoriaResponseDto(
                categoria.getIdCategoria(),
                categoria.getDesignacao(),
                categoria.getIdCategoriaPai()
        );
    }

    /**
     * Obtém o identificador da categoria.
     *
     * @return o identificador da categoria
     */
    public String getIdCategoria() {
        return idCategoria;
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
     * Obtém o identificador da categoria pai.
     *
     * @return o identificador da categoria pai
     */
    public String getIdCategoriaPai() {
        return idCategoriaPai;
    }
}
