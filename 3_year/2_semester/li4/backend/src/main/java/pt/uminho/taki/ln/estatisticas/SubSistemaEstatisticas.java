package pt.uminho.taki.ln.estatisticas;

import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.ProdutoDAO;

import java.time.LocalDateTime;

/**
 * Subsistema de Estatísticas.
 * @author TakiLN Team
 * @since 1.0
 */
public class SubSistemaEstatisticas implements ISubSistemaEstatisticas {

    private final IEstatisticasService estatisticasService;

    /**
     * Construtor para SubSistemaEstatisticas.
     * @param vendaDAO o DAO de venda
     * @param inventarioDAO o DAO de inventário
     * @param produtoDAO o DAO de produto
     */
    public SubSistemaEstatisticas(VendaDAO vendaDAO, InventarioDAO inventarioDAO, ProdutoDAO produtoDAO) {
        this.estatisticasService = new EstatisticasService(vendaDAO, inventarioDAO, produtoDAO);
    }

    /**
     * Calcula o volume de vendas.
     * @param inicio o início
     * @param fim o fim
     * @return o valor decimal
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas.
     */
    @Override
    public double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        return estatisticasService.calcularVolumeVendas(inicio, fim);
    }

    /**
     * Gera o relatório de vendas.
     * @param inicio o início
     * @param fim o fim
     * @param idLoja o identificador da loja
     * @param categoria a categoria
     * @return o DTO do relatório de vendas
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas.
     */
    @Override
    public RelatorioVendasDTO gerarRelatorioVendas(LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria) throws DatasInvalidasException {
        return estatisticasService.gerarRelatorioVendas(inicio, fim, idLoja, categoria);
    }

    /**
     * Gera o relatório de inventário.
     * @param idLoja o identificador da loja
     * @return o DTO do relatório de inventário
     */
    @Override
    public RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja) {
        return estatisticasService.gerarRelatorioInventario(idLoja);
    }

    /**
     * Calcula o valor do ticket médio.
     * @param inicio o início
     * @param fim o fim
     * @return o valor decimal
     * @throws DatasInvalidasException se as datas fornecidas forem inválidas.
     */
    @Override
    public double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        return estatisticasService.calcularTicketMedio(inicio, fim);
    }

    /**
     * Gera o painel de indicadores de desempenho (KPI).
     * @param idLoja o identificador da loja
     * @return o DTO do painel de KPIs
     */
    @Override
    public DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja) {
        return estatisticasService.gerarDashboardKPIs(idLoja);
    }
}
