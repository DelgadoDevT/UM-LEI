package pt.uminho.taki.ln.fornecimentos.exceptions;

/**
 * Exceção lançada quando existem campos obrigatórios em falta.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CamposObrigatoriosEmFaltaException extends RuntimeException {
    /**
     * Constrói uma nova instância de CamposObrigatoriosEmFaltaException.
     *
     * @param message a mensagem de detalhe
     */
    public CamposObrigatoriosEmFaltaException(String message) {
        super(message);
    }
}
