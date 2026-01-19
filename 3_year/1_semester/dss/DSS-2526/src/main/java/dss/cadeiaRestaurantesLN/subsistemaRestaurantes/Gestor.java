package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

/**
 * Represents a manager employee responsible for overseeing restaurant operations,
 * sending messages to staff, and viewing performance indicators.
 */
public class Gestor extends Funcionario {

    /**
     * Default constructor.
     */
    public Gestor() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param id Employee ID
     * @param nome Employee name
     * @param nif Employee tax identification number
     */
    public Gestor(int id, String nome, int nif) {
        super(id, nome, nif);
    }

    /**
     * Copy constructor.
     * @param g Manager to copy
     */
    public Gestor(Gestor g) {
        super(g);
    }

    @Override
    public String getRole() {
        return "Gestor";
    }

    @Override
    public Gestor clone() {
        return new Gestor(this);
    }
}