package pt.uminho.taki.ln.lojas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que define um perfil de acesso e as suas permissoes.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PerfilAcesso {
    private String id;
    private String nome;
    private List<Permissao> permissoes;

    /**
     * Construtor vazio para a classe PerfilAcesso.
     */
    public PerfilAcesso() {
        this.id = "";
        this.nome = "";
        this.permissoes = new ArrayList<>();
    }

    /**
     * Construtor completo para a classe PerfilAcesso.
     * @param id o identificador unico do perfil
     * @param nome o nome descritivo do perfil
     * @param permissoes a lista de permissoes associadas
     */
    public PerfilAcesso(String id, String nome, List<Permissao> permissoes) {
        this.id = id;
        this.nome = nome;
        this.permissoes = new ArrayList<>(permissoes);
    }

    /**
     * Construtor de copia para a classe PerfilAcesso.
     * Realiza copia profunda (deep copy) da lista de permissoes.
     * @param p o perfil a copiar
     */
    public PerfilAcesso(PerfilAcesso p) {
        this.id = p.getId();
        this.nome = p.getNome();
        this.permissoes = new ArrayList<>(p.getPermissoes());
    }

    /**
     * Obtém o identificador do perfil.
     * @return o identificador
     */
    public String getId() { return id; }
    /**
     * Define o identificador do perfil.
     * @param id o novo identificador
     */
    public void setId(String id) { this.id = id; }
    /**
     * Obtém o nome do perfil.
     * @return o nome
     */
    public String getNome() { return nome; }
    /**
     * Define o nome do perfil.
     * @param nome o novo nome
     */
    public void setNome(String nome) { this.nome = nome; }

    /**
     * Devolve uma copia da lista de permissoes.
     * @return lista de permissoes
     */
    public List<Permissao> getPermissoes() {
        return new ArrayList<>(this.permissoes);
    }

    /**
     * Define as permissoes do perfil.
     * @param permissoes nova lista de permissoes
     */
    public void setPermissoes(List<Permissao> permissoes) {
        this.permissoes = new ArrayList<>(permissoes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfilAcesso that = (PerfilAcesso) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(nome, that.nome) &&
               Objects.equals(permissoes, that.permissoes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, permissoes);
    }

    @Override
    public String toString() {
        return "PerfilAcesso{" +
               "id='" + id + '\'' +
               ", nome='" + nome + '\'' +
               ", permissoes=" + permissoes +
               '}';
    }

    @Override
    public PerfilAcesso clone() {
        return new PerfilAcesso(this);
    }
}
