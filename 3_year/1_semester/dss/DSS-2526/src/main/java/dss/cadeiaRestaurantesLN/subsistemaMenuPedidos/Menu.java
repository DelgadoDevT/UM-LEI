package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract class representing a menu containing multiple products.
 * Can be extended as MenuPack (sellable bundles) or MenuCategoria (product categories).
 */
public abstract class Menu {
    private int id;
    private String nome;
    private Set<Produto> produtos;

    /**
     * Default constructor. Creates a menu with empty values.
     */
    public Menu() {
        this.id = 0;
        this.nome = "";
        this.produtos = new HashSet<>();
    }

    /**
     * Parameterized constructor.
     * @param id Menu ID
     * @param nome Menu name
     * @param produtos Set of products in the menu
     */
    public Menu(int id, String nome, Set<Produto> produtos) {
        this.id = id;
        this.nome = nome;
        this.produtos = produtos;
    }

    /**
     * Copy constructor.
     * @param menu Menu to copy
     */
    public Menu(Menu menu) {
        this.id = menu.getId();
        this.nome = menu.getNome();
        this.produtos = menu.getProdutos();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(Set<Produto> produtos) {
        this.produtos = produtos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return id == menu.id && Objects.equals(nome, menu.nome) && Objects.equals(produtos, menu.produtos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, produtos);
    }

    @Override
    public abstract Menu clone();

    @Override
    public String toString() {
        return "═══════════════════════════════\n" +
                "          Menu                 \n" +
                "───────────────────────────────\n" +
                "• ID:      " + id + "\n" +
                "• Nome:    " + nome + "\n" +
                "• Produtos:" + produtos + "\n" +
                "• Tipo:    " + getType() + "\n" +
                "═══════════════════════════════";
    }

    /**
     * Gets the type of menu (e.g., "Packs", "Categorias").
     * @return Menu type identifier
     */
    public abstract String getType();
}