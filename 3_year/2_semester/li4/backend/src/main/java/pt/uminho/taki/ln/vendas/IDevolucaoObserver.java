package pt.uminho.taki.ln.vendas;

import java.util.List;

/**
 * Interface para o observador de devoluções.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IDevolucaoObserver {
    /**
     * Chamado quando uma devolução é concluída.
     *
     * @param devolucao a devolução
     * @param linhasDevolvidas as linhas devolvidas
     */
    void onDevolucaoConcluida(Devolucao devolucao, List<LinhaVenda> linhasDevolvidas);
}
