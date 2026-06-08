package pt.uminho.taki.ln.vendas;

/**
 * Implementação default do TPA.
 * Nesta camada valida apenas regras técnicas mínimas; a integração física pode ser substituída
 * por outra implementação de ITpaGateway.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class DefaultTpaGateway implements ITpaGateway {
    @Override
    public boolean autorizarPagamentoCartao(String idVenda, double valor) throws MetodoPagamentoIndisponivelException {
        if (idVenda == null || idVenda.isBlank()) {
            throw new MetodoPagamentoIndisponivelException("Falha no TPA: identificador de venda inválido.");
        }
        if (valor <= 0) {
            throw new MetodoPagamentoIndisponivelException("Falha no TPA: valor inválido para autorização.");
        }
        return true;
    }
}
