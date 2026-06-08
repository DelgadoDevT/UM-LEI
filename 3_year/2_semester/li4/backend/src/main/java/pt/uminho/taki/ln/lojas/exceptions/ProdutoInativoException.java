package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando um Produto se encontra inativo.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoInativoException extends RuntimeException {
    /**
     * Constrói uma nova instância de ProdutoInativoException.
     *
     * @param message a mensagem de detalhe
     */
    public ProdutoInativoException(String message) {
        super(message);
    }
}
