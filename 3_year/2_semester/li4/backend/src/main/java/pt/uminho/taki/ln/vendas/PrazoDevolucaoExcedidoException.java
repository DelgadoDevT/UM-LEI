package pt.uminho.taki.ln.vendas;

/**
 * Exceção lançada quando o prazo de devolução foi excedido.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PrazoDevolucaoExcedidoException extends Exception {
    /**
     * Constrói uma nova PrazoDevolucaoExcedidoException com a mensagem de detalhe especificada.
     *
     * @param mensagem a mensagem de detalhe.
     */
    public PrazoDevolucaoExcedidoException(String mensagem) {
        super(mensagem);
    }
}
