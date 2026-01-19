package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an ingredient that can be used in products.
 * Each ingredient has a name, price, and set of allergens.
 */
public class Ingrediente {
    private String nome;
    private double preco;
    private Set<String> alergenios;

    /**
     * Default constructor. Creates an ingredient with empty values.
     */
    public Ingrediente() {
        this.nome = "";
        this.preco = 0.0;
        this.alergenios = new HashSet<>();
    }

    /**
     * Parameterized constructor.
     * @param nome Ingredient name
     * @param preco Ingredient price
     * @param alergenios Set of allergen names
     */
    public Ingrediente(String nome, double preco, Set<String> alergenios) {
        this.nome = nome;
        this.preco = preco;
        this.alergenios = new HashSet<>(alergenios);
    }

    /**
     * Copy constructor.
     * @param i Ingredient to copy
     */
    public Ingrediente(Ingrediente i) {
        this.nome = i.getNome();
        this.preco = i.getPreco();
        this.alergenios = new HashSet<>(i.getAlergenios());
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Set<String> getAlergenios() {
        return new HashSet<>(alergenios);
    }

    public void setAlergenios(Set<String> alergenios) {
        this.alergenios = new HashSet<>(alergenios);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingrediente that = (Ingrediente) o;
        return Double.compare(preco, that.getPreco()) == 0 && Objects.equals(nome, that.getNome()) && Objects.equals(alergenios, that.getAlergenios());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, preco, alergenios);
    }

    @Override
    public String toString() {
        return "Ingrediente{" +
                "nome='" + nome + '\'' +
                ", preço=" + preco +
                ", alergénios=" + alergenios +
                '}';
    }

    @Override
    public Ingrediente clone() {
        return new Ingrediente(this);
    }
}