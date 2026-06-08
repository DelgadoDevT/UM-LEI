package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando a password fornecida não cumpre os requisitos de força.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PasswordFracaException extends RuntimeException {
    /**
     * Constrói uma nova instância de PasswordFracaException.
     *
     * @param message a mensagem de detalhe
     */
    public PasswordFracaException(String message) {
        super(message);
    }
}
