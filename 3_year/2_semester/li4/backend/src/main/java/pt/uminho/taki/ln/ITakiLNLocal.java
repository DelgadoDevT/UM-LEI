package pt.uminho.taki.ln;

import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.PerfilAcesso;
import pt.uminho.taki.ln.lojas.Permissao;
import pt.uminho.taki.ln.lojas.exceptions.*;
import pt.uminho.taki.ln.inventario.MovimentoInventario;
import pt.uminho.taki.ln.inventario.exceptions.*;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.Encomenda;
import pt.uminho.taki.ln.fornecimentos.LinhaEncomenda;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.vendas.Promocao;
import pt.uminho.taki.ln.vendas.MetodoPagamentoIndisponivelException;
import pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;
import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;
import java.util.List;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;

/**
 * Interface que representa o Nó Lógico (Logical Node) Local Taki.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ITakiLNLocal {
    // SubSistemaLojas
    /**
     * Autentica um funcionário.
     * 
     * @param email o correio eletrónico do funcionário
     * @param password a palavra-passe do funcionário
     * @return o funcionário autenticado
     * @throws ContaBloqueadaException se a conta estiver bloqueada
     * @throws CredenciaisInvalidasException se as credenciais forem inválidas
     */
    Funcionario autenticar(String email, String password) throws ContaBloqueadaException, CredenciaisInvalidasException;

    /**
     * Regista um novo funcionário.
     * 
     * @param funcionario o funcionário a registar
     * @throws EmailJaExisteException se o correio eletrónico já existir
     * @throws PasswordFracaException se a palavra-passe for demasiado fraca
     */
    void registarFuncionario(Funcionario funcionario) throws EmailJaExisteException, PasswordFracaException;

    /**
     * Adiciona um novo produto.
     * 
     * @param produto o produto a adicionar
     * @throws ProdutoExistenteException se o produto já existir
     */
    void adicionarProduto(Produto produto) throws ProdutoExistenteException;

    /**
     * Edita um produto existente.
     * 
     * @param produto o produto a editar
     * @throws ProdutoInexistenteException se o produto não existir
     * @throws ProdutoInativoException se o produto estiver inativo
     * @throws ProdutoExistenteException se o produto entrar em conflito com um existente
     */
    void editarProduto(Produto produto) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException;

    /**
     * Inativa um produto através do seu identificador.
     * 
     * @param idProduto o identificador do produto
     * @throws ProdutoInativoException se o produto estiver inativo
     */
    void inativarProduto(String idProduto) throws ProdutoInativoException;

    /**
     * Adiciona uma nova categoria.
     * 
     * @param categoria a categoria a adicionar
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    void adicionarCategoria(Categoria categoria) throws CategoriaInvalidaException;

    /**
     * Edita uma categoria existente.
     * 
     * @param categoria a categoria a editar
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    void editarCategoria(Categoria categoria) throws CategoriaInvalidaException;

    /**
     * Inativa uma categoria através do seu identificador.
     * 
     * @param idCategoria o identificador da categoria
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    void inativarCategoria(String idCategoria) throws CategoriaInvalidaException;

    /**
     * Lista todos os produtos.
     * 
     * @return uma lista de produtos
     */
    List<Produto> listarProdutos();

    /**
     * Lista os produtos de uma loja específica.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de produtos da loja
     */
    List<Produto> listarProdutos(Integer idLoja);

    /**
     * Lista todas as categorias.
     * 
     * @return uma lista de categorias
     */
    List<Categoria> listarCategorias();

    /**
     * Lista as categorias associadas a um produto.
     * 
     * @param idProduto o identificador do produto
     * @return uma lista de nomes de categorias
     */
    List<String> listarCategoriasDeProduto(String idProduto);

    /**
     * Lista todos os funcionários.
     * 
     * @return uma lista de funcionários
     */
    List<Funcionario> listarFuncionarios();

    /**
     * Procura um funcionário através do identificador.
     * 
     * @param id o identificador do funcionário
     * @return um optional que contém o funcionário, se encontrado
     */
    java.util.Optional<Funcionario> buscarFuncionarioPorId(String id);

    // SubSistemaInventario
    /**
     * Regista um movimento manual de inventário.
     * 
     * @param movimento o movimento a registar
     * @throws QuantidadeInvalidaException se a quantidade for inválida
     * @throws DataInvalidaException se a data for inválida
     * @throws MotivoObrigatorioException se o motivo estiver em falta
     * @throws StockInsuficienteException se o stock for insuficiente
     * @throws ArtigoNaoEncontradoException se o artigo não for encontrado
     */
    void registarMovimentoManual(MovimentoInventario movimento) throws QuantidadeInvalidaException, DataInvalidaException, MotivoObrigatorioException, StockInsuficienteException, ArtigoNaoEncontradoException;

    /**
     * Atualiza um artigo com stock.
     * 
     * @param produto o produto
     * @param stock a quantidade de stock
     * @param minStock o limite mínimo de stock
     * @param idFuncionarioAutenticado o identificador do funcionário autenticado
     * @throws ProdutoInexistenteException se o produto não existir
     * @throws ProdutoInativoException se o produto estiver inativo
     * @throws ProdutoExistenteException se o produto entrar em conflito com um existente
     */
    void atualizarArtigoComStock(Produto produto, Double stock, Double minStock, String idFuncionarioAutenticado) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException;

    // SubSistemaFornecimentos
    /**
     * Associa um produto a um fornecedor.
     * 
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @throws FornecedorInativoException se o fornecedor estiver inativo
     */
    void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto) throws FornecedorInativoException;

    /**
     * Adiciona um novo fornecedor.
     * 
     * @param fornecedor o fornecedor a adicionar
     * @throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException se o fornecedor já existir
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     */
    void adicionarFornecedor(Fornecedor fornecedor) throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException, CamposObrigatoriosEmFaltaException;

    /**
     * Edita um fornecedor existente.
     * 
     * @param fornecedor o fornecedor a editar
     * @throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException se o fornecedor já existir
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     */
    void editarFornecedor(Fornecedor fornecedor) throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException, CamposObrigatoriosEmFaltaException;

    /**
     * Inativa um fornecedor através do identificador.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException se o fornecedor já estiver inativo
     */
    void inativarFornecedor(String idFornecedor) throws FornecedorInativoException;

    /**
     * Lista todos os fornecedores.
     * 
     * @return uma lista de fornecedores
     */
    List<Fornecedor> listarFornecedores();

    /**
     * Lista os fornecedores associados a um produto.
     * 
     * @param idProduto o identificador do produto
     * @return uma lista de identificadores de fornecedores
     */
    List<String> listarFornecedoresDeProduto(String idProduto);

    /**
     * Consulta os produtos fornecidos por um fornecedor específico.
     * 
     * @param idFornecedor o identificador do fornecedor
     * @return uma lista de associações produto-fornecedor
     */
    List<pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor> consultarProdutosDoFornecedor(String idFornecedor);

    /**
     * Cria uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     * @param linhas as linhas da encomenda
     * @throws FornecedorInativoException se o fornecedor estiver inativo
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     */
    void criarEncomenda(String idEncomenda, String idFornecedor, String idLoja, List<LinhaEncomenda> linhas) throws FornecedorInativoException, CamposObrigatoriosEmFaltaException;

    /**
     * Lista todas as encomendas.
     * 
     * @return uma lista de encomendas
     */
    List<Encomenda> listarEncomendas();

    /**
     * Processa a transição de estado para uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     */
    void processarTransicaoEstado(String idEncomenda);

    /**
     * Avança o pipeline de fornecimento de uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     */
    void avancarPipelineFornecimento(String idEncomenda);

    // SubSistemaVendas
    /**
     * Inicia uma nova venda.
     * 
     * @param idLoja o identificador da loja
     * @param idFuncionario o identificador do funcionário
     * @return a venda inicializada
     */
    Venda iniciarVenda(int idLoja, String idFuncionario);

    /**
     * Adiciona uma linha a uma venda.
     * 
     * @param venda a venda
     * @param produto o produto
     * @param quantidade a quantidade
     */
    void adicionarLinhaVenda(Venda venda, Produto produto, int quantidade);

    /**
     * Regista uma venda.
     * 
     * @param venda a venda
     * @param metodoPagamento o método de pagamento
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento estiver indisponível
     */
    void registarVenda(Venda venda, String metodoPagamento) throws MetodoPagamentoIndisponivelException;

    /**
     * Regista uma venda com um dado valor entregue.
     * 
     * @param venda a venda
     * @param metodoPagamento o método de pagamento
     * @param valorEntregue o montante entregue
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento estiver indisponível
     */
    void registarVenda(Venda venda, String metodoPagamento, double valorEntregue) throws MetodoPagamentoIndisponivelException;

    /**
     * Processa uma devolução para linhas de venda específicas.
     * 
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução tiver sido excedido
     */
    void processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException;

    /**
     * Lista as promoções ativas.
     * 
     * @return uma lista de promoções ativas
     */
    List<Promocao> listarPromocoesAtivas();

    /**
     * Adiciona uma nova promoção.
     * 
     * @param promocao a promoção
     */
    void adicionarPromocao(Promocao promocao);

    /**
     * Cancela uma promoção.
     * 
     * @param idPromocao o identificador da promoção
     * @param motivo o motivo do cancelamento
     */
    void cancelarPromocao(String idPromocao, String motivo);

    /**
     * Lista todas as vendas.
     * 
     * @return uma lista de vendas
     */
    List<Venda> listarVendas();

    /**
     * Lista todas as devoluções.
     * 
     * @return uma lista de devoluções
     */
    List<pt.uminho.taki.ln.vendas.Devolucao> listarDevolucoes();

    // Gestão de contas e perfis
    /**
     * Atualiza um funcionário existente.
     * 
     * @param funcionario o funcionário a atualizar
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     * @throws EmailJaExisteException se o correio eletrónico já existir
     * @throws PasswordFracaException se a palavra-passe for demasiado fraca
     */
    void atualizarFuncionario(Funcionario funcionario) throws FuncionarioNaoEncontradoException, EmailJaExisteException, PasswordFracaException;

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void bloquearConta(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException;

    /**
     * Elimina logicamente a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void removerContaLogicamente(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException;

    /**
     * Atribui um perfil a um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param nomePerfil o nome do perfil
     * @param idAdministradorSessao o identificador do administrador da sessão
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministradorSessao) throws FuncionarioNaoEncontradoException;

    /**
     * Regista um novo perfil de acesso.
     * 
     * @param perfil o perfil a registar
     * @throws pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException se o perfil já existir
     */
    void registarPerfil(PerfilAcesso perfil) throws pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;

    /**
     * Edita um perfil de acesso.
     * 
     * @param nomePerfil o nome do perfil
     * @param permissoes a nova lista de permissões
     */
    void editarPerfil(String nomePerfil, List<Permissao> permissoes);

    /**
     * Lista todos os perfis de acesso.
     * 
     * @return uma lista de perfis de acesso
     */
    List<PerfilAcesso> listarPerfis();

    // Consulta de inventário
    /**
     * Lista o inventário.
     * 
     * @return uma lista de itens de inventário
     */
    List<Inventario> listarInventario();

    /**
     * Define um novo limite de segurança para um item de inventário.
     * 
     * @param idInventario o identificador do inventário
     * @param novoLimite o novo limite
     * @throws ArtigoNaoEncontradoException se o artigo não for encontrado
     */
    void definirLimiteSeguranca(String idInventario, double novoLimite) throws ArtigoNaoEncontradoException;

    // Estatísticas
    /**
     * Obtém os KPIs gerais de uma loja.
     * 
     * @param idLoja o identificador da loja
     * @return um mapa de KPIs
     */
    java.util.Map<String, Object> obterKpiGerais(int idLoja);

    /**
     * Obtém as vendas mensais de uma loja.
     * 
     * @param idLoja o identificador da loja
     * @param meses o número de meses
     * @return uma lista de mapas de vendas mensais
     */
    java.util.List<java.util.Map<String, Object>> obterVendasMensais(int idLoja, int meses);

    /**
     * Obtém as vendas por hora de uma loja.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de mapas de vendas por hora
     */
    java.util.List<java.util.Map<String, Object>> obterVendasPorHora(int idLoja);

    /**
     * Obtém as vendas por categoria de uma loja.
     * 
     * @param idLoja o identificador da loja
     * @return uma lista de mapas de vendas por categoria
     */
    java.util.List<java.util.Map<String, Object>> obterVendasPorCategoria(int idLoja);

    // SubSistemaFatura (SAF-T)
    /**
     * Gera um ficheiro SAF-T mensal.
     * 
     * @param ano o ano
     * @param mes o mês
     * @return o conteúdo do ficheiro SAF-T
     */
    String gerarSaftMensal(int ano, int mes);

    /**
     * Gera uma segunda via em PDF de uma fatura.
     * 
     * @param numFatura o número da fatura
     * @return um array de bytes do PDF
     */
    byte[] gerarSegundaViaPDF(String numFatura);

    // SubSistemaSincronizacao
    /**
     * Sincroniza os dados.
     * 
     * @throws FalhaSincronizacaoException se a sincronização falhar
     */
    void sincronizarDados() throws FalhaSincronizacaoException;

    /**
     * Verifica a disponibilidade do sistema central.
     * 
     * @return true se disponível, false caso contrário
     */
    boolean verificarDisponibilidadeCentral();

    /**
     * Obtém a disponibilidade do sistema central.
     * 
     * @return a percentagem ou o estado de disponibilidade
     */
    double obterDisponibilidadeCentral();
}
