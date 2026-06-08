package pt.uminho.taki.ln.inventario.exceptions;

/**
 * Exceção lançada quando um movimento manual de Saída ou Quebra carece de motivo.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MotivoObrigatorioException extends Exception {
    /**
     * Constrói uma nova instância de MotivoObrigatorioException.
     *
     * @param message a mensagem de detalhe
     */
    public MotivoObrigatorioException(String message) {
        super(message);
    }
}
