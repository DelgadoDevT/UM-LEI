package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

/**
 * Represents a cash payment method.
 */
public class Dinheiro extends Pagamento {
    /**
     * Default constructor.
     */
    public Dinheiro() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param pagamento Payment amount
     */
    public Dinheiro(double pagamento) {
        super(pagamento);
    }

    /**
     * Copy constructor.
     * @param d Cash payment to copy
     */
    public Dinheiro(Dinheiro d) {
        super(d);
    }

    @Override
    public Dinheiro clone() {
        return new Dinheiro(this);
    }

    public String getMethod() {
        return "Dinheiro";
    }
}