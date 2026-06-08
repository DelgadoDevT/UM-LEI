package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada quando ocorre uma operação de categoria inválida.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CategoriaInvalidaException extends RuntimeException {
    /**
     * Constrói uma nova instância de CategoriaInvalidaException.
     *
     * @param message a mensagem de detalhe
     */
    public CategoriaInvalidaException(String message) {
        super(message);
    }
}
