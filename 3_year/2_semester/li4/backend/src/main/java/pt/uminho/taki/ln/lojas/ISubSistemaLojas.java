package pt.uminho.taki.ln.lojas;

import java.util.List;
import java.util.Optional;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;

/**
 * Interface do subsistema para a gestão de lojas, funcionários e produtos.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaLojas {
    /**
     * Obtém o serviço de lojas.
     * 
     * @return o serviço de lojas
     */
    ILojaService getLojaService();

    /**
     * Obtém o serviço de funcionários.
     * 
     * @return o serviço de funcionários
     */
    IFuncionarioService getFuncionarioService();

    /**
     * Obtém o serviço de produtos.
     * 
     * @return o serviço de produtos
     */
    IProdutoService getProdutoService();

    /**
     * Obtém o serviço de categorias.
     * 
     * @return o serviço de categorias
     */
    ICategoriaService getCategoriaService();

    /**
     * Obtém o serviço de perfis de acesso.
     * 
     * @return o serviço de perfis de acesso
     */
    IPerfilAcessoService getPerfilAcessoService();

    // Lojas
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

    // Categorias
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
     * Edita uma categoria existente.
     * 
     * @param categoria a categoria a editar
     */
    void editarCategoria(Categoria categoria);

    /**
     * Inativa uma categoria através do seu identificador.
     * 
     * @param idCategoria o identificador da categoria
     */
    void inativarCategoria(String idCategoria);

    // Produtos
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
     * @throws pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException se o correio eletrónico já existir
     * @throws pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException se a palavra-passe for demasiado fraca
     */
    void registarFuncionario(Funcionario funcionario) throws pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException, pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;

    /**
     * Atualiza um funcionário existente.
     * 
     * @param funcionario o funcionário a atualizar
     * @throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException se o funcionário não for encontrado
     * @throws pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException se o correio eletrónico já existir
     * @throws pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException se a palavra-passe for demasiado fraca
     */
    void atualizarFuncionario(Funcionario funcionario) throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException, pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException, pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param motivo o motivo do bloqueio
     * @param idAdministrador o identificador do administrador
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void bloquearConta(String idFuncionario, String motivo, String idAdministrador, String passwordAdministrador) throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     */
    void bloquearConta(String idFuncionario);

    /**
     * Elimina logicamente a conta de um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param idAdministrador o identificador do administrador
     * @param passwordAdministrador a palavra-passe do administrador
     * @throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void removerContaLogicamente(String idFuncionario, String idAdministrador, String passwordAdministrador) throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;

    /**
     * Lista todos os funcionários.
     * 
     * @return uma lista de funcionários
     */
    List<Funcionario> listarFuncionarios();

    // Perfis
    /**
     * Atribui um perfil a um funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     * @param nomePerfil o nome do perfil
     * @param idAdministrador o identificador do administrador
     * @throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException se o funcionário não for encontrado
     */
    void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministrador) throws pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;

    /**
     * Regista um novo perfil de acesso.
     * 
     * @param perfil o perfil de acesso a registar
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

    /**
     * Lista todos os produtos.
     * 
     * @return uma lista de produtos
     */
    List<Produto> listarProdutos();

    /**
     * Autentica um funcionário com as suas credenciais.
     *
     * @param email O email do funcionário.
     * @param password A password do funcionário.
     * @return O funcionário autenticado, ou null se as credenciais forem inválidas.
     */
    Funcionario autenticar(String email, String password);

    /**
     * Procura um funcionário através do seu identificador.
     *
     * @param id O identificador do funcionário.
     * @return um Optional contendo o funcionário, se for encontrado.
     */
    Optional<Funcionario> buscarFuncionarioPorId(String id);
}
