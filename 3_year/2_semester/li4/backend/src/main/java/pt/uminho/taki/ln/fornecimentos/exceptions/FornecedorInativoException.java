package pt.uminho.taki.ln.fornecimentos.exceptions;

/**
 * Exceção lançada quando um fornecedor está inativo.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FornecedorInativoException extends RuntimeException {
    /**
     * Constrói uma nova instância de FornecedorInativoException com a mensagem de detalhe especificada.
     *
     * @param message a mensagem de detalhe
     */
    public FornecedorInativoException(String message) {
        super(message);
    }
}
