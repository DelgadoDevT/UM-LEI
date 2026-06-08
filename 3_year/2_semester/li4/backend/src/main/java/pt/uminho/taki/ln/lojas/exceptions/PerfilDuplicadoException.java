package pt.uminho.taki.ln.lojas.exceptions;

/**
 * Exceção lançada ao tentar criar um perfil com nome já existente.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PerfilDuplicadoException extends RuntimeException {
    /**
     * Constrói uma nova instância de PerfilDuplicadoException.
     *
     * @param message a mensagem de detalhe
     */
    public PerfilDuplicadoException(String message) {
        super(message);
    }
}
