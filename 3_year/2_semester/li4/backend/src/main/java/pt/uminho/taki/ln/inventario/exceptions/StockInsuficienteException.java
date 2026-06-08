package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando a operação de saída excede a quantidade em stock.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class StockInsuficienteException extends RuntimeException {
    /**
     * Constrói uma nova instância de StockInsuficienteException.
     *
     * @param message a mensagem de detalhe
     */
    public StockInsuficienteException(String message) {
        super(message);
    }
}
