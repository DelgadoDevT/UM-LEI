package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

/**
 * Represents an attendant employee responsible for taking orders and delivering them to customers.
 */
public class Atendente extends Funcionario {

    /**
     * Default constructor.
     */
    public Atendente() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param id Employee ID
     * @param nome Employee name
     * @param nif Employee tax identification number
     */
    public Atendente(int id, String nome, int nif) {
        super(id, nome, nif);
    }

    /**
     * Copy constructor.
     * @param a Attendant to copy
     */
    public Atendente(Atendente a) {
        super(a);
    }

    @Override
    public String getRole() {
        return "Atendente";
    }

    @Override
    public Atendente clone() {
        return new Atendente(this);
    }
}