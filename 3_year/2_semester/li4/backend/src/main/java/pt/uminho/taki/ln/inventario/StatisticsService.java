package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.dao.StatisticsDAO;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Serviço responsável pela gestão e consulta de estatísticas de inventário e vendas.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class StatisticsService implements IStatisticsService {
    private final StatisticsDAO statisticsDAO;

    /**
     * Construtor para o StatisticsService.
     *
     * @param statisticsDAO o DAO de estatísticas
     */
    public StatisticsService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    /**
     * Obtém os indicadores de desempenho (KPIs) gerais para uma loja ou para todas as lojas.
     *
     * @param idLoja o identificador da loja (0 para todas as lojas)
     * @return um mapa contendo os KPIs (vendasHoje, receitaHoje, valorStock)
     */
    @Override
    public Map<String, Object> getKpiGerais(int idLoja) {
        Map<String, Object> kpis = new HashMap<>();
        if (idLoja == 0) {
            List<Map<String, Object>> todos = statisticsDAO.getKpiDiarioTodasLojas();
            double totalDia = todos.stream().mapToDouble(m -> (Double)m.get("totalFaturadoDia")).sum();
            int vendasDia = todos.stream().mapToInt(m -> (Integer)m.get("contagemVendasDia")).sum();
            
            // Subtrai devoluções
            totalDia -= statisticsDAO.getTotalDevolucoesHoje(0);
            
            kpis.put("vendasHoje", vendasDia);
            kpis.put("receitaHoje", Math.max(0.0, totalDia));
        } else {
            Map<String, Object> diario = statisticsDAO.getKpiDiario(idLoja);
            double totalDia = (Double) diario.getOrDefault("totalFaturadoDia", 0.0);
            
            // Subtrai devoluções
            totalDia -= statisticsDAO.getTotalDevolucoesHoje(idLoja);
            
            kpis.put("vendasHoje", diario.getOrDefault("contagemVendasDia", 0));
            kpis.put("receitaHoje", Math.max(0.0, totalDia));
        }
        kpis.put("valorStock", statisticsDAO.getValorizacaoEstoque(idLoja));
        return kpis;
    }

    /**
     * Obtém as vendas mensais para uma determinada loja num período de meses.
     *
     * @param idLoja o identificador da loja
     * @param meses o número de meses a retroceder
     * @return uma lista de mapas com a informação das vendas mensais
     */
    @Override
    public List<Map<String, Object>> getVendasMensais(int idLoja, int meses) {
        return statisticsDAO.getVendasMensais(idLoja, meses);
    }

    /**
     * Obtém as vendas por hora para o dia corrente numa determinada loja.
     *
     * @param idLoja o identificador da loja
     * @return uma lista de mapas com a informação das vendas por hora
     */
    @Override
    public List<Map<String, Object>> getVendasPorHora(int idLoja) {
        return statisticsDAO.getVendasPorHora(idLoja);
    }

    /**
     * Obtém o desempenho de vendas por categoria numa determinada loja.
     *
     * @param idLoja o identificador da loja
     * @return uma lista de mapas com o desempenho por categoria
     */
    @Override
    public List<Map<String, Object>> getVendasPorCategoria(int idLoja) {
        return statisticsDAO.getDesempenhoCategorias(idLoja);
    }
}
