package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando o funcionário indicado no movimento não existe no sistema.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FuncionarioInexistenteException extends Exception {
    /**
     * Constrói uma nova instância de FuncionarioInexistenteException.
     *
     * @param message a mensagem de detalhe
     */
    public FuncionarioInexistenteException(String message) {
        super(message);
    }
}
