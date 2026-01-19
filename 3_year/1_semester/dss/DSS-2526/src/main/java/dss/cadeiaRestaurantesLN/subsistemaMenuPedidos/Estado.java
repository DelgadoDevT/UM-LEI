package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

/**
 * Enum representing the possible states of an order throughout its lifecycle.
 */
public enum Estado {
    /** Order created but not yet in preparation */
    REGISTADO,

    /** Order being prepared in the kitchen */
    EM_PREPARACAO,

    /** Order completed by kitchen, awaiting delivery */
    PRONTO,

    /** Order delivered to customer */
    ENTREGUE,

    /** Order cancelled (e.g., due to missing ingredients) */
    CANCELADO
}