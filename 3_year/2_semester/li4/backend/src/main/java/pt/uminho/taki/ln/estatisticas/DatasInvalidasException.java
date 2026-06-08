package pt.uminho.taki.ln.estatisticas;

/**
 * Exceção lançada quando o intervalo temporal fornecido é inválido,
 * ou seja, quando a data de início é posterior à data de fim.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class DatasInvalidasException extends Exception {
    public DatasInvalidasException(String message) {
        super(message);
    }
}
