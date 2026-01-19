package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import java.util.Objects;

/**
 * Abstract class representing an employee in the restaurant chain.
 * Concrete implementations include Atendente (Attendant), Cozinheiro (Cook), and Gestor (Manager).
 */
public abstract class Funcionario {
    private int id;
    private String nome;
    private int nif;

    /**
     * Default constructor. Creates an employee with empty values.
     */
    public Funcionario() {
        this.id = 0;
        this.nome = "";
        this.nif = 0;
    }

    /**
     * Parameterized constructor.
     * @param id Employee ID
     * @param nome Employee name
     * @param nif Employee tax identification number
     */
    public Funcionario(int id, String nome, int nif) {
        this.id = id;
        this.nome = nome;
        this.nif = nif;
    }

    /**
     * Copy constructor.
     * @param f Employee to copy
     */
    public Funcionario(Funcionario f) {
        this.id = f.getId();
        this.nome = f.getNome();
        this.nif = f.getNif();
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

    public int getNif() {
        return nif;
    }

    public void setNif(int nif) {
        this.nif = nif;
    }

    /**
     * Gets the role/type of the employee.
     * @return Role identifier (e.g., "Atendente", "Cozinheiro", "Gestor")
     */
    public abstract String getRole();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return id == that.getId() && nif == that.getNif() && Objects.equals(nome, that.getNome());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, nif);
    }

    @Override
    public String toString() {
        return "ID: " + id + "\n" +
                "Nome: " + nome + "\n" +
                "NIF: " + nif + "\n" +
                "Tipo: " + getRole();
    }

    @Override
    public abstract Funcionario clone();
}