package pt.uminho.taki.ln.fornecimentos.exceptions;

/**
 * Exceção lançada quando um fornecedor já existe.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FornecedorExistenteException extends RuntimeException {
    /**
     * Constrói uma nova instância de FornecedorExistenteException com a mensagem de detalhe especificada.
     *
     * @param message a mensagem de detalhe
     */
    public FornecedorExistenteException(String message) {
        super(message);
    }
}
