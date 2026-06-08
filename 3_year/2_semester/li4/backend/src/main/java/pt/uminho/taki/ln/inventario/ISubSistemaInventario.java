package pt.uminho.taki.ln.inventario;

/**
 * Interface do subsistema para a gestão de inventário.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaInventario {
    /**
     * Obtém o serviço de inventário.
     * 
     * @return o serviço de inventário
     */
    IInventarioService getInventarioService();

    /**
     * Obtém o serviço de movimentos de inventário.
     * 
     * @return o serviço de movimentos de inventário
     */
    IMovimentoInventarioService getMovimentoInventarioService();

    /**
     * Obtém o serviço de alertas de stock.
     * 
     * @return o serviço de alertas de stock
     */
    IAlertaStockService getAlertaStockService();

    /**
     * Obtém o serviço de estatísticas.
     * 
     * @return o serviço de estatísticas
     */
    IStatisticsService getStatisticsService();

    /**
     * Regista um movimento de stock.
     * 
     * @param movimento o movimento de stock a registar
     */
    void registarMovimento(MovimentoStock movimento);

    /**
     * Verifica se existe uma rutura de stock para um produto.
     * 
     * @param idProduto o identificador do produto
     * @return true se existir uma rutura, false caso contrário
     */
    boolean verificarRuturaStock(String idProduto);
}
