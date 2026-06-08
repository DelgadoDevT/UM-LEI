package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import java.util.List;

/**
 * Subsistema de Fornecimentos.
 * @author TakiLN Team
 * @since 1.0
 */
public class SubSistemaFornecimentos implements ISubSistemaFornecimentos {

    private final IFornecedorService fornecedorService;
    private final IEncomendaService encomendaService;
    private final IProdutoFornecedorService produtoFornecedorService;

    /**
     * Construtor para SubSistemaFornecimentos.
     * @param fornecedorDAO o DAO de fornecedor
     * @param encomendaDAO o DAO de encomenda
     * @param produtoDAO o DAO de produto
     * @param pfDAO o DAO de produto-fornecedor
     */
    public SubSistemaFornecimentos(FornecedorDAO fornecedorDAO, EncomendaDAO encomendaDAO,
                                   ProdutoDAO produtoDAO, ProdutoFornecedorDAO pfDAO) {
        this.fornecedorService      = new FornecedorService(fornecedorDAO);
        this.encomendaService       = new EncomendaService(encomendaDAO, fornecedorDAO, pfDAO);
        this.produtoFornecedorService = new ProdutoFornecedorService(fornecedorDAO, pfDAO);
    }

    // ── Sub-serviços ─────────────────────────────────────────────────────────
    /**
     * Obtém o serviço de fornecedor.
     * @return o serviço IFornecedorService
     */
    @Override public IFornecedorService getFornecedorService()           { return fornecedorService; }

    /**
     * Obtém o serviço de encomenda.
     * @return o serviço IEncomendaService
     */
    @Override public IEncomendaService getEncomendaService()             { return encomendaService; }

    /**
     * Obtém o serviço de produto-fornecedor.
     * @return o serviço IProdutoFornecedorService
     */
    @Override public IProdutoFornecedorService getProdutoFornecedorService() { return produtoFornecedorService; }

    // ── Fornecedores ─────────────────────────────────────────────────────────
    /**
     * Adiciona um fornecedor.
     * @param fornecedor o fornecedor
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     * @throws FornecedorExistenteException se o fornecedor já existir.
     */
    @Override
    public void adicionarFornecedor(Fornecedor fornecedor)
            throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException {
        fornecedorService.adicionarFornecedor(fornecedor);
    }

    /**
     * Edita um fornecedor.
     * @param fornecedor o fornecedor
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     * @throws FornecedorExistenteException se o fornecedor já existir.
     */
    @Override
    public void editarFornecedor(Fornecedor fornecedor)
            throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException {
        fornecedorService.editarFornecedor(fornecedor);
    }

    /**
     * Inativa um fornecedor.
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     */
    @Override
    public void inativarFornecedor(String idFornecedor) throws FornecedorInativoException {
        fornecedorService.inativarFornecedor(idFornecedor);
    }

    /**
     * Lista os fornecedores.
     * @return a lista
     */
    @Override
    public List<Fornecedor> listarFornecedores() {
        return fornecedorService.listarFornecedores();
    }

    /**
     * Procura fornecedores.
     * @param termo o termo de pesquisa
     * @return a lista
     */
    @Override
    public List<Fornecedor> pesquisarFornecedores(String termo) {
        return fornecedorService.pesquisarFornecedores(termo);
    }

    // ── Encomendas ────────────────────────────────────────────────────────────
    /**
     * Cria uma guia de encomenda.
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     * @param linhas as linhas da encomenda
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     */
    @Override
    public void criarGuia(String idEncomenda, String idFornecedor, String idLoja,
                          List<LinhaEncomenda> linhas)
            throws FornecedorInativoException, CamposObrigatoriosEmFaltaException {
        encomendaService.criarGuia(idEncomenda, idFornecedor, idLoja, linhas);
    }

    /**
     * Processa a transição de estado de uma encomenda.
     * @param idEncomenda o identificador da encomenda
     * @throws IllegalStateException se o estado for inválido.
     */
    @Override
    public void processarTransicaoEstado(String idEncomenda) throws IllegalStateException {
        encomendaService.processarTransicaoEstado(idEncomenda);
    }

    /**
     * Calcula o total da guia de encomenda.
     * @param idEncomenda o identificador da encomenda
     * @return o valor decimal
     */
    @Override
    public double calcularTotalGuia(String idEncomenda) {
        return encomendaService.calcularTotalGuia(idEncomenda);
    }

    /**
     * Lista as encomendas.
     * @return a lista
     */
    @Override
    public List<Encomenda> listarEncomendas() {
        return encomendaService.listarEncomendas();
    }

    // ── Produto-Fornecedor ────────────────────────────────────────────────────
    /**
     * Associa um produto a um fornecedor.
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     */
    @Override
    public void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto)
            throws FornecedorInativoException {
        produtoFornecedorService.associarProdutoAFornecedor(idProduto, idFornecedor, precoCusto);
    }

    /**
     * Remove a associação entre um produto e um fornecedor.
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     */
    @Override
    public void removerAssociacaoProdutoFornecedor(String idProduto, String idFornecedor) {
        produtoFornecedorService.removerAssociacao(idProduto, idFornecedor);
    }

    /**
     * Consulta os fornecedores de um produto.
     * @param idProduto o identificador do produto
     * @return a lista
     */
    @Override
    public List<ProdutoFornecedor> consultarFornecedoresDoProduto(String idProduto) {
        return produtoFornecedorService.consultarPorProduto(idProduto);
    }

    /**
     * Consulta os produtos de um fornecedor.
     * @param idFornecedor o identificador do fornecedor
     * @return a lista
     */
    @Override
    public List<ProdutoFornecedor> consultarProdutosDoFornecedor(String idFornecedor) {
        return produtoFornecedorService.consultarPorFornecedor(idFornecedor);
    }
}
