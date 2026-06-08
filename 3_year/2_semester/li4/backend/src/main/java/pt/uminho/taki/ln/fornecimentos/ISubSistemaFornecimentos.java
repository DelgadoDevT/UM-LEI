package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import java.util.List;

/**
 * Interface do subsistema para a gestão de fornecimentos.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaFornecimentos {

    // ── Sub-serviços ─────────────────────────────────────────────────────────
    /**
     * Obtém o serviço de fornecedores.
     * 
     * @return o serviço de fornecedores
     */
    IFornecedorService getFornecedorService();

    /**
     * Obtém o serviço de encomendas.
     * 
     * @return o serviço de encomendas
     */
    IEncomendaService getEncomendaService();

    /**
     * Obtém o serviço de produtos-fornecedores.
     * 
     * @return o serviço de produtos-fornecedores
     */
    IProdutoFornecedorService getProdutoFornecedorService();

    // ── Fornecedores ─────────────────────────────────────────────────────────
    /**
     * Adiciona um novo fornecedor.
     * 
     * @param fornecedor o fornecedor a adicionar
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     * @throws FornecedorExistenteException se o fornecedor já existir
     */
    void adicionarFornecedor(Fornecedor fornecedor)
            throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException;

    /**
     * Edita um fornecedor existente.
     * 
     * @param fornecedor o fornecedor a editar
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     * @throws FornecedorExistenteException se o fornecedor já existir
     */
    void editarFornecedor(Fornecedor fornecedor)
            throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException;

    /**
     * Inativa um fornecedor através do identificador.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException se o fornecedor já estiver inativo
     */
    void inativarFornecedor(String idFornecedor)
            throws FornecedorInativoException;

    /**
     * Lista todos os fornecedores.
     * 
     * @return uma lista de fornecedores
     */
    List<Fornecedor> listarFornecedores();

    /**
     * Pesquisa fornecedores através de um termo de pesquisa.
     * 
     * @param termo o termo de pesquisa
     * @return uma lista de fornecedores correspondentes
     */
    List<Fornecedor> pesquisarFornecedores(String termo);

    // ── Encomendas ────────────────────────────────────────────────────────────
    /**
     * Cria uma guia de encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     * @param linhas as linhas da encomenda
     * @throws FornecedorInativoException se o fornecedor estiver inativo
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     */
    void criarGuia(String idEncomenda, String idFornecedor, String idLoja, List<LinhaEncomenda> linhas)
            throws FornecedorInativoException, CamposObrigatoriosEmFaltaException;

    /**
     * Processa a transição de estado para uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @throws IllegalStateException se a transição de estado for inválida
     */
    void processarTransicaoEstado(String idEncomenda)
            throws IllegalStateException;

    /**
     * Calcula o valor total de uma guia de encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @return o valor total
     */
    double calcularTotalGuia(String idEncomenda);

    /**
     * Lista todas as encomendas.
     * 
     * @return uma lista de encomendas
     */
    List<Encomenda> listarEncomendas();

    // ── Produto-Fornecedor ────────────────────────────────────────────────────
    /**
     * Associa um produto a um fornecedor.
     * 
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @throws FornecedorInativoException se o fornecedor estiver inativo
     */
    void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto)
            throws FornecedorInativoException;

    /**
     * Remove a associação entre um produto e um fornecedor.
     * 
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     */
    void removerAssociacaoProdutoFornecedor(String idProduto, String idFornecedor);

    /**
     * Consulta os fornecedores de um produto.
     * 
     * @param idProduto o identificador do produto
     * @return uma lista de associações produto-fornecedor
     */
    List<ProdutoFornecedor> consultarFornecedoresDoProduto(String idProduto);

    /**
     * Consulta os produtos de um fornecedor.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @return uma lista de associações produto-fornecedor
     */
    List<ProdutoFornecedor> consultarProdutosDoFornecedor(String idFornecedor);
}
