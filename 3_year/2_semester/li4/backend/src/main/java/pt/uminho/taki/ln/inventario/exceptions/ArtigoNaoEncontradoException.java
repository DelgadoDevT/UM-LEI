package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando o artigo ou registo de inventário não é encontrado.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ArtigoNaoEncontradoException extends Exception {
    /**
     * Constrói uma nova instância de ArtigoNaoEncontradoException.
     *
     * @param message a mensagem de detalhe
     */
    public ArtigoNaoEncontradoException(String message) {
        super(message);
    }
}
