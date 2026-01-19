package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.Set;
import java.util.Objects;

/**
 * Represents a menu pack that can be sold as a bundle at a fixed price.
 * Menu packs contain multiple products and can be ordered as a single item.
 */
public class MenuPack extends Menu implements ArtigoVenda {
    private double preco;

    /**
     * Default constructor. Creates a menu pack with zero price.
     */
    public MenuPack() {
        super();
        this.preco = 0.0;
    }

    /**
     * Parameterized constructor.
     * @param id Menu pack ID
     * @param nome Menu pack name
     * @param produtos Set of products in the pack
     * @param valor Pack price
     */
    public MenuPack(int id, String nome, Set<Produto> produtos, double valor) {
        super(id, nome, produtos);
        this.preco = valor;
    }

    /**
     * Copy constructor.
     * @param mp Menu pack to copy
     */
    public MenuPack(MenuPack mp) {
        super(mp);
        this.preco = mp.preco;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double valor) {
        this.preco = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MenuPack menuPack = (MenuPack) o;
        return Double.compare(menuPack.preco, preco) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), preco);
    }

    @Override
    public String toString() {
        return super.toString().replace("═══════════════════════════════", "") +
                "• Preço:   " + preco + " €\n" +
                "═══════════════════════════════";
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public MenuPack clone() {
        return new MenuPack(this);
    }

    public String getType() {
        return "Packs";
    }
}