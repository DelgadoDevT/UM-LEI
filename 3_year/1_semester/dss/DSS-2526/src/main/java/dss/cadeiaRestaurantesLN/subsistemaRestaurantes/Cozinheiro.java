package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

/**
 * Represents a cook employee responsible for preparing orders in the kitchen.
 */
public class Cozinheiro extends Funcionario {

    /**
     * Default constructor.
     */
    public Cozinheiro() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param id Employee ID
     * @param nome Employee name
     * @param nif Employee tax identification number
     */
    public Cozinheiro(int id, String nome, int nif) {
        super(id, nome, nif);
    }

    /**
     * Copy constructor.
     * @param c Cook to copy
     */
    public Cozinheiro(Cozinheiro c) {
        super(c);
    }

    @Override
    public String getRole() {
        return "Cozinheiro";
    }

    @Override
    public Cozinheiro clone() {
        return new Cozinheiro(this);
    }
}