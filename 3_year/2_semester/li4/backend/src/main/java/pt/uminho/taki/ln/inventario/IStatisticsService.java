package pt.uminho.taki.ln.inventario;

import java.util.List;
import java.util.Map;

/**
 * Interface para o serviço de consulta de estatísticas.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IStatisticsService {
    /**
     * Obtém os indicadores principais (KPIs) de uma loja.
     * @param idLoja o identificador da loja
     * @return um mapa com os indicadores gerais
     */
    Map<String, Object> getKpiGerais(int idLoja);
    /**
     * Obtém as vendas mensais agrupadas.
     * @param idLoja o identificador da loja
     * @param meses o número de meses a retroceder
     * @return lista de resultados mensais
     */
    List<Map<String, Object>> getVendasMensais(int idLoja, int meses);
    /**
     * Obtém as vendas por hora para análise de afluência.
     * @param idLoja o identificador da loja
     * @return lista de resultados horários
     */
    List<Map<String, Object>> getVendasPorHora(int idLoja);
    /**
     * Obtém o desempenho de vendas por categoria.
     * @param idLoja o identificador da loja
     * @return lista de resultados por categoria
     */
    List<Map<String, Object>> getVendasPorCategoria(int idLoja);
}
