package pt.uminho.taki.ln.inventario;

/**
 * Enumeracao que define os tipos de movimento de inventario.
 * Alinhada com os valores estritos da base de dados PostgreSQL.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public enum TipoMovimento {
    /** Incremento de stock. */
    ENTRADA,
    /** Decremento de stock por venda. */
    SAIDA,
    /** Decremento de stock por quebra ou perda. */
    QUEBRA;
}
