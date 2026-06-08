package pt.uminho.taki.ln.fornecimentos;

import java.util.Objects;

/**
 * Representa um fornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Fornecedor {
    private String idFornecedor;
    private String nome;
    private String nif;
    private String telefone;
    private String email;
    private String estado;

    /**
     * Constrói um novo Fornecedor vazio com valores por omissão.
     */
    public Fornecedor() {
        this.idFornecedor = "";
        this.nome = "";
        this.nif = "";
        this.telefone = "";
        this.email = "";
        this.estado = "Ativo";
    }

    /**
     * Constrói um novo Fornecedor com os detalhes especificados.
     *
     * @param idFornecedor o identificador único do fornecedor
     * @param nome o nome do fornecedor
     * @param nif o NIF do fornecedor
     * @param telefone o número de telefone do fornecedor
     * @param email o endereço de correio eletrónico do fornecedor
     * @param estado o estado do fornecedor
     */
    public Fornecedor(String idFornecedor, String nome, String nif, String telefone, String email, String estado) {
        this.idFornecedor = idFornecedor;
        this.nome = nome;
        this.nif = nif;
        this.telefone = telefone;
        this.email = email;
        this.estado = estado;
    }

    /**
     * Constrói um novo Fornecedor através da cópia de um existente.
     *
     * @param f o fornecedor a copiar
     */
    public Fornecedor(Fornecedor f) {
        this.idFornecedor = f.getIdFornecedor();
        this.nome = f.getNome();
        this.nif = f.getNif();
        this.telefone = f.getTelefone();
        this.email = f.getEmail();
        this.estado = f.getEstado();
    }

    /**
     * Obtém o identificador do fornecedor.
     *
     * @return o identificador do fornecedor
     */
    public String getIdFornecedor() { return idFornecedor; }

    /**
     * Define o identificador do fornecedor.
     *
     * @param idFornecedor o novo identificador
     */
    public void setIdFornecedor(String idFornecedor) { this.idFornecedor = idFornecedor; }
    
    /**
     * Obtém o nome do fornecedor.
     *
     * @return o nome do fornecedor
     */
    public String getNome() { return nome; }

    /**
     * Define o nome do fornecedor.
     *
     * @param nome o novo nome
     */
    public void setNome(String nome) { this.nome = nome; }
    
    /**
     * Obtém o NIF do fornecedor.
     *
     * @return o NIF do fornecedor
     */
    public String getNif() { return nif; }

    /**
     * Define o NIF do fornecedor.
     *
     * @param nif o novo NIF
     */
    public void setNif(String nif) { this.nif = nif; }
    
    /**
     * Obtém o número de telefone do fornecedor.
     *
     * @return o número de telefone do fornecedor
     */
    public String getTelefone() { return telefone; }

    /**
     * Define o número de telefone do fornecedor.
     *
     * @param telefone o novo número de telefone
     */
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    /**
     * Obtém o endereço de correio eletrónico do fornecedor.
     *
     * @return o endereço de correio eletrónico do fornecedor
     */
    public String getEmail() { return email; }

    /**
     * Define o endereço de correio eletrónico do fornecedor.
     *
     * @param email o novo endereço de correio eletrónico
     */
    public void setEmail(String email) { this.email = email; }
    
    /**
     * Obtém o estado do fornecedor.
     *
     * @return o estado do fornecedor
     */
    public String getEstado() { return estado; }

    /**
     * Define o estado do fornecedor.
     *
     * @param estado o novo estado
     */
    public void setEstado(String estado) { this.estado = estado; }
    
    /**
     * Verifica se o fornecedor está inativo.
     *
     * @return true se estiver inativo, false caso contrário
     */
    public boolean isInativo() {
        return "Inativo".equalsIgnoreCase(this.estado);
    }

    /**
     * Compara este fornecedor com o objeto especificado.
     *
     * @param o o objeto a comparar
     * @return true se os objetos forem iguais, false caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fornecedor that = (Fornecedor) o;
        return Objects.equals(idFornecedor, that.idFornecedor) &&
               Objects.equals(nome, that.nome) &&
               Objects.equals(nif, that.nif) &&
               Objects.equals(telefone, that.telefone) &&
               Objects.equals(email, that.email) &&
               Objects.equals(estado, that.estado);
    }

    /**
     * Retorna o valor do código hash para o fornecedor.
     *
     * @return o valor do código hash para este fornecedor
     */
    @Override
    public int hashCode() {
        return Objects.hash(idFornecedor, nome, nif, telefone, email, estado);
    }

    /**
     * Retorna uma representação em cadeia de caracteres do fornecedor.
     *
     * @return a representação em cadeia de caracteres do fornecedor
     */
    @Override
    public String toString() {
        return "Fornecedor{" +
               "idFornecedor='" + idFornecedor + "'" +
               ", nome='" + nome + "'" +
               ", nif='" + nif + "'" +
               ", telefone='" + telefone + "'" +
               ", email='" + email + "'" +
               ", estado='" + estado + "'" +
               "}";
    }

    /**
     * Cria e retorna uma cópia deste fornecedor.
     *
     * @return um clone deste fornecedor
     */
    @Override
    public Fornecedor clone() {
        return new Fornecedor(this);
    }
}
