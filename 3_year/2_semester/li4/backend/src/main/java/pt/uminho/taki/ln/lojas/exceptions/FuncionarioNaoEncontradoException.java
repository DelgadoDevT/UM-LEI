package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando o funcionário não é encontrado no sistema.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FuncionarioNaoEncontradoException extends RuntimeException {
    /**
     * Constrói uma nova instância de FuncionarioNaoEncontradoException.
     *
     * @param message a mensagem de detalhe
     */
    public FuncionarioNaoEncontradoException(String message) {
        super(message);
    }
}
