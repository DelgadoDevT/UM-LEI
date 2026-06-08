package pt.uminho.taki.ln.estatisticas;

import java.time.LocalDateTime;

/**
 * Contrato do subsistema de estatísticas exposto ao TakiLN.
 * Fornece métodos para cálculo de volumes de venda, geração de relatórios e KPIs.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaEstatisticas {
    /**
     * Calcula o volume total de vendas num período.
     *
     * @param inicio a data de início do período
     * @param fim a data de fim do período
     * @return o volume total de vendas
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera um relatório detalhado de vendas.
     *
     * @param inicio a data de início
     * @param fim a data de fim
     * @param idLoja o identificador da loja (opcional)
     * @param categoria a categoria dos produtos (opcional)
     * @return o DTO do relatório de vendas
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    RelatorioVendasDTO gerarRelatorioVendas(LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria) throws DatasInvalidasException;

    /**
     * Gera um relatório do estado do inventário.
     *
     * @param idLoja o identificador da loja (opcional)
     * @return o DTO do relatório de inventário
     */
    RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja);

    /**
     * Calcula o valor do ticket médio de vendas.
     *
     * @param inicio a data de início
     * @param fim a data de fim
     * @return o valor do ticket médio
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException;

    /**
     * Gera o dashboard de KPIs para uma loja ou visão global.
     *
     * @param idLoja o identificador da loja (opcional)
     * @return o DTO com os KPIs do dashboard
     */
    DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja);
}
