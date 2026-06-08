package pt.uminho.taki.ln.estatisticas;

import java.time.LocalDateTime;

/**
 * Contrato do serviço de análise e estatísticas do sistema Taki.
 * Cobre os requisitos funcionais RF16 (Relatório de Vendas),
 * RF17 (Relatório de Inventário) e RF18 (Dashboard de KPIs).
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IEstatisticasService {

    /**
     * Calcula o volume total de vendas num período (RF16).
     * @param inicio data de início do período
     * @param fim    data de fim do período
     * @return o valor total faturado
     * @throws DatasInvalidasException caso a data de início seja posterior à de fim
     */
    double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera um relatório agregado de vendas para um período e filtros opcionais (RF16).
     * @param inicio    data de início
     * @param fim       data de fim
     * @param idLoja    identificador da loja (null para todas as lojas)
     * @param categoria filtro de categoria (null para todas)
     * @return DTO com volume total, quantidade de artigos e ticket médio
     * @throws DatasInvalidasException caso o intervalo temporal seja inválido
     */
    RelatorioVendasDTO gerarRelatorioVendas(LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria) throws DatasInvalidasException;

    /**
     * Gera um relatório do estado atual do inventário de uma loja (RF17).
     * Calcula a valorização financeira do stock e identifica produtos em rutura.
     * @param idLoja identificador da loja (null para todas as lojas)
     * @return DTO com valorização total e lista de IDs de produtos em risco de rutura
     */
    RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja);

    /**
     * Calcula o ticket médio de vendas para um período (KPI auxiliar - RF18).
     * @param inicio data de início
     * @param fim    data de fim
     * @return o ticket médio
     * @throws DatasInvalidasException caso o intervalo temporal seja inválido
     */
    double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera o painel de indicadores de desempenho (KPIs) para a data atual (RF18).
     * @param idLoja identificador da loja para filtrar (null para visão global)
     * @return DTO com os principais KPIs do dia
     */
    DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja);
}
