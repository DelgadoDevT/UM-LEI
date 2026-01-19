package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

/**
 * Represents an MBWay mobile payment method.
 */
public class MBWay extends Pagamento {
    /**
     * Default constructor.
     */
    public MBWay() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param pagamento Payment amount
     */
    public MBWay(double pagamento) {
        super(pagamento);
    }

    /**
     * Copy constructor.
     * @param mbw MBWay payment to copy
     */
    public MBWay(MBWay mbw) {
        super(mbw);
    }

    @Override
    public MBWay clone() {
        return new MBWay(this);
    }

    public String getMethod() {
        return "MBWay";
    }
}