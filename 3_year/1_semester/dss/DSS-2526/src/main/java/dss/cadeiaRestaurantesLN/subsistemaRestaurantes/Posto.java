package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a work position in a restaurant (e.g., kitchen, counter).
 * Each position has a type, messages for employees, and assigned staff.
 */
public class Posto {
    private String tipo;
    private List<String> mensagens;
    private List<Funcionario> funcionarios;

    /**
     * Default constructor. Creates a position with empty values.
     */
    public Posto() {
        this.tipo = "";
        this.mensagens = new ArrayList<>();
        this.funcionarios = new ArrayList<>(); }

    /**
     * Parameterized constructor.
     * @param tipo Position type (e.g., "COZINHA", "ATENDIMENTO")
     * @param mensagens List of messages for this position
     * @param funcionarios List of employees assigned to this position
     */
    public Posto(String tipo, List<String> mensagens, List<Funcionario> funcionarios) {
        this.tipo = tipo;
        this.mensagens = new ArrayList<>(mensagens);
        this.funcionarios = new ArrayList<>(funcionarios);
    }

    /**
     * Copy constructor.
     * @param p Position to copy
     */
    public Posto(Posto p) {
        this.tipo = p.getTipo();
        this.mensagens = new ArrayList<>(p.getMensagens());
        this.funcionarios = new ArrayList<>(p.getFuncionarios());
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<String> getMensagens() {
        return new ArrayList<>(mensagens);
    }

    public void setMensagens(List<String> mensagens) {
        this.mensagens = new ArrayList<>(mensagens);
    }

    public List<Funcionario> getFuncionarios() {
        return new ArrayList<>(funcionarios);
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = new ArrayList<>(funcionarios);
    }

    /**
     * Adds a message to this position.
     * @param mensagem Message to add
     */
    public void adicionarMensagem(String mensagem) {
        mensagens.add(mensagem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Posto posto = (Posto) o;
        return Objects.equals(tipo, posto.tipo) &&
                Objects.equals(mensagens, posto.mensagens) &&
                Objects.equals(funcionarios, posto.funcionarios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, mensagens, funcionarios);
    }

    @Override
    public String toString() {
        return "Posto{" +
                "tipo='" + tipo + '\'' +
                ", mensagens=" + mensagens +
                ", funcionarios=" + funcionarios +
                '}';
    }

    @Override
    public Posto clone() {
        return new Posto(this);
    }
}