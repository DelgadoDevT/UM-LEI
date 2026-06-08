package pt.uminho.taki.ln;

import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.lojas.Loja;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.estatisticas.DatasInvalidasException;
import pt.uminho.taki.ln.estatisticas.DashboardKPIsDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioInventarioDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioVendasDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface que representa o Nó Lógico (Logical Node) Global Taki.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ITakiLNGlobal {
    // Lojas (CRUD)
    /**
     * Regista uma nova loja.
     * 
     * @param loja a loja a registar
     * @return a loja registada
     */
    Loja registarLoja(Loja loja);

    /**
     * Procura uma loja através do seu identificador.
     * 
     * @param idLoja o identificador da loja
     * @return um optional que contém a loja, se encontrada
     */
    Optional<Loja> buscarLoja(int idLoja);

    /**
     * Lista todas as lojas.
     * 
     * @return uma lista de lojas
     */
    List<Loja> listarLojas();

    /**
     * Atualiza uma loja existente.
     * 
     * @param loja a loja a atualizar
     * @return a loja atualizada
     */
    Loja atualizarLoja(Loja loja);

    /**
     * Elimina uma loja através do seu identificador.
     * 
     * @param idLoja o identificador da loja
     */
    void removerLoja(int idLoja);

    // Categorias e Produtos
    /**
     * Adiciona uma nova categoria.
     * 
     * @param categoria a categoria a adicionar
     */
    void adicionarCategoria(Categoria categoria);

    /**
     * Lista todas as categorias.
     * 
     * @return uma lista de categorias
     */
    List<Categoria> listarCategorias();

    /**
     * Adiciona um novo produto.
     * 
     * @param produto o produto a adicionar
     */
    void adicionarProduto(Produto produto);

    /**
     * Inativa um produto através do seu identificador.
     * 
     * @param idProduto o identificador do produto
     */
    void inativarProduto(String idProduto);

    /**
     * Procura um produto através do seu código de barras.
     * 
     * @param codigoBarras o código de barras
     * @return o produto encontrado
     */
    Produto pesquisarPorCodigoBarras(String codigoBarras);

    // Funcionarios
    /**
     * Regista um novo funcionário.
     * 
     * @param funcionario o funcionário a registar
     */
    void registarFuncionario(Funcionario funcionario);

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     */
    void bloquearConta(String idFuncionario);

    // Fornecimentos
    /**
     * Adiciona um novo fornecedor.
     * 
     * @param fornecedor o fornecedor a adicionar
     */
    void adicionarFornecedor(Fornecedor fornecedor);

    /**
     * Inativa um fornecedor através do identificador.
     * 
     * @param idFornecedor o identificador do fornecedor
     */
    void inativarFornecedor(String idFornecedor);

    // Estatisticas
    /**
     * Calcula o volume de vendas entre duas datas.
     * 
     * @param inicio a data de início
     * @param fim a data de fim
     * @return o volume total de vendas
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas
     */
    double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera um relatório de vendas.
     * 
     * @param inicio a data de início
     * @param fim a data de fim
     * @param idLoja o identificador da loja (pode ser nulo para global)
     * @param categoria a categoria (pode ser nulo para todas)
     * @return o DTO do relatório de vendas
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas
     */
    RelatorioVendasDTO gerarRelatorioVendas(LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria) throws DatasInvalidasException;

    /**
     * Gera um relatório de inventário.
     * 
     * @param idLoja o identificador da loja (pode ser nulo para global)
     * @return o DTO do relatório de inventário
     */
    RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja);

    /**
     * Calcula o ticket médio entre duas datas.
     * 
     * @param inicio a data de início
     * @param fim a data de fim
     * @return o ticket médio
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas
     */
    double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera os KPIs do painel de controlo (dashboard).
     * 
     * @param idLoja o identificador da loja (pode ser nulo para global)
     * @return o DTO dos KPIs do painel de controlo (dashboard)
     */
    DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja);

    // View
    /**
     * Atualiza a vista (view) global.
     */
    void atualizarView();

    // Sincronizacao Central
    /**
     * Processa a exportação de sincronização com o payload da loja.
     * 
     * @param payloadLoja o payload da loja
     */
    void processarSincronizacaoExportacao(String payloadLoja);

    /**
     * Processa a importação de sincronização e retorna o payload.
     * 
     * @return a cadeia de caracteres do payload de sincronização
     */
    String processarSincronizacaoImportacao();
}
