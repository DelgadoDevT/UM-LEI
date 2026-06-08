/*
 * NOTA DE COPYRIGHT (C) 2024.
 *
 * Esta obra prima pertence ao Grupo Taki.
 * Todas as diretivas de plágio são estritamente aplicadas.
 * Nenhuma parte desta obra pode ser reproduzida, distribuída, ou transmitida
 * por qualquer forma ou por qualquer meio, incluindo fotocópia, gravação, ou outros
 * métodos eletrónicos ou mecânicos, sem a prévia autorização por escrito do Grupo Taki,
 * exceto no caso de breves citações incorporadas em revisões críticas e outros usos
 * não comerciais permitidos pela lei de direitos de autor.
 */


package pt.uminho.taki.ln;

import pt.uminho.taki.ln.estatisticas.DashboardKPIsDTO;
import pt.uminho.taki.ln.estatisticas.DatasInvalidasException;
import pt.uminho.taki.ln.estatisticas.ISubSistemaEstatisticas;
import pt.uminho.taki.ln.estatisticas.RelatorioInventarioDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioVendasDTO;
import pt.uminho.taki.ln.fornecimentos.ISubSistemaFornecimentos;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.Loja;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.view.ISubSistemaView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A implementação da fachada da lógica de negócio global.
 * Esta classe é responsável por coordenar as operações que abrangem múltiplos subsistemas,
 * fornecendo uma interface única para a camada da API.
 *
 * @author TakiLN Team
 * @version 2024.05.02
 */
public class TakiLNGlobal implements ITakiLNGlobal {
    /** Subsistema responsável pela gestão global de lojas e catálogo de produtos. */
    private final ISubSistemaLojas subSistemaLojas;
    /** Subsistema responsável pela gestão de fornecedores a nível global. */
    private final ISubSistemaFornecimentos subSistemaFornecimentos;
    /** Subsistema responsável pelo cálculo de métricas e geração de relatórios consolidados. */
    private final ISubSistemaEstatisticas subSistemaEstatisticas;
    /** Subsistema responsável pela atualização das vistas de dados globais. */
    private final ISubSistemaView subSistemaView;
    
    public TakiLNGlobal(ISubSistemaLojas subSistemaLojas,
                        ISubSistemaFornecimentos subSistemaFornecimentos,
                        ISubSistemaEstatisticas subSistemaEstatisticas,
                        ISubSistemaView subSistemaView) {
        this.subSistemaLojas = subSistemaLojas;
        this.subSistemaFornecimentos = subSistemaFornecimentos;
        this.subSistemaEstatisticas = subSistemaEstatisticas;
        this.subSistemaView = subSistemaView;
    }

    // --- Lojas ---

    /**
     * Regista uma nova loja.
     * 
     * @param loja a loja a registar
     * @return a loja registada
     */
    @Override
    public Loja registarLoja(Loja loja) {
        return subSistemaLojas.registarLoja(loja);
    }

    /**
     * Procura uma loja pelo seu identificador.
     * 
     * @param idLoja o identificador da loja
     * @return um Optional que contém a loja, caso seja encontrada.
     */
    @Override
    public Optional<Loja> buscarLoja(int idLoja) {
        return subSistemaLojas.buscarLoja(idLoja);
    }

    /**
     * Lista todas as lojas.
     * 
     * @return uma lista de lojas
     */
    @Override
    public List<Loja> listarLojas() {
        List<Loja> lojas = subSistemaLojas.listarLojas();
        
        // Adicionar a loja "Global" como primeira opção virtual
        Loja global = new Loja();
        global.setIdLoja(0);
        global.setNome("Global (Todas as Lojas)");
        global.setDistrito("Sede");
        global.setRua("Edifício Central");
        
        lojas.add(0, global);
        return lojas;
    }

    /**
     * Atualiza uma loja existente.
     * 
     * @param loja a loja a atualizar
     * @return a loja atualizada
     */
    @Override
    public Loja atualizarLoja(Loja loja) {
        return subSistemaLojas.atualizarLoja(loja);
    }

    /**
     * Remove uma loja pelo seu identificador.
     * 
     * @param idLoja o identificador da loja
     */
    @Override
    public void removerLoja(int idLoja) {
        subSistemaLojas.removerLoja(idLoja);
    }

    // --- Categorias e Produtos ---

    /**
     * Adiciona uma nova categoria.
     * 
     * @param categoria a categoria a adicionar
     */
    @Override
    public void adicionarCategoria(Categoria categoria) {
        subSistemaLojas.adicionarCategoria(categoria);
    }

    /**
     * Lista todas as categorias.
     * 
     * @return uma lista de categorias
     */
    @Override
    public List<Categoria> listarCategorias() {
        return subSistemaLojas.listarCategorias();
    }

    /**
     * Adiciona um novo produto.
     * 
     * @param produto o produto a adicionar
     */
    @Override
    public void adicionarProduto(Produto produto) {
        subSistemaLojas.adicionarProduto(produto);
    }

    /**
     * Inativa um produto.
     * 
     * @param idProduto o identificador do produto
     */
    @Override
    public void inativarProduto(String idProduto) {
        subSistemaLojas.inativarProduto(idProduto);
    }

    /**
     * Procura um produto pelo seu código de barras.
     * 
     * @param codigoBarras o código de barras
     * @return o produto encontrado
     */
    @Override
    public Produto pesquisarPorCodigoBarras(String codigoBarras) {
        return subSistemaLojas.pesquisarPorCodigoBarras(codigoBarras);
    }

    /**
     * Regista um funcionário.
     * 
     * @param funcionario o funcionário a registar
     */
    @Override
    public void registarFuncionario(Funcionario funcionario) {
        subSistemaLojas.registarFuncionario(funcionario);
    }

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     */
    @Override
    public void bloquearConta(String idFuncionario) {
        subSistemaLojas.bloquearConta(idFuncionario);
    }

    // --- Fornecimentos ---

    /**
     * Adiciona um novo fornecedor.
     * 
     * @param fornecedor o fornecedor a adicionar
     */
    @Override
    public void adicionarFornecedor(Fornecedor fornecedor) {
        subSistemaFornecimentos.adicionarFornecedor(fornecedor);
    }

    /**
     * Inativa um fornecedor.
     * 
     * @param idFornecedor o identificador do fornecedor
     */
    @Override
    public void inativarFornecedor(String idFornecedor) {
        subSistemaFornecimentos.inativarFornecedor(idFornecedor);
    }

    // --- Estatisticas ---

    /**
     * Calcula o volume de vendas num determinado período.
     * 
     * @param inicio a data e hora de início
     * @param fim a data e hora de fim
     * @return o valor total das vendas
     * @throws DatasInvalidasException se as datas forem inválidas.
     */
    @Override
    public double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        return subSistemaEstatisticas.calcularVolumeVendas(inicio, fim);
    }

    /**
     * Gera um relatório de vendas.
     * 
     * @param inicio a data e hora de início
     * @param fim a data e hora de fim
     * @param idLoja o identificador da loja (opcional)
     * @param categoria a categoria (opcional)
     * @return o relatório de vendas gerado
     * @throws DatasInvalidasException se as datas forem inválidas.
     */
    @Override
    public RelatorioVendasDTO gerarRelatorioVendas(LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria) throws DatasInvalidasException {
        return subSistemaEstatisticas.gerarRelatorioVendas(inicio, fim, idLoja, categoria);
    }

    /**
     * Gera um relatório de inventário.
     * 
     * @param idLoja o identificador da loja
     * @return o relatório de inventário gerado
     */
    @Override
    public RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja) {
        return subSistemaEstatisticas.gerarRelatorioInventario(idLoja);
    }

    /**
     * Calcula o valor do ticket médio.
     * 
     * @param inicio a data e hora de início
     * @param fim a data e hora de fim
     * @return o valor do ticket médio
     * @throws DatasInvalidasException se as datas forem inválidas.
     */
    @Override
    public double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        return subSistemaEstatisticas.calcularTicketMedio(inicio, fim);
    }

    /**
     * Gera os indicadores de desempenho (KPI) do painel de controlo.
     * 
     * @param idLoja o identificador da loja
     * @return os KPIs do painel de controlo
     */
    @Override
    public DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja) {
        return subSistemaEstatisticas.gerarDashboardKPIs(idLoja);
    }

    // --- View ---

    /**
     * Atualiza a visualização.
     */
    @Override
    public void atualizarView() {
        subSistemaView.atualizarView();
    }

    // --- Sincronizacao ---

    /**
     * Processa uma exportação de sincronização.
     * 
     * @param payloadLoja os dados da loja
     */
    @Override
    public void processarSincronizacaoExportacao(String payloadLoja) {
        System.out.println("Nó central recebeu exportação: " + payloadLoja);
    }

    /**
     * Processa uma importação de sincronização.
     * 
     * @return a cadeia de caracteres de resultado
     */
    @Override
    public String processarSincronizacaoImportacao() {
        System.out.println("Nó central enviou importação de catálogo.");
        return "{\"status\":\"ok\",\"atualizacoes\":[]}";
    }

    public ISubSistemaLojas getSubSistemaLojas() {
        return this.subSistemaLojas;
    }
}
