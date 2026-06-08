package pt.uminho.taki.ln.vendas;

/**
 * Porta de integração para autorização de pagamentos eletrónicos (TPA).
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface ITpaGateway {
    /**
     * Solicita autorização de pagamento por cartão.
     * @param idVenda identificador da venda
     * @param valor montante a autorizar
     * @return true se autorizado
     * @throws MetodoPagamentoIndisponivelException se ocorrer falha de integração ou recusa
     */
    boolean autorizarPagamentoCartao(String idVenda, double valor) throws MetodoPagamentoIndisponivelException;
}
