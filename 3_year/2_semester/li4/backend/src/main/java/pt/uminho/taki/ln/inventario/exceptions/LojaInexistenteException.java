package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando o identificador da loja é inválido ou inexistente.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LojaInexistenteException extends Exception {
    /**
     * Constrói uma nova instância de LojaInexistenteException.
     *
     * @param message a mensagem de detalhe
     */
    public LojaInexistenteException(String message) {
        super(message);
    }
}
