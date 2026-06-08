package pt.uminho.taki.ln.lojas;

/**
 * Enumeração que representa as taxas de IVA.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public enum TaxaIva {
    /** Taxa de IVA Normal de 23%. */
    NORMAL_23(0.23),
    /** Taxa de IVA Intermédia de 13%. */
    INTERMEDIA_13(0.13),
    /** Taxa de IVA Reduzida de 6%. */
    REDUZIDA_6(0.06),
    /** Taxa de IVA Isenta de 0%. */
    ISENTO_0(0.00);

    private final double valor;

    TaxaIva(double valor) {
        this.valor = valor;
    }

    /**
     * Obtém o valor da taxa de IVA.
     * 
     * @return o valor da taxa de IVA
     */
    public double getValor() {
        return valor;
    }

    /**
     * Obtém a enumeração TaxaIva a partir de um valor.
     * 
     * @param valor o valor da taxa de IVA
     * @return a enumeração TaxaIva
     */
    public static TaxaIva fromValor(double valor) {
        for (TaxaIva taxa : values()) {
            if (Double.compare(taxa.valor, valor) == 0) {
                return taxa;
            }
        }
        throw new IllegalArgumentException("Valor de taxa de IVA inválido: " + valor);
    }
}
