package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando as credenciais fornecidas não são válidas.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CredenciaisInvalidasException extends RuntimeException {
    /**
     * Constrói uma nova instância de CredenciaisInvalidasException.
     *
     * @param message a mensagem de detalhe
     */
    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
