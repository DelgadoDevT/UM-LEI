package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

/**
 * Represents a bank card payment method.
 */
public class Cartao_Bancario extends Pagamento {
    /**
     * Default constructor.
     */
    public Cartao_Bancario() {
        super();
    }

    /**
     * Parameterized constructor.
     * @param pagamento Payment amount
     */
    public Cartao_Bancario(double pagamento) {
        super(pagamento);
    }

    /**
     * Copy constructor.
     * @param cb Bank card payment to copy
     */
    public Cartao_Bancario(Cartao_Bancario cb) {
        super(cb);
    }

    @Override
    public Cartao_Bancario clone() {
        return new Cartao_Bancario(this);
    }

    public String getMethod() {
        return "Cartão Bancário";
    }
}