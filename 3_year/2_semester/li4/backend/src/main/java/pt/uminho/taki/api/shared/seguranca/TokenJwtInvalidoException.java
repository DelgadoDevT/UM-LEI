package pt.uminho.taki.api.shared.seguranca;

/**
 * Exceção lançada quando um token JWT é inválido.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class TokenJwtInvalidoException extends RuntimeException {
    
    /**
     * Constrói uma nova TokenJwtInvalidoException com a mensagem especificada.
     * 
     * @param message a mensagem de detalhe
     */
    public TokenJwtInvalidoException(String message) {
        super(message);
    }

    /**
     * Constrói uma nova TokenJwtInvalidoException com a mensagem e a causa especificadas.
     * 
     * @param message a mensagem de detalhe
     * @param cause a causa
     */
    public TokenJwtInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
