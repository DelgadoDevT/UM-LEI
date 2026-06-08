package pt.uminho.taki.ln.lojas;

import java.util.Objects;

/**
 * Entidade que representa um Funcionario no sistema.
 * Sincronizada com o esquema PostgreSQL (inclui idLoja e estados restritos).
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Funcionario {
    private String id;
    private String nome;
    private String email;
    private String password;
    private String idPerfilAcesso;
    private int idLoja;
    private EstadoConta estadoConta;

    /**
     * Construtor vazio para a classe Funcionario.
     * O estado da conta por omissao e ATIVO.
     */
    public Funcionario() {
        this.id = "";
        this.nome = "";
        this.email = "";
        this.password = "";
        this.idPerfilAcesso = "";
        this.idLoja = 0;
        this.estadoConta = EstadoConta.ATIVO;
    }

    /**
     * Construtor completo para a classe Funcionario.
     * @param id o id do funcionario
     * @param nome o nome do funcionario
     * @param email o email do funcionario
     * @param password a password do funcionario
     * @param idPerfilAcesso o identificador do perfil de acesso (mapeado para cargo na BD)
     * @param idLoja o identificador da loja associada
     */
    public Funcionario(String id, String nome, String email, String password, String idPerfilAcesso, int idLoja) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.idPerfilAcesso = idPerfilAcesso;
        this.idLoja = idLoja;
        this.estadoConta = EstadoConta.ATIVO;
    }

    /**
     * Construtor completo com estado de conta para a classe Funcionario.
     * @param id o id do funcionario
     * @param nome o nome do funcionario
     * @param email o email do funcionario
     * @param password a password do funcionario
     * @param idPerfilAcesso o identificador do perfil de acesso
     * @param idLoja o identificador da loja associada
     * @param estadoConta o estado atual da conta
     */
    public Funcionario(String id, String nome, String email, String password, String idPerfilAcesso, int idLoja, EstadoConta estadoConta) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.idPerfilAcesso = idPerfilAcesso;
        this.idLoja = idLoja;
        this.estadoConta = estadoConta;
    }

    /**
     * Construtor de copia para a classe Funcionario.
     * @param f o funcionario a copiar
     */
    public Funcionario(Funcionario f) {
        this.id = f.getId();
        this.nome = f.getNome();
        this.email = f.getEmail();
        this.password = f.getPassword();
        this.idPerfilAcesso = f.getIdPerfilAcesso();
        this.idLoja = f.getIdLoja();
        this.estadoConta = f.getEstadoConta();
    }

    /**
     * Obtém o identificador do funcionário.
     * @return o identificador
     */
    public String getId() { return id; }
    /**
     * Define o identificador do funcionário.
     * @param id o novo identificador
     */
    public void setId(String id) { this.id = id; }
    /**
     * Obtém o nome do funcionário.
     * @return o nome
     */
    public String getNome() { return nome; }
    /**
     * Define o nome do funcionário.
     * @param nome o novo nome
     */
    public void setNome(String nome) { this.nome = nome; }
    /**
     * Obtém o email do funcionário.
     * @return o email
     */
    public String getEmail() { return email; }
    /**
     * Define o email do funcionário.
     * @param email o novo email
     */
    public void setEmail(String email) { this.email = email; }
    /**
     * Obtém a password do funcionário.
     * @return a password
     */
    public String getPassword() { return password; }
    /**
     * Define a password do funcionário.
     * @param password a nova password
     */
    public void setPassword(String password) { this.password = password; }
    /**
     * Obtém o identificador do perfil de acesso.
     * @return o identificador do perfil
     */
    public String getIdPerfilAcesso() { return idPerfilAcesso; }
    /**
     * Define o identificador do perfil de acesso.
     * @param idPerfilAcesso o novo identificador do perfil
     */
    public void setIdPerfilAcesso(String idPerfilAcesso) { this.idPerfilAcesso = idPerfilAcesso; }
    /**
     * Obtém o identificador da loja associada.
     * @return o identificador da loja
     */
    public int getIdLoja() { return idLoja; }
    /**
     * Define o identificador da loja associada.
     * @param idLoja o novo identificador da loja
     */
    public void setIdLoja(int idLoja) { this.idLoja = idLoja; }
    /**
     * Obtém o estado atual da conta.
     * @return o estado da conta
     */
    public EstadoConta getEstadoConta() { return estadoConta; }
    /**
     * Define o estado atual da conta.
     * @param estadoConta o novo estado da conta
     */
    public void setEstadoConta(EstadoConta estadoConta) { this.estadoConta = estadoConta; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return idLoja == that.idLoja &&
               Objects.equals(id, that.id) &&
               Objects.equals(nome, that.nome) &&
               Objects.equals(email, that.email) &&
               Objects.equals(password, that.password) &&
               Objects.equals(idPerfilAcesso, that.idPerfilAcesso) &&
               estadoConta == that.estadoConta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, password, idPerfilAcesso, idLoja, estadoConta);
    }

    @Override
    public String toString() {
        return "Funcionario{" +
               "id='" + id + '\'' +
               ", nome='" + nome + '\'' +
               ", email='" + email + '\'' +
               ", idPerfilAcesso='" + idPerfilAcesso + '\'' +
               ", idLoja=" + idLoja +
               ", estadoConta=" + estadoConta +
               '}';
    }

    @Override
    public Funcionario clone() {
        return new Funcionario(this);
    }
}
