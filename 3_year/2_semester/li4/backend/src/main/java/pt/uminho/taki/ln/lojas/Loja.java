package pt.uminho.taki.ln.lojas;

/**
 * Representa uma entidade de loja.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Loja {
    private Integer idLoja;
    private String nome;
    private String telefone;
    private String email;
    private String nif;
    private String rua;
    private String cidade;
    private String distrito;

    /**
     * Construtor por omissão para Loja.
     */
    public Loja() {
    }

    /**
     * Construtor parametrizado para Loja.
     *
     * @param idLoja o identificador da loja
     * @param nome o nome da loja
     * @param telefone o número de telefone
     * @param email o endereço de correio eletrónico
     * @param nif o NIF (Número de Identificação Fiscal)
     * @param rua a rua
     * @param cidade a cidade
     * @param distrito o distrito
     */
    public Loja(Integer idLoja, String nome, String telefone, String email, String nif, String rua, String cidade, String distrito) {
        this.idLoja = idLoja;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.nif = nif;
        this.rua = rua;
        this.cidade = cidade;
        this.distrito = distrito;
    }

    /**
     * Obtém o identificador da loja.
     *
     * @return o identificador da loja
     */
    public Integer getIdLoja() {
        return idLoja;
    }

    /**
     * Define o identificador da loja.
     *
     * @param idLoja o identificador da loja
     */
    public void setIdLoja(Integer idLoja) {
        this.idLoja = idLoja;
    }

    /**
     * Obtém o nome da loja.
     *
     * @return o nome da loja
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da loja.
     *
     * @param nome o nome da loja
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém o número de telefone.
     *
     * @return o número de telefone
     */
    public String getTelefone() {
        return telefone;
    }

    /**
     * Define o número de telefone.
     *
     * @param telefone o número de telefone
     */
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /**
     * Obtém o endereço de correio eletrónico.
     *
     * @return o endereço de correio eletrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o endereço de correio eletrónico.
     *
     * @param email o endereço de correio eletrónico
     */
    public void setEmail(String email) {
        this.email = email;
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
     * @param nif o NIF
     */
    public void setNif(String nif) {
        this.nif = nif;
    }

    /**
     * Obtém a rua.
     *
     * @return a rua
     */
    public String getRua() {
        return rua;
    }

    /**
     * Define a rua.
     *
     * @param rua a rua
     */
    public void setRua(String rua) {
        this.rua = rua;
    }

    /**
     * Obtém a cidade.
     *
     * @return a cidade
     */
    public String getCidade() {
        return cidade;
    }

    /**
     * Define a cidade.
     *
     * @param cidade a cidade
     */
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    /**
     * Obtém o distrito.
     *
     * @return o distrito
     */
    public String getDistrito() {
        return distrito;
    }

    /**
     * Define o distrito.
     *
     * @param distrito o distrito
     */
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
}
