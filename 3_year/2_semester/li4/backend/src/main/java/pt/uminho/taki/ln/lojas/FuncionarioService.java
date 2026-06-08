package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.ln.lojas.seguranca.IPasswordHasher;
import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.exceptions.ContaBloqueadaException;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementação do serviço de gestão de funcionários com suporte a encriptação e perfis.
 * Adaptado para refletir as restrições da base de dados PostgreSQL.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class FuncionarioService implements IFuncionarioService {
    private static final Logger LOGGER = Logger.getLogger(FuncionarioService.class.getName());
    private final FuncionarioDAO funcionarioDAO;
    private final IPasswordHasher passwordHasher;
    private final IPerfilAcessoService perfilAcessoService;

    /**
     * Construtor para injeção de dependências.
     * @param funcionarioDAO o DAO de funcionários
     * @param passwordHasher o mecanismo de hashing de passwords
     */
    public FuncionarioService(FuncionarioDAO funcionarioDAO, IPasswordHasher passwordHasher) {
        this(funcionarioDAO, passwordHasher, null);
    }

    /**
     * Construtor para injeção de dependências com serviço de perfis.
     * @param funcionarioDAO o DAO de funcionários
     * @param passwordHasher o mecanismo de hashing de passwords
     * @param perfilAcessoService o serviço de gestão de perfis de acesso
     */
    public FuncionarioService(FuncionarioDAO funcionarioDAO, IPasswordHasher passwordHasher, IPerfilAcessoService perfilAcessoService) {
        this.funcionarioDAO = funcionarioDAO;
        this.passwordHasher = passwordHasher;
        this.perfilAcessoService = perfilAcessoService;
    }

    /**
     * Localiza um funcionário através do seu endereço de correio eletrónico.
     * 
     * @param email o endereço a pesquisar
     * @return um Optional contendo o funcionário encontrado
     */
    private Optional<Funcionario> findByEmail(String email) {
        return this.funcionarioDAO.findAll().stream()
            .filter(f -> f.getEmail().equalsIgnoreCase(email))
            .findFirst();
    }

    /**
     * Valida a presença de todos os dados obrigatórios para a criação ou atualização de uma conta.
     * 
     * @param funcionario o objeto com os dados a validar
     * @throws IllegalArgumentException se algum dado essencial estiver em falta
     */
    private void validarCamposObrigatorios(Funcionario funcionario) {
        if (funcionario == null
                || funcionario.getNome() == null || funcionario.getNome().isBlank()
                || funcionario.getEmail() == null || funcionario.getEmail().isBlank()
                || funcionario.getIdPerfilAcesso() == null || funcionario.getIdPerfilAcesso().isBlank()
                || funcionario.getIdLoja() <= 0) {
            throw new IllegalArgumentException("Dados obrigatórios em falta para a conta de utilizador.");
        }
    }

    /**
     * Procura um funcionário pelo seu identificador interno.
     * 
     * @param idFuncionario o identificador único
     * @return um Optional com o funcionário, se existir
     */
    private Optional<Funcionario> findById(String idFuncionario) {
        return this.funcionarioDAO.findById(idFuncionario);
    }

    /**
     * Regista um novo funcionário no sistema.
     * 
     * @param funcionario Objeto com os dados do funcionário a registar
     * @throws EmailJaExisteException Caso o email já se encontre associado a outra conta
     * @throws PasswordFracaException Caso a password não cumpra os requisitos mínimos de segurança
     */
    @Override
    public void registarFuncionario(Funcionario funcionario) throws EmailJaExisteException, PasswordFracaException {
        validarCamposObrigatorios(funcionario);
        validarPerfilSeConfigurado(funcionario.getIdPerfilAcesso());
        if (findByEmail(funcionario.getEmail()).isPresent()) {
            throw new EmailJaExisteException("O email " + funcionario.getEmail() + " ja esta registado.");
        }

        validarPassword(funcionario.getPassword());
        
        Funcionario novoFuncionario = funcionario.clone();
        
        String hash = this.passwordHasher.hash(funcionario.getPassword());
        novoFuncionario.setPassword(hash);
        
        novoFuncionario.setEstadoConta(EstadoConta.ATIVO);
        
        this.funcionarioDAO.save(novoFuncionario.getId(), novoFuncionario);
    }

    /**
     * Bloqueia a conta de um funcionário por um motivo específico.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param motivo Razão para o bloqueio da conta
     * @throws FuncionarioNaoEncontradoException Caso o ID fornecido não corresponda a nenhum funcionário
     */
    @Override
    public void bloquearConta(String idFuncionario, String motivo) throws FuncionarioNaoEncontradoException {
        bloquearConta(idFuncionario, motivo, null);
    }

    /**
     * Bloqueia a conta de um funcionário com registo do administrador responsável.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param motivo Razão para o bloqueio da conta
     * @param idAdministradorSessao Identificador do administrador que executa a ação
     * @throws FuncionarioNaoEncontradoException Caso o ID fornecido não corresponda a nenhum funcionário
     */
    @Override
    public void bloquearConta(String idFuncionario, String motivo, String idAdministradorSessao) throws FuncionarioNaoEncontradoException {
        bloquearConta(idFuncionario, motivo, idAdministradorSessao, null);
    }

    /**
     * Bloqueia a conta de um funcionário com validação de credenciais do administrador.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param motivo Razão para o bloqueio da conta
     * @param idAdministradorSessao Identificador do administrador que executa a ação
     * @param passwordAdministrador Password do administrador para confirmação da ação
     * @throws FuncionarioNaoEncontradoException Caso o ID fornecido não corresponda a nenhum funcionário
     */
    @Override
    public void bloquearConta(String idFuncionario, String motivo, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        if (idAdministradorSessao != null && idAdministradorSessao.equals(idFuncionario)) {
            throw new IllegalStateException("O administrador autenticado não pode bloquear a própria conta.");
        }
        validarPermissaoAdministradorSeFornecido(idAdministradorSessao, "BLOQUEAR_CONTA");
        validarPasswordAdministradorSeFornecida(idAdministradorSessao, passwordAdministrador);
        Optional<Funcionario> fOpt = findById(idFuncionario);
        
        if (fOpt.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Funcionario com ID " + idFuncionario + " nao foi encontrado.");
        }
        
        Funcionario f = fOpt.get();
        f.setEstadoConta(EstadoConta.BLOQUEADO);
        this.funcionarioDAO.save(f.getId(), f);
    }

    /**
     * Realiza a autenticação de um funcionário através do email e password.
     * 
     * @param email Endereço de correio eletrónico do funcionário
     * @param password Password em texto limpo para validação
     * @return O objeto Funcionario clonado em caso de sucesso
     * @throws ContaBloqueadaException Caso a conta se encontre num estado de bloqueio
     * @throws CredenciaisInvalidasException Caso as credenciais não coincidam com os registos
     */
    @Override
    public Funcionario autenticar(String email, String password) throws ContaBloqueadaException, CredenciaisInvalidasException {
        Optional<Funcionario> fOpt = findByEmail(email);
        
        if (fOpt.isEmpty()) {
            throw new CredenciaisInvalidasException("Email ou password incorretos.");
        }
        
        Funcionario f = fOpt.get();
        
        if (f.getEstadoConta() == EstadoConta.BLOQUEADO) {
            throw new ContaBloqueadaException("A conta encontra-se bloqueada. Contacte a administracao.");
        }
        
        if (!this.passwordHasher.matches(password, f.getPassword())) {
            throw new CredenciaisInvalidasException("Email ou password incorretos.");
        }
        
        return f.clone();
    }

    /**
     * Atualiza os dados de um funcionário existente.
     * 
     * @param funcionario Objeto com os dados atualizados do funcionário
     * @throws FuncionarioNaoEncontradoException Caso o funcionário não exista no sistema
     * @throws EmailJaExisteException Caso o novo email já pertença a outra conta
     * @throws PasswordFracaException Caso a nova password não cumpra os requisitos de segurança
     */
    @Override
    public void atualizarFuncionario(Funcionario funcionario) throws FuncionarioNaoEncontradoException, EmailJaExisteException, PasswordFracaException {
        validarCamposObrigatorios(funcionario);
        validarPerfilSeConfigurado(funcionario.getIdPerfilAcesso());

        Optional<Funcionario> existente = findById(funcionario.getId());
        if (existente.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Funcionario com ID " + funcionario.getId() + " nao foi encontrado.");
        }

        Optional<Funcionario> outroComMesmoEmail = findByEmail(funcionario.getEmail())
                .filter(f -> !f.getId().equals(funcionario.getId()));
        if (outroComMesmoEmail.isPresent()) {
            throw new EmailJaExisteException("O email " + funcionario.getEmail() + " ja esta registado noutra conta.");
        }

        Funcionario atualizado = funcionario.clone();
        String password = atualizado.getPassword();
        if (password != null && !password.isBlank()) {
            boolean pareceHash = password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$") || password.startsWith("$argon2");
            if (!pareceHash) {
                validarPassword(password);
                atualizado.setPassword(this.passwordHasher.hash(password));
            }
        } else {
            atualizado.setPassword(existente.get().getPassword());
        }

        this.funcionarioDAO.save(atualizado.getId(), atualizado);
    }

    /**
     * Atribui um perfil de acesso específico a um funcionário.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param nomePerfil Nome do novo perfil a atribuir
     * @param idAdministradorSessao Identificador do administrador que executa a ação
     * @throws FuncionarioNaoEncontradoException Caso o funcionário não seja encontrado
     */
    @Override
    public void atribuirPerfil(String idFuncionario, String nomePerfil, String idAdministradorSessao) throws FuncionarioNaoEncontradoException {
        validarPermissaoAdministradorSeFornecido(idAdministradorSessao, "ATRIBUIR_PERFIL");
        validarPerfilSeConfigurado(nomePerfil);
        Optional<Funcionario> fOpt = findById(idFuncionario);
        if (fOpt.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Funcionario com ID " + idFuncionario + " nao foi encontrado.");
        }
        Funcionario f = fOpt.get();
        f.setIdPerfilAcesso(nomePerfil);
        this.funcionarioDAO.save(f.getId(), f);
    }

    /**
     * Efetua a remoção lógica (desativação) da conta de um funcionário.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param idAdministradorSessao Identificador do administrador que executa a ação
     * @throws FuncionarioNaoEncontradoException Caso o funcionário não seja encontrado
     */
    @Override
    public void removerContaLogicamente(String idFuncionario, String idAdministradorSessao) throws FuncionarioNaoEncontradoException {
        removerContaLogicamente(idFuncionario, idAdministradorSessao, null);
    }

    /**
     * Efetua a remoção lógica da conta de um funcionário com confirmação de password do administrador.
     * 
     * @param idFuncionario Identificador único do funcionário
     * @param idAdministradorSessao Identificador do administrador que executa a ação
     * @param passwordAdministrador Password do administrador para validação da segurança
     * @throws FuncionarioNaoEncontradoException Caso o funcionário não seja encontrado
     */
    @Override
    public void removerContaLogicamente(String idFuncionario, String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        if (idAdministradorSessao != null && idAdministradorSessao.equals(idFuncionario)) {
            throw new IllegalStateException("O administrador autenticado não pode remover a própria conta.");
        }
        validarPermissaoAdministradorSeFornecido(idAdministradorSessao, "REMOVER_LOGICAMENTE");
        validarPasswordAdministradorSeFornecida(idAdministradorSessao, passwordAdministrador);
        Optional<Funcionario> fOpt = findById(idFuncionario);
        if (fOpt.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Funcionario com ID " + idFuncionario + " nao foi encontrado.");
        }
        Funcionario f = fOpt.get();
        f.setEstadoConta(EstadoConta.INATIVO);
        this.funcionarioDAO.save(f.getId(), f);
    }

    /**
     * Lista todos os funcionários registados no sistema.
     * 
     * @return Lista de objetos Funcionario
     */
    @Override
    public java.util.List<Funcionario> listarFuncionarios() {
        return new java.util.ArrayList<>(this.funcionarioDAO.findAll());
    }

    /**
     * Procura um funcionário através do seu identificador.
     *
     * @param id O identificador do funcionário.
     * @return um Optional contendo o funcionário, se for encontrado.
     */
    @Override
    public Optional<Funcionario> buscarPorId(String id) {
        return this.findById(id);
    }
    
    /**
     * Valida se uma palavra-passe cumpre os critérios mínimos de complexidade.
     * 
     * @param password o texto a validar
     * @throws PasswordFracaException se a complexidade for insuficiente
     */
    private void validarPassword(String password) throws PasswordFracaException {
        if (password == null || password.length() < 8) {
            throw new PasswordFracaException("A password deve ter pelo menos 8 caracteres.");
        }

        boolean temMaiuscula = false;
        boolean temNumero = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) temMaiuscula = true;
            if (Character.isDigit(c)) temNumero = true;
        }

        if (!temMaiuscula || !temNumero) {
            throw new PasswordFracaException("A password deve conter pelo menos uma letra maiuscula e um numero.");
        }
    }

    /**
     * Valida as credenciais do administrador para confirmação de operações críticas.
     * 
     * @param idAdministradorSessao o identificador do administrador
     * @param passwordAdministrador o texto da palavra-passe a validar
     * @throws FuncionarioNaoEncontradoException se o administrador não for localizado
     * @throws IllegalArgumentException se a palavra-passe for incorreta
     */
    private void validarPasswordAdministradorSeFornecida(String idAdministradorSessao, String passwordAdministrador) throws FuncionarioNaoEncontradoException {
        if (passwordAdministrador == null) return;
        if (idAdministradorSessao == null || idAdministradorSessao.isBlank()) {
            throw new IllegalArgumentException("ID do administrador é obrigatório para confirmação de password.");
        }

        Optional<Funcionario> adminOpt = findById(idAdministradorSessao);
        if (adminOpt.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Administrador com ID " + idAdministradorSessao + " nao foi encontrado.");
        }
        Funcionario admin = adminOpt.get();
        if (!this.passwordHasher.matches(passwordAdministrador, admin.getPassword())) {
            throw new IllegalArgumentException("Confirmação de password do administrador inválida.");
        }
    }

    /**
     * Verifica se o utilizador em sessão possui privilégios administrativos.
     * 
     * @param idAdministradorSessao o identificador do utilizador
     * @param acao a descrição da operação para registo de auditoria
     * @throws FuncionarioNaoEncontradoException se o utilizador não for localizado
     * @throws SecurityException se o utilizador não possuir autorização
     */
    private void validarPermissaoAdministradorSeFornecido(String idAdministradorSessao, String acao) throws FuncionarioNaoEncontradoException {
        if (idAdministradorSessao == null || idAdministradorSessao.isBlank()) {
            return;
        }
        Optional<Funcionario> adminOpt = findById(idAdministradorSessao);
        if (adminOpt.isEmpty()) {
            throw new FuncionarioNaoEncontradoException("Administrador com ID " + idAdministradorSessao + " nao foi encontrado.");
        }

        Funcionario admin = adminOpt.get();
        boolean autorizado = temPermissaoAdministrativa(admin);
        if (!autorizado) {
            LOGGER.warning("Tentativa de operação administrativa sem permissão. admin=" + idAdministradorSessao + ", acao=" + acao);
            throw new SecurityException("Acesso negado: o utilizador não possui permissões administrativas.");
        }
    }

    /**
     * Valida a existência e validade de um perfil de acesso no sistema.
     * 
     * @param nomePerfil a designação do perfil
     * @throws IllegalArgumentException se o perfil for inválido ou não existir
     */
    private void validarPerfilSeConfigurado(String nomePerfil) {
        if (this.perfilAcessoService == null) return;
        if (nomePerfil == null || nomePerfil.isBlank()) {
            throw new IllegalArgumentException("Perfil de acesso é obrigatório.");
        }
        if (!this.perfilAcessoService.existePerfil(nomePerfil)) {
            throw new IllegalArgumentException("Perfil de acesso inválido: " + nomePerfil);
        }
    }

    /**
     * Verifica se um objeto de funcionário detém privilégios de administração.
     * 
     * @param funcionario o objeto a analisar
     * @return verdadeiro se possuir acesso administrativo
     */
    private boolean temPermissaoAdministrativa(Funcionario funcionario) {
        if (funcionario == null) return false;
        String perfil = funcionario.getIdPerfilAcesso();
        if (perfil == null) return false;
        String normalizado = perfil.trim().toUpperCase();
        return normalizado.equals("ADMIN")
                || normalizado.equals("ADMINISTRADOR")
                || normalizado.equals("ADMINISTRADOR DO SISTEMA")
                || normalizado.equals("PROPRIETARIO DA CADEIA")
                || normalizado.equals("PROPRIETÁRIO DA CADEIA")
                || normalizado.equals("GESTOR_CENTRAL")
                || normalizado.equals("GESTOR_LOJA")
                || normalizado.equals("GERENTE")
                || normalizado.equals("GERENTE DE LOJA");
    }
}