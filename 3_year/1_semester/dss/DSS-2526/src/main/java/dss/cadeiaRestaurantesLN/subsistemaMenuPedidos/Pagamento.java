package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import java.util.Objects;

/**
 * Abstract class representing a payment method.
 * Concrete implementations include cash, bank card, and MBWay.
 */
public abstract class Pagamento {
    private double pagamento;
    protected int idPedido;

    /**
     * Default constructor. Creates a payment with zero amount.
     */
    public Pagamento() {
        this.pagamento = 0.0;
    }

    /**
     * Parameterized constructor.
     * @param pagamento Payment amount
     */
    public Pagamento(double pagamento) {
        this.pagamento = pagamento;
    }

    /**
     * Copy constructor.
     * @param p Payment to copy
     */
    public Pagamento(Pagamento p) {
        this.pagamento = p.getPagamento();
    }

    public double getPagamento() {
        return pagamento;
    }

    public void setPagamento(double pagamento) {
        this.pagamento = pagamento;
    }

    public int getIdPedido() {
        return this.idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento1 = (Pagamento) o;
        return Double.compare(pagamento, pagamento1.pagamento) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pagamento);
    }

    @Override
    public abstract Pagamento clone();

    @Override
    public String toString() {
        return "═══════════════════════════════\n" +
                "          Menu                 \n" +
                "───────────────────────────────\n" +
                "• Valor:  " + pagamento + "\n" +
                "• Método: " + getMethod() + "\n" +
                "═══════════════════════════════";
    }

    /**
     * Gets the payment method name.
     * @return Payment method identifier
     */
    public abstract String getMethod();
}