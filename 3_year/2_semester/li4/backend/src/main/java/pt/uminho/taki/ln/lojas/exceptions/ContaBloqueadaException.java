package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando a conta alvo da operação se encontra bloqueada.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ContaBloqueadaException extends RuntimeException {
    /**
     * Constrói uma nova instância de ContaBloqueadaException.
     *
     * @param message a mensagem de detalhe
     */
    public ContaBloqueadaException(String message) {
        super(message);
    }
}
