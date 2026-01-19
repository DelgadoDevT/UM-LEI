package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a product that can be sold individually.
 * Products have ingredients that can be customized by customers.
 */
public class Produto implements ArtigoVenda {
    private int id;
    private String nome;
    private double preco;
    private Tamanho tamanho;
    private Set<Ingrediente> ingredientes;

    /**
     * Default constructor. Creates a product with empty values.
     */
    public Produto() {
        this.id = 0;
        this.nome = "";
        this.preco = 0.0;
        this.tamanho = null;
        this.ingredientes = new HashSet<>();
    }

    /**
     * Parameterized constructor.
     * @param id Product ID
     * @param nome Product name
     * @param preco Product price
     * @param tamanho Product size
     * @param ingredientes Set of ingredients in the product
     */
    public Produto(int id, String nome, double preco, Tamanho tamanho, Set<Ingrediente> ingredientes) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.tamanho = tamanho;
        this.ingredientes = ingredientes;
    }

    /**
     * Copy constructor.
     * @param p Product to copy
     */
    public Produto(Produto p) {
        this.id = p.getId();
        this.nome = p.getNome();
        this.preco = p.getPreco();
        this.tamanho = p.getTamanho();
        this.ingredientes = p.getIngredientes();
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

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Tamanho getTamanho() {
        return tamanho;
    }

    public void setTamanho(Tamanho tamanho) {
        this.tamanho = tamanho;
    }

    public Set<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(Set<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return id == produto.id && Double.compare(preco, produto.preco) == 0 && Objects.equals(nome, produto.nome) && Objects.equals(tamanho, produto.tamanho) && Objects.equals(ingredientes, produto.ingredientes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, preco, tamanho, ingredientes);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                ", tamanho='" + tamanho + '\'' +
                ", ingredientes=" + ingredientes +
                '}';
    }

    @Override
    public Produto clone() {
        return new Produto(this);
    }
}