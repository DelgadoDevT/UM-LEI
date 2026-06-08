package pt.uminho.taki.ln.vendas;

/**
 * Interface de observador para vendas concluídas.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface IVendaObserver {
    /**
     * Chamado quando uma venda é concluída.
     * 
     * @param venda a venda concluída
     */
    void onVendaConcluida(Venda venda);
}
