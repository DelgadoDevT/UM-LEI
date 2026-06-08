package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando a quantidade de um movimento é nula ou negativa.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class QuantidadeInvalidaException extends Exception {
    /**
     * Constrói uma nova instância de QuantidadeInvalidaException.
     *
     * @param message a mensagem de detalhe
     */
    public QuantidadeInvalidaException(String message) {
        super(message);
    }
}
