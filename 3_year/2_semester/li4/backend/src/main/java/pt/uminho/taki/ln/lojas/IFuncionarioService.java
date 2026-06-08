package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.exceptions.ContaBloqueadaException;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;

/**
 * Interface para o servico de gestao de funcionarios.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IFuncionarioService {
    /**
     * Regista um novo funcionario no sistema.
     * @param funcionario o funcionario a registar
     * @throws EmailJaExisteException se o email ja estiver registado
     * @throws PasswordFracaException se a password nao cumprir os requisitos de seguranca
     */
    void registarFuncionario(Funcionario funcionario) throws EmailJaExisteException, PasswordFracaException;

    /**
     * Bloqueia o acesso de um funcionario temporariamente.
     * @param idFuncionario o identificador do funcionario
     * @param motivo o motivo do bloqueio
     * @throws FuncionarioNaoEncontradoException se o funcionario nao for encontrado
     */
    void bloquearConta(String idFuncionario, String motivo) throws FuncionarioNaoEncontradoException;

    /**
     * Bloqueia o acesso de um funcionario com validacao de auto-bloqueio.
     * @param idFuncionario alvo do bloqueio
     * @param motivo motivo do bloqueio
     * @param idAdministradorSessao utilizador administrador autenticado na sessao
     * @throws FuncionarioNaoEncontradoException se o funcionario nao for encontrado
     */
    void bloquearConta(String idFuncionario, String motivo, String idAdministradorSessao) throws FuncionarioNaoEncontradoException;

    /**
     * Bloqueia o acesso com confirmação explícita da password do administrador.
     * @param idFuncionario alvo do bloqueio
     * @param motivo motivo do bloqueio
     * @param idAdministradorSessao utilizador administrador autenticado na sessão
     * @param passwordAdministrador password em claro do administrador para confirmação
     * @throws FuncionarioNaoEncontradoException se o funcionário alvo ou administrador não existirem
     */
    void bloquearConta(String idFuncionario, String motivo, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException;

    /**
     * Autentica um funcionario validando as suas credenciais e o estado da conta.
     * @param email o email do funcionario
     * @param password a password do funcionario
     * @return o funcionario autenticado
     * @throws ContaBloqueadaException se a conta estiver bloqueada
     * @throws CredenciaisInvalidasException se o email ou password estiverem incorretos
     */
    Funcionario autenticar(String email, String password) throws ContaBloqueadaException, CredenciaisInvalidasException;

    /**
     * Atualiza os dados de uma conta existente com validacao de unicidade e perfil.
     * @param funcionario dados atualizados
     * @throws FuncionarioNaoEncontradoException se a conta nao existir
     * @throws EmailJaExisteException se o email ja existir noutra conta
     * @throws PasswordFracaException se a password nao cumprir regras quando for alterada
     */
    void atualizarFuncionario(Funcionario funcionario) throws FuncionarioNaoEncontradoException, EmailJaExisteException, PasswordFracaException;

    /**
     * Atribui/altera o perfil de acesso de um utilizador existente.
     * @param idFuncionario alvo da alteração
     * @param nomePerfil nome do perfil a associar
     * @param idAdministradorSessao administrador autenticado na sessão
     * @throws FuncionarioNaoEncontradoException se o utilizador não existir
     */
    void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministradorSessao) throws FuncionarioNaoEncontradoException;

    /**
     * Remove logicamente uma conta (mantendo rastreabilidade historica).
     * @param idFuncionario alvo da remocao
     * @param idAdministradorSessao utilizador administrador autenticado na sessao
     * @throws FuncionarioNaoEncontradoException se a conta nao existir
     */
    void removerContaLogicamente(String idFuncionario, String idAdministradorSessao) throws FuncionarioNaoEncontradoException;

    /**
     * Remove logicamente uma conta com confirmação da password do administrador.
     * @param idFuncionario alvo da remoção
     * @param idAdministradorSessao administrador autenticado
     * @param passwordAdministrador password em claro do administrador
     * @throws FuncionarioNaoEncontradoException se a conta não existir
     */
    void removerContaLogicamente(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException;

    /**
     * Retorna a lista de todos os funcionarios registados.
     * @return lista de funcionarios
     */
    java.util.List<Funcionario> listarFuncionarios();

    /**
     * Procura um funcionário através do seu identificador.
     *
     * @param id O identificador do funcionário.
     * @return um Optional contendo o funcionário, se for encontrado.
     */
    java.util.Optional<Funcionario> buscarPorId(String id);
}
