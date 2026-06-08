package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.lojas.Funcionario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

/**
 * DTO (Objeto de Transferência de Dados) para pedidos de Funcionario.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FuncionarioRequestDto {

    private String id;
    
    @NotBlank(message = "O nome não pode ser vazio")
    private String nome;
    
    @NotBlank(message = "O email não pode ser vazio")
    @Email(message = "Formato de email inválido")
    private String email;
    
    private String password;
    
    @NotBlank(message = "O perfil de acesso não pode ser vazio")
    private String idPerfilAcesso;
    
    @NotNull(message = "A loja não pode ser nula")
    private Integer idLoja;

    /**
     * Obtém o identificador.
     *
     * @return o identificador
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador.
     *
     * @param id o identificador a definir
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o nome.
     *
     * @return o nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome.
     *
     * @param nome o nome a definir
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém o correio eletrónico.
     *
     * @return o correio eletrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o correio eletrónico.
     *
     * @param email o correio eletrónico a definir
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém a palavra-passe.
     *
     * @return a palavra-passe
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a palavra-passe.
     *
     * @param password a palavra-passe a definir
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtém o identificador do perfil de acesso (idPerfilAcesso).
     *
     * @return o identificador do perfil de acesso (idPerfilAcesso)
     */
    public String getIdPerfilAcesso() {
        return idPerfilAcesso;
    }

    /**
     * Define o identificador do perfil de acesso (idPerfilAcesso).
     *
     * @param idPerfilAcesso o identificador do perfil de acesso (idPerfilAcesso) a definir
     */
    public void setIdPerfilAcesso(String idPerfilAcesso) {
        this.idPerfilAcesso = idPerfilAcesso;
    }

    /**
     * Obtém o identificador da loja (idLoja).
     *
     * @return o identificador da loja (idLoja)
     */
    public Integer getIdLoja() {
        return idLoja;
    }

    /**
     * Define o identificador da loja (idLoja).
     *
     * @param idLoja o identificador da loja (idLoja) a definir
     */
    public void setIdLoja(Integer idLoja) {
        this.idLoja = idLoja;
    }

    /**
     * Converte para objeto de domínio.
     *
     * @return o objeto de domínio
     */
    public Funcionario paraDominio() {
        return new Funcionario(id, nome, email, password, idPerfilAcesso, idLoja != null ? idLoja : 0);
    }
}
