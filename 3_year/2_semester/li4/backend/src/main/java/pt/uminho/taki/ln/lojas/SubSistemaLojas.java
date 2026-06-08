package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.dao.CategoriaDAO;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.dao.LojaDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.ln.lojas.seguranca.BCryptPasswordHasher;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;
import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;
import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;

import java.util.List;
import java.util.Optional;

/**
 * Subsistema de Lojas.
 * @author TakiLN Team
 * @since 1.0
 */
public class SubSistemaLojas implements ISubSistemaLojas {

    private final ILojaService lojaService;
    private final IFuncionarioService funcionarioService;
    private final IProdutoService produtoService;
    private final ICategoriaService categoriaService;
    private final IPerfilAcessoService perfilAcessoService;

    /**
     * Construtor para SubSistemaLojas.
     * @param funcionarioDAO o DAO de funcionário
     * @param lojaDAO o DAO de loja
     * @param categoriaDAO o DAO de categoria
     * @param produtoDAO o DAO de produto
     */
    public SubSistemaLojas(FuncionarioDAO funcionarioDAO, LojaDAO lojaDAO, CategoriaDAO categoriaDAO, ProdutoDAO produtoDAO) {
        this.lojaService = new LojaService(lojaDAO);
        this.perfilAcessoService = new PerfilAcessoService();
        this.funcionarioService = new FuncionarioService(funcionarioDAO, new BCryptPasswordHasher(), this.perfilAcessoService);
        this.produtoService = new ProdutoService(produtoDAO);
        this.categoriaService = new CategoriaService(categoriaDAO);
    }

    // --- Lojas ---

    /**
     * Regista uma nova loja no sistema.
     * 
     * @param loja a entidade de loja a registar
     * @return a entidade de loja registada
     */
    @Override
    public Loja registarLoja(Loja loja) {
        return this.lojaService.registarLoja(loja);
    }

    /**
     * Procura uma loja através do seu identificador.
     * 
     * @param idLoja o identificador da loja
     * @return um optional que contém a entidade de loja
     */
    @Override
    public Optional<Loja> buscarLoja(int idLoja) {
        return this.lojaService.buscarLoja(idLoja);
    }

    /**
     * Lista todas as lojas registadas.
     * 
     * @return uma lista com as entidades de loja
     */
    @Override
    public List<Loja> listarLojas() {
        return this.lojaService.listarLojas();
    }

    /**
     * Atualiza os dados de uma loja existente.
     * 
     * @param loja a entidade de loja com os dados atualizados
     * @return a entidade de loja após a atualização
     */
    @Override
    public Loja atualizarLoja(Loja loja) {
        return this.lojaService.atualizarLoja(loja);
    }

    /**
     * Remove uma loja do sistema pelo seu identificador.
     * 
     * @param idLoja o identificador da loja a remover
     */
    @Override
    public void removerLoja(int idLoja) {
        this.lojaService.removerLoja(idLoja);
    }

    // --- Delegação para serviços existentes ---

    /**
     * Obtém o serviço de gestão de lojas.
     * 
     * @return o serviço ILojaService
     */
    @Override
    public ILojaService getLojaService() {
        return this.lojaService;
    }

    /**
     * Obtém o serviço de gestão de funcionários.
     * 
     * @return o serviço IFuncionarioService
     */
    @Override
    public IFuncionarioService getFuncionarioService() {
        return this.funcionarioService;
    }

    /**
     * Obtém o serviço de gestão de produtos.
     * 
     * @return o serviço IProdutoService
     */
    @Override
    public IProdutoService getProdutoService() {
        return this.produtoService;
    }

    /**
     * Obtém o serviço de gestão de categorias.
     * 
     * @return o serviço ICategoriaService
     */
    @Override
    public ICategoriaService getCategoriaService() {
        return this.categoriaService;
    }

    /**
     * Obtém o serviço de gestão de perfis de acesso.
     * 
     * @return o serviço IPerfilAcessoService
     */
    @Override
    public IPerfilAcessoService getPerfilAcessoService() {
        return this.perfilAcessoService;
    }

    /**
     * Adiciona uma nova categoria ao sistema.
     * 
     * @param categoria a entidade de categoria a adicionar
     */
    @Override
    public void adicionarCategoria(Categoria categoria) {
        this.categoriaService.adicionarCategoria(categoria);
    }

    /**
     * Lista todas as categorias registadas.
     * 
     * @return uma lista com as entidades de categoria
     */
    @Override
    public List<Categoria> listarCategorias() {
        return this.categoriaService.listarCategorias();
    }

    /**
     * Edita os dados de uma categoria existente.
     * 
     * @param categoria a entidade de categoria com os dados atualizados
     */
    @Override
    public void editarCategoria(Categoria categoria) {
        this.categoriaService.editarCategoria(categoria);
    }

    /**
     * Inativa uma categoria no sistema.
     * 
     * @param idCategoria o identificador da categoria a inativar
     */
    @Override
    public void inativarCategoria(String idCategoria) {
        this.categoriaService.inativarCategoria(idCategoria);
    }

    /**
     * Adiciona um novo produto ao catálogo.
     * 
     * @param produto a entidade de produto a adicionar
     * @throws ProdutoExistenteException se já existir um produto com o mesmo código
     */
    @Override
    public void adicionarProduto(Produto produto) throws ProdutoExistenteException {
        this.produtoService.adicionarProduto(produto);
    }

    /**
     * Edita os dados de um produto existente.
     * 
     * @param produto a entidade de produto com os dados atualizados
     * @throws ProdutoInexistenteException se o produto não for encontrado
     * @throws ProdutoInativoException se o produto estiver inativo
     * @throws ProdutoExistenteException se os novos dados entrarem em conflito com outro produto
     */
    @Override
    public void editarProduto(Produto produto) throws ProdutoInexistenteException, ProdutoInativoException, ProdutoExistenteException {
        this.produtoService.editarProduto(produto);
    }

    /**
     * Inativa um produto no sistema.
     * 
     * @param idProduto o identificador do produto a inativar
     * @throws ProdutoInativoException se o produto já se encontrar inativo
     */
    @Override
    public void inativarProduto(String idProduto) throws ProdutoInativoException {
        this.produtoService.inativarProduto(idProduto);
    }

    /**
     * Procura um produto através do seu código de barras.
     * 
     * @param codigoBarras o código de barras do produto
     * @return a entidade de produto encontrada
     */
    @Override
    public Produto pesquisarPorCodigoBarras(String codigoBarras) {
        return this.produtoService.pesquisarPorCodigoBarras(codigoBarras);
    }

    /**
     * Regista um novo funcionário no sistema.
     * 
     * @param funcionario a entidade de funcionário a registar
     * @throws EmailJaExisteException se o e-mail já estiver em uso
     * @throws PasswordFracaException se a palavra-passe não cumprir os requisitos de segurança
     */
    @Override
    public void registarFuncionario(Funcionario funcionario) throws EmailJaExisteException, PasswordFracaException {
        this.funcionarioService.registarFuncionario(funcionario);
    }

    /**
     * Atualiza os dados de um funcionário existente.
     * 
     * @param funcionario a entidade de funcionário com os dados atualizados
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     * @throws EmailJaExisteException se o novo e-mail já estiver em uso por outro utilizador
     * @throws PasswordFracaException se a nova palavra-passe for fraca
     */
    @Override
    public void atualizarFuncionario(Funcionario funcionario) throws FuncionarioNaoEncontradoException, EmailJaExisteException, PasswordFracaException {
        this.funcionarioService.atualizarFuncionario(funcionario);
    }

    /**
     * Bloqueia a conta de um funcionário por motivos administrativos.
     * 
     * @param idFuncionario o identificador do funcionário a bloquear
     * @param motivo o motivo do bloqueio
     * @param idAdministrador o identificador do administrador que realiza a operação
     * @param passwordAdministrador a palavra-passe do administrador para confirmação
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    @Override
    public void bloquearConta(String idFuncionario, String motivo, String idAdministrador, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        this.funcionarioService.bloquearConta(idFuncionario, motivo, idAdministrador, passwordAdministrador);
    }

    /**
     * Remove logicamente a conta de um funcionário do sistema.
     * 
     * @param idFuncionario o identificador do funcionário a remover
     * @param idAdministrador o identificador do administrador que realiza a operação
     * @param passwordAdministrador a palavra-passe do administrador para confirmação
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    @Override
    public void removerContaLogicamente(String idFuncionario, String idAdministrador, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        this.funcionarioService.removerContaLogicamente(idFuncionario, idAdministrador, passwordAdministrador);
    }

    /**
     * Lista todos os funcionários registados no sistema.
     * 
     * @return uma lista com as entidades de funcionário
     */
    @Override
    public List<Funcionario> listarFuncionarios() {
        return this.funcionarioService.listarFuncionarios();
    }

    /**
     * Atribui um perfil de acesso a um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param nomePerfil o nome do perfil a atribuir
     * @param idAdministrador o identificador do administrador que realiza a operação
     * @throws FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    @Override
    public void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministrador) throws FuncionarioNaoEncontradoException {
        this.funcionarioService.atribuirPerfil(idFuncionario, nomePerfil, idAdministrador);
    }

    /**
     * Regista um novo perfil de acesso no sistema.
     * 
     * @param perfil a entidade de perfil de acesso a registar
     * @throws PerfilDuplicadoException se já existir um perfil com o mesmo nome ou identificador
     */
    @Override
    public void registarPerfil(PerfilAcesso perfil) throws PerfilDuplicadoException {
        this.perfilAcessoService.registarPerfil(perfil);
    }

    /**
     * Edita as permissões de um perfil de acesso existente.
     * 
     * @param nomePerfil o nome do perfil a editar
     * @param permissoes a nova lista de permissões para o perfil
     */
    @Override
    public void editarPerfil(String nomePerfil, List<Permissao> permissoes) {
        this.perfilAcessoService.editarPerfil(nomePerfil, permissoes);
    }

    /**
     * Lista todos os perfis de acesso registados.
     * 
     * @return uma lista com as entidades de perfil de acesso
     */
    @Override
    public List<PerfilAcesso> listarPerfis() {
        return this.perfilAcessoService.listarPerfis();
    }

    /**
     * Lista todos os produtos registados no catálogo.
     * 
     * @return uma lista com as entidades de produto
     */
    @Override
    public List<Produto> listarProdutos() {
        return this.produtoService.listarProdutos();
    }

    /**
     * Bloqueia a conta de um funcionário de forma simplificada (administrativa).
     * 
     * @param idFuncionario o identificador do funcionário a bloquear
     */
    @Override
    public void bloquearConta(String idFuncionario) {
        this.funcionarioService.bloquearConta(idFuncionario, "Bloqueio administrativo");
    }

    @Override
    public Funcionario autenticar(String email, String password) {
        return this.funcionarioService.autenticar(email, password);
    }

    @Override
    public Optional<Funcionario> buscarFuncionarioPorId(String id) {
        return this.funcionarioService.buscarPorId(id);
    }
}
