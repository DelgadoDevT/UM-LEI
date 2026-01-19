package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.Set;
import java.util.Objects;

/**
 * Represents a menu category for organizing products.
 * Categories group related products together for display purposes.
 */
public class MenuCategoria extends Menu {
    private String nomeCategoria;

    /**
     * Default constructor. Creates a menu category with empty values.
     */
    public MenuCategoria() {
        super();
        this.nomeCategoria = "";
    }

    /**
     * Parameterized constructor.
     * @param id Menu category ID
     * @param nome Menu name
     * @param produtos Set of products in the category
     * @param nomeCategoria Category name
     */
    public MenuCategoria(int id, String nome, Set<Produto> produtos, String nomeCategoria) {
        super(id, nome, produtos);
        this.nomeCategoria = nomeCategoria;
    }

    /**
     * Copy constructor.
     * @param mc Menu category to copy
     */
    public MenuCategoria(MenuCategoria mc) {
        super(mc);
        this.nomeCategoria = mc.nomeCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MenuCategoria that = (MenuCategoria) o;
        return Objects.equals(nomeCategoria, that.nomeCategoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nomeCategoria);
    }

    @Override
    public String toString() {
        return super.toString().replace("═══════════════════════════════", "") +
                "• Categoria: " + nomeCategoria + "\n" +
                "═══════════════════════════════";
    }

    @Override
    public MenuCategoria clone() {
        return new MenuCategoria(this);
    }

    public String getType() {
        return "Categorias";
    }
}