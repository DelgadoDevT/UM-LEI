package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando um Produto não existe.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoInexistenteException extends Exception {
    /**
     * Constrói uma nova instância de ProdutoInexistenteException.
     *
     * @param message a mensagem de detalhe
     */
    public ProdutoInexistenteException(String message) {
        super(message);
    }
}
