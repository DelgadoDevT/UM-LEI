package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando se tenta registar um funcionário com um email já existente.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EmailJaExisteException extends RuntimeException {
    /**
     * Constrói uma nova instância de EmailJaExisteException.
     *
     * @param message a mensagem de detalhe
     */
    public EmailJaExisteException(String message) {
        super(message);
    }
}
