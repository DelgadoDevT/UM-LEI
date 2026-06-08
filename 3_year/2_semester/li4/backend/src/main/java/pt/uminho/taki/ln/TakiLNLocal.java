package pt.uminho.taki.ln;

import pt.uminho.taki.dao.CategoriaDAO;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FaturaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.LojaDAO;
import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.dao.PromocaoDAO;
import pt.uminho.taki.dao.StatisticsDAO;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.Encomenda;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.fornecimentos.ISubSistemaFornecimentos;
import pt.uminho.taki.ln.fornecimentos.LinhaEncomenda;
import pt.uminho.taki.ln.fornecimentos.SubSistemaFornecimentos;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.inventario.ISubSistemaInventario;
import pt.uminho.taki.ln.inventario.MovimentoInventario;
import pt.uminho.taki.ln.inventario.SubSistemaInventario;
import pt.uminho.taki.ln.inventario.exceptions.ArtigoNaoEncontradoException;
import pt.uminho.taki.ln.inventario.exceptions.DataInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.MotivoObrigatorioException;
import pt.uminho.taki.ln.inventario.exceptions.QuantidadeInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.lojas.exceptions.CategoriaInvalidaException;
import pt.uminho.taki.ln.lojas.exceptions.ContaBloqueadaException;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.PerfilAcesso;
import pt.uminho.taki.ln.lojas.Permissao;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;
import pt.uminho.taki.ln.lojas.SubSistemaLojas;
import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;
import pt.uminho.taki.ln.sincronizacao.ISubSistemaSincronizacao;
import pt.uminho.taki.ln.sincronizacao.SubSistemaSincronizacao;
import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;
import pt.uminho.taki.ln.vendas.ISubSistemaVendas;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.vendas.MetodoPagamentoIndisponivelException;
import pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;
import pt.uminho.taki.ln.vendas.Promocao;
import pt.uminho.taki.ln.vendas.SubSistemaVendas;
import pt.uminho.taki.ln.vendas.Venda;

import java.util.*;

/**
 * Implementação da interface de Lógica de Negócio Local.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class TakiLNLocal implements ITakiLNLocal {

    /** Subsistema responsável pela gestão de funcionários, lojas, categorias e produtos. */
    private final ISubSistemaLojas subSistemaLojas;
    /** Subsistema responsável pela gestão de fornecedores, encomendas e relações produto-fornecedor. */
    private final ISubSistemaFornecimentos subSistemaFornecimentos;
    /** Subsistema responsável pelo controlo de stock e movimentos de inventário. */
    private final ISubSistemaInventario subSistemaInventario;
    /** Subsistema responsável pela orquestração de vendas, devoluções e promoções. */
    private final ISubSistemaVendas subSistemaVendas;
    /** Subsistema responsável pela comunicação e sincronização com o servidor central. */
    private final ISubSistemaSincronizacao subSistemaSincronizacao;
    /** Objeto de acesso a dados para operações de persistência de inventário. */
    private final InventarioDAO inventarioDAO;
    /** Objeto de acesso a dados para operações de persistência de produtos. */
    private final ProdutoDAO produtoDAO;
    /** Serviço responsável pela emissão de faturas e integridade SAF-T. */
    private final pt.uminho.taki.ln.fatura.IFaturaService faturaService;

    /**
     * Constrói uma nova instância de TakiLNLocal.
     */
    public TakiLNLocal() {
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        LojaDAO lojaDAO = new LojaDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        InventarioDAO inventarioDAO = new InventarioDAO();
        StatisticsDAO statisticsDAO = new StatisticsDAO();
        FornecedorDAO fornecedorDAO = new FornecedorDAO();
        EncomendaDAO encomendaDAO = new EncomendaDAO();
        ProdutoFornecedorDAO pfDAO = new ProdutoFornecedorDAO();
        VendaDAO vendaDAO = new VendaDAO();
        FaturaDAO faturaDAO = new FaturaDAO();
        PromocaoDAO promocaoDAO = new PromocaoDAO();
        OutboxDAO outboxDAO = new OutboxDAO();

        this.subSistemaLojas = new SubSistemaLojas(funcionarioDAO, lojaDAO, categoriaDAO, produtoDAO);
        this.subSistemaInventario = new SubSistemaInventario(inventarioDAO, statisticsDAO);
        this.subSistemaFornecimentos = new SubSistemaFornecimentos(fornecedorDAO, encomendaDAO, produtoDAO, pfDAO);
        this.subSistemaVendas = new SubSistemaVendas(this.subSistemaLojas, this.subSistemaInventario, vendaDAO, promocaoDAO);
        this.subSistemaSincronizacao = new SubSistemaSincronizacao(outboxDAO);
        this.inventarioDAO = inventarioDAO;
        this.produtoDAO = produtoDAO;
        this.faturaService = new pt.uminho.taki.ln.fatura.FaturaService(faturaDAO, vendaDAO);

        // Link EncomendaService with InventarioService
        this.subSistemaFornecimentos.getEncomendaService().setInventarioService(this.subSistemaInventario.getInventarioService());

        if (this.subSistemaVendas.getVendaService() instanceof pt.uminho.taki.ln.vendas.VendaService) {
            ((pt.uminho.taki.ln.vendas.VendaService) this.subSistemaVendas.getVendaService())
                .adicionarObserver((pt.uminho.taki.ln.vendas.IVendaObserver) this.faturaService);
        }
    }

    /**
     * Construtor de visibilidade de pacote, utilizado primordialmente para testes de integração 
     * e injeção de dependências controlada (mocks).
     *
     * @param subSistemaLojas o subsistema de lojas
     * @param subSistemaFornecimentos o subsistema de fornecimentos
     * @param subSistemaInventario o subsistema de inventário
     * @param subSistemaVendas o subsistema de vendas
     * @param subSistemaSincronizacao o subsistema de sincronização
     */
    TakiLNLocal(ISubSistemaLojas subSistemaLojas,
                ISubSistemaFornecimentos subSistemaFornecimentos,
                ISubSistemaInventario subSistemaInventario,
                ISubSistemaVendas subSistemaVendas,
                ISubSistemaSincronizacao subSistemaSincronizacao) {
        this.subSistemaLojas = subSistemaLojas;
        this.subSistemaFornecimentos = subSistemaFornecimentos;
        this.subSistemaInventario = subSistemaInventario;
        this.subSistemaVendas = subSistemaVendas;
        this.subSistemaSincronizacao = subSistemaSincronizacao;
        this.inventarioDAO = new InventarioDAO();
        this.produtoDAO = new ProdutoDAO();
        this.faturaService = new pt.uminho.taki.ln.fatura.FaturaService();

        // Link EncomendaService with InventarioService
        this.subSistemaFornecimentos.getEncomendaService().setInventarioService(this.subSistemaInventario.getInventarioService());

        if (this.subSistemaVendas.getVendaService() instanceof pt.uminho.taki.ln.vendas.VendaService) {
            ((pt.uminho.taki.ln.vendas.VendaService) this.subSistemaVendas.getVendaService())
                .adicionarObserver((pt.uminho.taki.ln.vendas.IVendaObserver) this.faturaService);
        }
    }

    /**
     * Autentica um funcionário.
     * 
     * @param email o e-mail
     * @param password a palavra-passe
     * @return o funcionário autenticado
     * @throws ContaBloqueadaException se a conta se encontrar bloqueada.
     * @throws CredenciaisInvalidasException se as credenciais forem inválidas.
     */
    @Override
    public Funcionario autenticar(String email, String password) throws ContaBloqueadaException, CredenciaisInvalidasException {
        return this.subSistemaLojas.getFuncionarioService().autenticar(email, password);
    }

    /**
     * Regista um funcionário.
     * 
     * @param funcionario o funcionário
     * @throws EmailJaExisteException se o e-mail já existir.
     * @throws PasswordFracaException se a palavra-passe for fraca.
     */
    @Override
    public void registarFuncionario(Funcionario funcionario) throws EmailJaExisteException, PasswordFracaException {
        this.subSistemaLojas.getFuncionarioService().registarFuncionario(funcionario);
    }

    /**
     * Adiciona um novo produto.
     * 
     * @param produto o produto
     * @throws ProdutoExistenteException se o produto já existir.
     */
    @Override
    public void adicionarProduto(Produto produto) throws ProdutoExistenteException {
        this.subSistemaLojas.getProdutoService().adicionarProduto(produto);
    }

    /**
     * Edita um produto.
     * 
     * @param produto o produto
     * @throws ProdutoInexistenteException se o produto não existir.
     * @throws ProdutoInativoException se o produto se encontrar inativo.
     * @throws ProdutoExistenteException se o produto já existir.
     */
    @Override
    public void editarProduto(Produto produto) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException {
        this.subSistemaLojas.getProdutoService().editarProduto(produto);
    }

    /**
     * Inativa um produto.
     * 
     * @param idProduto o identificador do produto
     * @throws ProdutoInativoException se o produto se encontrar inativo.
     */
    @Override
    public void inativarProduto(String idProduto) throws ProdutoInativoException {
        this.subSistemaLojas.getProdutoService().inativarProduto(idProduto);
    }

    /**
     * Adiciona uma nova categoria.
     * 
     * @param categoria a categoria
     * @throws CategoriaInvalidaException se a categoria for inválida.
     */
    @Override
    public void adicionarCategoria(Categoria categoria) throws CategoriaInvalidaException {
        this.subSistemaLojas.getCategoriaService().adicionarCategoria(categoria);
    }

    /**
     * Edita uma categoria.
     * 
     * @param categoria a categoria
     * @throws CategoriaInvalidaException se a categoria for inválida.
     */
    @Override
    public void editarCategoria(Categoria categoria) throws CategoriaInvalidaException {
        this.subSistemaLojas.getCategoriaService().editarCategoria(categoria);
    }

    /**
     * Inativa uma categoria.
     * 
     * @param idCategoria o identificador da categoria
     * @throws CategoriaInvalidaException se a categoria for inválida.
     */
    @Override
    public void inativarCategoria(String idCategoria) throws CategoriaInvalidaException {
        this.subSistemaLojas.getCategoriaService().inativarCategoria(idCategoria);
    }

    /**
     * Lista todos os produtos.
     * 
     * @return uma lista de produtos
     */
    @Override
    public List<Produto> listarProdutos() {
        return Collections.unmodifiableList(this.subSistemaLojas.getProdutoService().listarProdutos());
    }

    /**
     * Lista todos os produtos de uma loja.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de produtos
     */
    @Override
    public List<Produto> listarProdutos(Integer idLoja) {
        if (idLoja == null) return listarProdutos();
        
        List<String> produtosIds = this.inventarioDAO.findAll().stream()
                .filter(i -> i.getIdLoja() == idLoja)
                .map(Inventario::getIdProduto)
                .toList();
        
        return this.subSistemaLojas.getProdutoService().listarProdutos().stream()
                .filter(p -> produtosIds.contains(p.getIdProduto()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todas as categorias.
     * 
     * @return uma lista de categorias
     */
    @Override
    public List<Categoria> listarCategorias() {
        return Collections.unmodifiableList(this.subSistemaLojas.getCategoriaService().listarCategorias());
    }

    /**
     * Lista as categorias de um produto.
     * 
     * @param idProduto o identificador do produto
     * @return uma lista de categorias
     */
    @Override
    public List<String> listarCategoriasDeProduto(String idProduto) {
        return new ArrayList<>(this.produtoDAO.getCategorias(idProduto));
    }

    /**
     * Lista todos os funcionários.
     * 
     * @return uma lista de funcionários
     */
    @Override
    public List<Funcionario> listarFuncionarios() {
        return Collections.unmodifiableList(this.subSistemaLojas.getFuncionarioService().listarFuncionarios());
    }

    /**
     * Procura um funcionário pelo seu identificador.
     * 
     * @param id o identificador do funcionário
     * @return um Optional que contém o funcionário, caso seja encontrado.
     */
    @Override
    public java.util.Optional<Funcionario> buscarFuncionarioPorId(String id) {
        return this.subSistemaLojas.getFuncionarioService().listarFuncionarios().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    /**
     * Regista um movimento de inventário manual.
     * 
     * @param movimento o movimento
     * @throws QuantidadeInvalidaException se a quantidade for inválida.
     * @throws DataInvalidaException se a data for inválida.
     * @throws MotivoObrigatorioException se o motivo não for fornecido.
     * @throws StockInsuficienteException se o stock for insuficiente.
     * @throws ArtigoNaoEncontradoException se o artigo não for encontrado.
     */
    @Override
    public void registarMovimentoManual(MovimentoInventario movimento)
            throws QuantidadeInvalidaException, DataInvalidaException, MotivoObrigatorioException, StockInsuficienteException, ArtigoNaoEncontradoException {
        this.subSistemaInventario.getInventarioService().registarMovimentoManual(movimento);
    }

    /**
     * Define o limite de segurança para um item de inventário.
     * 
     * @param idInventario o identificador de inventário
     * @param novoLimite o novo limite
     * @throws ArtigoNaoEncontradoException se o artigo não for encontrado.
     */
    @Override
    public void definirLimiteSeguranca(String idInventario, double novoLimite) throws ArtigoNaoEncontradoException {
        this.subSistemaInventario.getInventarioService().definirLimiteSeguranca(idInventario, novoLimite);
    }

    /**
     * Atualiza um artigo com informação de stock.
     * 
     * @param produto o produto
     * @param stock a quantidade de stock
     * @param minStock o stock mínimo
     * @param idFuncionarioAutenticado o identificador do funcionário autenticado
     * @throws ProdutoInexistenteException se o produto não existir.
     * @throws ProdutoInativoException se o produto se encontrar inativo.
     * @throws ProdutoExistenteException se o produto já existir.
     */
    @Override
    public void atualizarArtigoComStock(Produto produto, Double stock, Double minStock, String idFuncionarioAutenticado)
            throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException {
        // 1. Atualizar dados base do produto
        this.subSistemaLojas.getProdutoService().editarProduto(produto);

        // 2. Atualizar ou Criar inventário se stock/minStock fornecidos
        if (stock != null || minStock != null) {
            java.util.Optional<Inventario> invOpt = this.inventarioDAO.findAll().stream()
                    .filter(i -> i.getIdProduto().equals(produto.getIdProduto()))
                    .findFirst();

            Inventario inv;
            if (invOpt.isPresent()) {
                inv = invOpt.get();
                if (minStock != null) {
                    try {
                        this.subSistemaInventario.getInventarioService().definirLimiteSeguranca(inv.getId(), minStock);
                    } catch (ArtigoNaoEncontradoException e) {
                        throw new RuntimeException("Falha ao atualizar limite de segurança.", e);
                    }
                }
            } else {
                // Criar novo registo de inventário (assume-se loja do funcionário ou loja 1 como default)
                int idLoja = 1;
                java.util.Optional<Funcionario> f = buscarFuncionarioPorId(idFuncionarioAutenticado);
                if (f.isPresent()) idLoja = f.get().getIdLoja();

                inv = new Inventario();
                inv.setId(java.util.UUID.randomUUID().toString());
                inv.setIdProduto(produto.getIdProduto());
                inv.setIdLoja(idLoja);
                inv.setQuantidade(0.0); // Começa a 0 e ajusta com movimento
                inv.setQuantidadeMinima(minStock != null ? minStock : 0.0);
                this.inventarioDAO.save(inv.getId(), inv);
            }

            if (stock != null && !java.util.Objects.equals(stock, inv.getQuantidade())) {
                double diff = stock - inv.getQuantidade();
                MovimentoInventario mov = new MovimentoInventario();
                mov.setId(java.util.UUID.randomUUID().toString());
                mov.setTipo(diff > 0 ? pt.uminho.taki.ln.inventario.TipoMovimento.ENTRADA : pt.uminho.taki.ln.inventario.TipoMovimento.SAIDA);
                mov.setQuantidade(Math.abs(diff));
                mov.setDataRegisto(java.time.LocalDateTime.now());
                mov.setMotivo("Ajuste manual (Edição/Criação de produto)");
                mov.setIdInventario(inv.getId());
                mov.setIdFuncionario(idFuncionarioAutenticado);
                try {
                    this.subSistemaInventario.getInventarioService().registarMovimentoManual(mov);
                } catch (Exception e) {
                    throw new RuntimeException("Falha ao ajustar stock do produto.", e);
                }
            }
        }
    }

    /**
     * Associa um produto a um fornecedor.
     * 
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     */
    @Override
    public void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto) throws FornecedorInativoException {
        this.subSistemaFornecimentos.getProdutoFornecedorService().associarProdutoAFornecedor(idProduto, idFornecedor, precoCusto);
    }

    /**
     * Adiciona um novo fornecedor.
     * 
     * @param fornecedor o fornecedor
     * @throws FornecedorExistenteException se o fornecedor já existir.
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     */
    @Override
    public void adicionarFornecedor(Fornecedor fornecedor) throws FornecedorExistenteException, CamposObrigatoriosEmFaltaException {
        this.subSistemaFornecimentos.getFornecedorService().adicionarFornecedor(fornecedor);
    }

    /**
     * Edita um fornecedor.
     * 
     * @param fornecedor o fornecedor
     * @throws FornecedorExistenteException se o fornecedor já existir.
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     */
    @Override
    public void editarFornecedor(Fornecedor fornecedor) throws FornecedorExistenteException, CamposObrigatoriosEmFaltaException {
        this.subSistemaFornecimentos.getFornecedorService().editarFornecedor(fornecedor);
    }

    /**
     * Inativa um fornecedor.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     */
    @Override
    public void inativarFornecedor(String idFornecedor) throws FornecedorInativoException {
        this.subSistemaFornecimentos.getFornecedorService().inativarFornecedor(idFornecedor);
    }

    /**
     * Lista todos os fornecedores.
     * 
     * @return uma lista de fornecedores
     */
    @Override
    public List<Fornecedor> listarFornecedores() {
        return Collections.unmodifiableList(this.subSistemaFornecimentos.listarFornecedores());
    }

    /**
     * Lista os fornecedores de um produto.
     * 
     * @param idProduto o identificador do produto
     * @return uma lista de identificadores de fornecedor
     */
    @Override
    public List<String> listarFornecedoresDeProduto(String idProduto) {
        return this.subSistemaFornecimentos.consultarFornecedoresDoProduto(idProduto).stream()
                .map(pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor::getIdFornecedor)
                .toList();
    }

    /**
     * Consulta os produtos de um fornecedor.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @return uma lista de relações entre produto e fornecedor
     */
    @Override
    public List<pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor> consultarProdutosDoFornecedor(String idFornecedor) {
        return this.subSistemaFornecimentos.consultarProdutosDoFornecedor(idFornecedor);
    }

    /**
     * Cria uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     * @param linhas as linhas da encomenda
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta.
     */
    @Override
    public void criarEncomenda(String idEncomenda, String idFornecedor, String idLoja, List<LinhaEncomenda> linhas)
            throws FornecedorInativoException, CamposObrigatoriosEmFaltaException {
        this.subSistemaFornecimentos.criarGuia(idEncomenda, idFornecedor, idLoja, linhas);
    }

    /**
     * Lista todas as encomendas.
     * 
     * @return uma lista de encomendas
     */
    @Override
    public List<Encomenda> listarEncomendas() {
        return Collections.unmodifiableList(this.subSistemaFornecimentos.listarEncomendas());
    }

    /**
     * Processa a transição de estado de uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     */
    @Override
    public void processarTransicaoEstado(String idEncomenda) {
        this.subSistemaFornecimentos.processarTransicaoEstado(idEncomenda);
    }

    /**
     * Faz avançar o pipeline de abastecimento de uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     */
    @Override
    public void avancarPipelineFornecimento(String idEncomenda) {
        // Pipeline de Abastecimento: transição de estado orquestrada via interface
        this.subSistemaFornecimentos.processarTransicaoEstado(idEncomenda);
    }

    /**
     * Inicia uma nova venda.
     * 
     * @param idLoja o identificador da loja
     * @param idFuncionario o identificador do funcionário
     * @return a venda iniciada
     */
    @Override
    public Venda iniciarVenda(int idLoja, String idFuncionario) {
        if (idLoja <= 0) {
            throw new IllegalArgumentException("Não é possível iniciar vendas em modo Global ou Loja inválida.");
        }
        return this.subSistemaVendas.getVendaService().iniciarVenda(idLoja, idFuncionario);
    }

    /**
     * Adiciona uma linha de venda a uma venda.
     * 
     * @param venda a venda
     * @param produto o produto
     * @param quantidade a quantidade
     */
    @Override
    public void adicionarLinhaVenda(Venda venda, Produto produto, int quantidade) {
        this.subSistemaVendas.getVendaService().adicionarLinha(venda, produto, quantidade);
    }

    /**
     * Regista uma venda com um método de pagamento específico.
     * 
     * @param venda a venda
     * @param metodoPagamento o método de pagamento
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento não se encontrar disponível.
     */
    @Override
    public void registarVenda(Venda venda, String metodoPagamento) throws MetodoPagamentoIndisponivelException {
        this.subSistemaVendas.getVendaService().processarVenda(venda, metodoPagamento);
    }

    /**
     * Regista uma venda com um método de pagamento específico e valor entregue.
     * 
     * @param venda a venda
     * @param metodoPagamento o método de pagamento
     * @param valorEntregue o valor entregue
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento não se encontrar disponível.
     */
    @Override
    public void registarVenda(Venda venda, String metodoPagamento, double valorEntregue) throws MetodoPagamentoIndisponivelException {
        this.subSistemaVendas.getVendaService().processarVenda(venda, metodoPagamento, valorEntregue);
    }

    /**
     * Lista todas as vendas.
     * 
     * @return uma lista de vendas
     */
    @Override
    public List<Venda> listarVendas() {
        return Collections.unmodifiableList(this.subSistemaVendas.getVendaService().listarVendas());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<pt.uminho.taki.ln.vendas.Devolucao> listarDevolucoes() {
        return Collections.unmodifiableList(this.subSistemaVendas.getDevolucaoService().listarDevolucoes());
    }

    /**
     * Processa uma devolução.
     * 
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução tiver sido excedido.
     */
    @Override
    public void processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException {
        this.subSistemaVendas.getDevolucaoService().processarDevolucao(vendaOriginal, linhasADevolver);
    }

    /**
     * Lista todas as promoções ativas.
     * 
     * @return uma lista de promoções ativas
     */
    @Override
    public List<Promocao> listarPromocoesAtivas() {
        return Collections.unmodifiableList(this.subSistemaVendas.getPromocaoService().getPromocoesAtivas());
    }

    /**
     * Adiciona uma nova promoção.
     * 
     * @param promocao a promoção
     */
    @Override
    public void adicionarPromocao(Promocao promocao) {
        this.subSistemaVendas.getPromocaoService().adicionarPromocao(promocao);
    }

    /**
     * Cancela uma promoção.
     * 
     * @param idPromocao o identificador da promoção
     * @param motivo o motivo do cancelamento
     */
    @Override
    public void cancelarPromocao(String idPromocao, String motivo) {
        this.subSistemaVendas.getPromocaoService().cancelarPromocao(idPromocao, motivo);
    }

    /**
     * Atualiza um funcionário.
     * 
     * @param funcionario o funcionário
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado.
     * @throws EmailJaExisteException se o e-mail já existir.
     * @throws PasswordFracaException se a palavra-passe for fraca.
     */
    @Override
    public void atualizarFuncionario(Funcionario funcionario) throws FuncionarioNaoEncontradoException, EmailJaExisteException, PasswordFracaException {
        this.subSistemaLojas.getFuncionarioService().atualizarFuncionario(funcionario);
    }

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado.
     */
    @Override
    public void bloquearConta(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        this.subSistemaLojas.getFuncionarioService().bloquearConta(
                idFuncionario,
                "Bloqueio administrativo",
                idAdministradorSessao,
                passwordAdministrador
        );
    }

    /**
     * Remove logicamente a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado.
     */
    @Override
    public void removerContaLogicamente(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        this.subSistemaLojas.getFuncionarioService().removerContaLogicamente(idFuncionario, idAdministradorSessao, passwordAdministrador);
    }

    /**
     * Atribui um perfil a um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param nomePerfil o nome do perfil
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado.
     */
    @Override
    public void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministradorSessao) throws FuncionarioNaoEncontradoException {
        this.subSistemaLojas.getFuncionarioService().atribuirPerfil(idFuncionario, nomePerfil, idAdministradorSessao);
    }

    /**
     * Regista um novo perfil.
     * 
     * @param perfil o perfil
     * @throws PerfilDuplicadoException se o perfil se encontrar duplicado.
     */
    @Override
    public void registarPerfil(PerfilAcesso perfil) throws PerfilDuplicadoException {
        this.subSistemaLojas.getPerfilAcessoService().registarPerfil(perfil);
    }

    /**
     * Edita um perfil existente.
     * 
     * @param nomePerfil o nome do perfil
     * @param permissoes a nova lista de permissões
     */
    @Override
    public void editarPerfil(String nomePerfil, List<Permissao> permissoes) {
        this.subSistemaLojas.getPerfilAcessoService().editarPerfil(nomePerfil, permissoes);
    }

    /**
     * Lista todos os perfis.
     * 
     * @return uma lista de perfis
     */
    @Override
    public List<PerfilAcesso> listarPerfis() {
        return Collections.unmodifiableList(this.subSistemaLojas.getPerfilAcessoService().listarPerfis());
    }

    /**
     * Gera um relatório mensal SAF-T.
     * 
     * @param ano o ano
     * @param mes o mês
     * @return a cadeia de caracteres SAF-T gerada
     */
    @Override
    public String gerarSaftMensal(int ano, int mes) {
        java.time.LocalDate inicio = java.time.LocalDate.of(ano, mes, 1);
        java.time.LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return this.faturaService.exportarSaftPt(inicio, fim);
    }

    /**
     * Gera uma segunda via em PDF de uma fatura.
     * 
     * @param numFatura o número da fatura
     * @return o PDF gerado como um array de bytes
     */
    @Override
    public byte[] gerarSegundaViaPDF(String numFatura) {
        return this.faturaService.gerarFaturaPDF(numFatura);
    }

    /**
     * Lista o inventário.
     * 
     * @return uma lista de itens de inventário
     */
    @Override
    public List<Inventario> listarInventario() {
        return Collections.unmodifiableList(new ArrayList<>(this.inventarioDAO.findAll()));
    }

    /**
     * Obtém os indicadores de desempenho (KPI) gerais.
     * 
     * @param idLoja o identificador da loja
     * @return um mapa de KPIs
     */
    @Override
    public Map<String, Object> obterKpiGerais(int idLoja) {
        return this.subSistemaInventario.getStatisticsService().getKpiGerais(idLoja);
    }

    /**
     * Obtém as vendas mensais.
     * 
     * @param idLoja o identificador da loja
     * @param meses o número de meses
     * @return uma lista de vendas mensais
     */
    @Override
    public List<Map<String, Object>> obterVendasMensais(int idLoja, int meses) {
        return this.subSistemaInventario.getStatisticsService().getVendasMensais(idLoja, meses);
    }

    /**
     * Obtém as vendas por hora.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de vendas por hora
     */
    @Override
    public List<Map<String, Object>> obterVendasPorHora(int idLoja) {
        return this.subSistemaInventario.getStatisticsService().getVendasPorHora(idLoja);
    }

    /**
     * Obtém as vendas por categoria.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de vendas por categoria
     */
    @Override
    public List<Map<String, Object>> obterVendasPorCategoria(int idLoja) {
        return this.subSistemaInventario.getStatisticsService().getVendasPorCategoria(idLoja);
    }

    /**
     * Sincroniza os dados com o servidor central.
     * 
     * @throws FalhaSincronizacaoException se a sincronização falhar.
     */
    @Override
    public void sincronizarDados() throws FalhaSincronizacaoException {
        // Mock success for development/demo
        System.out.println("Sincronização mockada iniciada...");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        System.out.println("Sincronização mockada concluída.");
    }

    /**
     * Verifica a disponibilidade do servidor central.
     * 
     * @return verdadeiro se estiver disponível, falso caso contrário.
     */
    @Override
    public boolean verificarDisponibilidadeCentral() {
        return this.subSistemaSincronizacao.getSincronizacaoService().verificarDisponibilidadeCentral();
    }

    /**
     * Obtém a percentagem de disponibilidade do servidor central.
     * 
     * @return a percentagem de disponibilidade
     */
    @Override
    public double obterDisponibilidadeCentral() {
        return this.subSistemaSincronizacao.obterDisponibilidadeCentral();
    }
}
