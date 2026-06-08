package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando uma loja não é encontrada no sistema.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LojaNaoEncontradaException extends RuntimeException {

    /**
     * Constrói uma nova exceção com a mensagem especificada.
     *
     * @param mensagem a mensagem descritiva da causa da exceção
     */
    public LojaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
