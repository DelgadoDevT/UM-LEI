package pt.uminho.taki.ln.vendas;

/**
 * Exceção lançada quando um método de pagamento selecionado não se encontra disponível.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MetodoPagamentoIndisponivelException extends Exception {
    /**
     * Constrói uma nova MetodoPagamentoIndisponivelException com a mensagem de detalhe especificada.
     *
     * @param mensagem a mensagem de detalhe.
     */
    public MetodoPagamentoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
