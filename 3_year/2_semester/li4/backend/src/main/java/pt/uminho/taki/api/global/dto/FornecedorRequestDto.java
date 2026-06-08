package pt.uminho.taki.api.global.dto;

import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

/**
 * DTO (Objeto de Transferência de Dados) para pedidos de Fornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FornecedorRequestDto {

    private String idFornecedor;
    
    @NotBlank(message = "O nome não pode ser vazio")
    private String nome;
    
    @NotBlank(message = "O NIF não pode ser vazio")
    private String nif;
    
    @NotBlank(message = "O telefone não pode ser vazio")
    private String telefone;
    
    @NotBlank(message = "O email não pode ser vazio")
    @Email(message = "Formato de email inválido")
    private String email;
    
    private String estado;

    /**
     * Obtém o identificador do fornecedor (idFornecedor).
     *
     * @return o identificador do fornecedor (idFornecedor)
     */
    public String getIdFornecedor() {
        return idFornecedor;
    }

    /**
     * Define o identificador do fornecedor (idFornecedor).
     *
     * @param idFornecedor o identificador do fornecedor (idFornecedor) a definir
     */
    public void setIdFornecedor(String idFornecedor) {
        this.idFornecedor = idFornecedor;
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
     * Obtém o NIF.
     *
     * @return o NIF
     */
    public String getNif() {
        return nif;
    }

    /**
     * Define o NIF.
     *
     * @param nif o NIF a definir
     */
    public void setNif(String nif) {
        this.nif = nif;
    }

    /**
     * Obtém o telefone.
     *
     * @return o telefone
     */
    public String getTelefone() {
        return telefone;
    }

    /**
     * Define o telefone.
     *
     * @param telefone o telefone a definir
     */
    public void setTelefone(String telefone) {
        this.telefone = telefone;
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
     * Obtém o estado.
     *
     * @return o estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define o estado.
     *
     * @param estado o estado a definir
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Converte para objeto de domínio.
     *
     * @return o objeto de domínio
     */
    public Fornecedor paraDominio() {
        String estadoNormalizado = (estado == null || estado.isBlank()) ? "Ativo" : estado.trim();
        return new Fornecedor(idFornecedor, nome, nif, telefone, email, estadoNormalizado);
    }
}
