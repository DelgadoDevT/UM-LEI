package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando a data de registo de um movimento é no futuro.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class DataInvalidaException extends Exception {
    /**
     * Constrói uma nova instância de DataInvalidaException.
     *
     * @param message a mensagem de detalhe
     */
    public DataInvalidaException(String message) {
        super(message);
    }
}
