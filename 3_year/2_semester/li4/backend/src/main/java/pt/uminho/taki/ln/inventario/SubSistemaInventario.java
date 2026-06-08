package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.StatisticsDAO;

/**
 * Subsistema de Inventário.
 * @author TakiLN Team
 * @since 1.0
 */
public class SubSistemaInventario implements ISubSistemaInventario {
    
    private final IInventarioService inventarioService;
    private final IMovimentoInventarioService movimentoInventarioService;
    private final IAlertaStockService alertaStockService;
    private final IStatisticsService statisticsService;

    /**
     * Construtor para SubSistemaInventario.
     * @param inventarioDAO o DAO de inventário
     * @param statisticsDAO o DAO de estatísticas
     */
    public SubSistemaInventario(InventarioDAO inventarioDAO, StatisticsDAO statisticsDAO) {
        this.inventarioService = new InventarioService(inventarioDAO);
        this.movimentoInventarioService = new MovimentoInventarioService(inventarioDAO);
        this.alertaStockService = new AlertaStockService(statisticsDAO);
        this.statisticsService = new StatisticsService(statisticsDAO);
    }

    /**
     * Obtém o serviço de inventário.
     * @return o serviço IInventarioService
     */
    @Override
    public IInventarioService getInventarioService() {
        return this.inventarioService;
    }

    /**
     * Obtém o serviço de movimento de inventário.
     * @return o serviço IMovimentoInventarioService
     */
    @Override
    public IMovimentoInventarioService getMovimentoInventarioService() {
        return this.movimentoInventarioService;
    }

    /**
     * Obtém o serviço de alerta de stock.
     * @return o serviço IAlertaStockService
     */
    @Override
    public IAlertaStockService getAlertaStockService() {
        return this.alertaStockService;
    }

    /**
     * Obtém o serviço de estatísticas.
     * @return o serviço IStatisticsService
     */
    @Override
    public IStatisticsService getStatisticsService() {
        return this.statisticsService;
    }

    /**
     * Regista um movimento de stock.
     * @param movimento o movimento de stock
     */
    @Override
    public void registarMovimento(MovimentoStock movimento) {
        // Implementacao delegada se necessario
    }

    /**
     * Verifica a rutura de stock de um produto.
     * @param idProduto o identificador do produto
     * @return o valor booleano
     */
    @Override
    public boolean verificarRuturaStock(String idProduto) { 
        return false; 
    }
}
