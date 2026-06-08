package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando um Produto já existe.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoExistenteException extends RuntimeException {
    /**
     * Constrói uma nova instância de ProdutoExistenteException.
     *
     * @param message a mensagem de detalhe
     */
    public ProdutoExistenteException(String message) {
        super(message);
    }
}
