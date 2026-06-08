package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;
import java.util.Optional;

/**
 * Serviço para Produto.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoService implements IProdutoService {

    private final ProdutoDAO produtoDAO;

    /**
     * Constrói um novo ProdutoService.
     *
     * @param produtoDAO o ProdutoDAO
     */
    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    /**
     * Adiciona um novo Produto.
     *
     * @param produto o Produto a adicionar
     * @throws ProdutoExistenteException se o Produto já existir.
     */
    @Override
    public void adicionarProduto(Produto produto) throws ProdutoExistenteException {
        Optional<Produto> existente = produtoDAO.findByCodigoBarras(produto.getCodigoBarras());
        if (existente.isPresent()) {
            throw new ProdutoExistenteException("J\u00e1 existe um produto com o c\u00f3digo de barras " + produto.getCodigoBarras());
        }
        produto.setEstado("Ativo");
        produto.setPrecoVenda(produto.calcularPrecoVenda());
        produtoDAO.save(produto.getIdProduto(), produto);
    }

    /**
     * Edita um Produto existente.
     *
     * @param produto o Produto a editar
     * @throws ProdutoInexistenteException se o Produto não existir.
     * @throws ProdutoInativoException se o Produto se encontrar inativo.
     * @throws ProdutoExistenteException se já existir um Produto com o novo código de barras.
     */
    @Override
    public void editarProduto(Produto produto) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException {
        Optional<Produto> original = produtoDAO.findById(produto.getIdProduto());
        if (original.isEmpty()) {
            throw new ProdutoInexistenteException("Produto com ID " + produto.getIdProduto() + " n\u00e3o existe.");
        }
        if (!original.get().isAtivo()) {
            throw new ProdutoInativoException("N\u00e3o \u00e9 poss\u00edvel editar um produto inativo.");
        }
        if (!original.get().getCodigoBarras().equals(produto.getCodigoBarras())) {
            Optional<Produto> existente = produtoDAO.findByCodigoBarras(produto.getCodigoBarras());
            if (existente.isPresent()) {
                throw new ProdutoExistenteException("O novo c\u00f3digo de barras j\u00e1 est\u00e1 em uso.");
            }
        }
        produto.setPrecoVenda(produto.calcularPrecoVenda());
        produtoDAO.save(produto.getIdProduto(), produto);
    }

    /**
     * Inativa um Produto.
     *
     * @param idProduto o identificador do Produto a inativar
     * @throws ProdutoInativoException se o Produto já se encontrar inativo.
     */
    @Override
    public void inativarProduto(String idProduto) throws ProdutoInativoException {
        Optional<Produto> p = produtoDAO.findById(idProduto);
        if (p.isPresent()) {
            if (!p.get().isAtivo()) {
                throw new ProdutoInativoException("O produto j\u00e1 se encontra inativo.");
            }
            p.get().setEstado("Descontinuado");
            produtoDAO.save(p.get().getIdProduto(), p.get());
        }
    }

    /**
     * Procura um Produto pelo seu código de barras.
     *
     * @param codigoBarras o código de barras a procurar
     * @return o Produto encontrado, ou nulo caso não seja encontrado.
     */
    @Override
    public Produto pesquisarPorCodigoBarras(String codigoBarras) {
        return produtoDAO.findByCodigoBarras(codigoBarras).orElse(null);
    }

    /**
     * Associa uma categoria a um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idCategoria o identificador da categoria
     */
    @Override
    public void associarCategoria(String idProduto, String idCategoria) {
        if (idProduto == null || idProduto.isBlank() || idCategoria == null || idCategoria.isBlank()) {
            throw new IllegalArgumentException("Produto e categoria são obrigatórios.");
        }
        if (!produtoDAO.exists(idProduto)) {
            throw new IllegalArgumentException("Produto inexistente.");
        }
        produtoDAO.addCategoria(idProduto, idCategoria);
    }

    /**
     * Remove uma categoria de um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idCategoria o identificador da categoria
     */
    @Override
    public void removerCategoria(String idProduto, String idCategoria) {
        if (idProduto == null || idProduto.isBlank() || idCategoria == null || idCategoria.isBlank()) {
            throw new IllegalArgumentException("Produto e categoria são obrigatórios.");
        }
        produtoDAO.removeCategoria(idProduto, idCategoria);
    }

    /**
     * Lista todos os Produtos por categoria.
     *
     * @param idCategoria o identificador da categoria
     * @return uma lista de Produtos na categoria
     */
    @Override
    public java.util.List<Produto> listarProdutosPorCategoria(String idCategoria) {
        return listarProdutos().stream()
                .filter(p -> produtoDAO.getCategorias(p.getIdProduto()).contains(idCategoria))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todos os Produtos.
     *
     * @return uma lista de todos os Produtos
     */
    @Override
    public java.util.List<Produto> listarProdutos() {
        return this.produtoDAO.findAll().stream()
                .filter(Produto::isAtivo)
                .collect(java.util.stream.Collectors.toList());
    }
}
