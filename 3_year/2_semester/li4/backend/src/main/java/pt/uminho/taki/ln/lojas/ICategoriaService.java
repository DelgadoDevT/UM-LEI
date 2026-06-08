package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.ln.lojas.exceptions.CategoriaInvalidaException;
import java.util.List;

/**
 * Interface para o serviço de gestão de categorias.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface ICategoriaService {
    /**
     * Regista uma nova categoria no sistema.
     * @param categoria a categoria a adicionar
     * @throws CategoriaInvalidaException se a hierarquia for inv\u00e1lida
     */
    void adicionarCategoria(Categoria categoria) throws CategoriaInvalidaException;

    /**
     * Valida se uma dada categoria pai existe e cumpre as restri\u00e7\u00f5es de hierarquia.
     * @param idCategoriaPai o id da categoria pai a validar
     * @throws CategoriaInvalidaException se a categoria n\u00e3o for apta a ser pai
     */
    void validarHierarquia(String idCategoriaPai) throws CategoriaInvalidaException;

    /**
     * Devolve a lista de todas as categorias ativas.
     * @return lista de categorias
     */
    List<Categoria> listarCategorias();

    /**
     * Atualiza os dados de uma categoria existente.
     * @param categoria categoria com os dados finais
     * @throws CategoriaInvalidaException se a categoria não existir ou tiver hierarquia inválida
     */
    void editarCategoria(Categoria categoria) throws CategoriaInvalidaException;

    /**
     * Inativa/descontinua uma categoria.
     * @param idCategoria categoria alvo
     * @throws CategoriaInvalidaException se a categoria não existir
     */
    void inativarCategoria(String idCategoria) throws CategoriaInvalidaException;

    /**
     * Devolve a lista de IDs de categorias desde a categoria indicada até à raiz da hierarquia.
     * @param idCategoria o identificador da categoria inicial
     * @return lista de identificadores (UUIDs)
     */
    List<String> obterCaminhoHierarquico(String idCategoria);
}
