package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;

/**
 * Interface para o serviço de gestão de produtos do catálogo.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IProdutoService {
    /**
     * Regista um novo produto no catalogo.
     * @param produto o produto a adicionar
     * @throws ProdutoExistenteException se o codigo EAN ja existir
     */
    void adicionarProduto(Produto produto) throws ProdutoExistenteException;

    /**
     * Edita os dados de um produto existente.
     * @param produto o produto a atualizar
     * @throws ProdutoInexistenteException se o produto nao existir
     * @throws ProdutoInativoException se o produto estiver inativo
     * @throws ProdutoExistenteException se o novo EAN ja pertencer a outro produto
     */
    void editarProduto(Produto produto) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException;

    /**
     * Inativa um produto de forma logica no sistema.
     * @param idProduto o identificador do produto
     * @throws ProdutoInativoException se o produto ja estiver inativo
     */
    void inativarProduto(String idProduto) throws ProdutoInativoException;

    /**
     * Pesquisa um produto pelo seu codigo de barras.
     * @param codigoBarras o codigo de barras a procurar
     * @return o produto encontrado ou null
     */
    Produto pesquisarPorCodigoBarras(String codigoBarras);

    /**
     * Associa um produto a uma categoria.
     * @param idProduto produto alvo
     * @param idCategoria categoria alvo
     */
    void associarCategoria(String idProduto, String idCategoria);

    /**
     * Remove associação entre produto e categoria.
     * @param idProduto produto alvo
     * @param idCategoria categoria alvo
     */
    void removerCategoria(String idProduto, String idCategoria);

    /**
     * Lista produtos de uma categoria.
     * @param idCategoria categoria alvo
     * @return lista de produtos associados
     */
    java.util.List<Produto> listarProdutosPorCategoria(String idCategoria);

    /**
     * Retorna a lista de todos os produtos registados.
     * @return lista de produtos
     */
    java.util.List<Produto> listarProdutos();
}
