package pt.uminho.taki.ln.estatisticas;

import java.util.Objects;

/**
 * DTO de resposta para o Dashboard de KPIs (RF18).
 * Contém os indicadores principais consolidados para o painel de gestão.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class DashboardKPIsDTO {
    private double volumeVendasDia;
    private double faturacaoTotal;
    private int numeroProdutosEmRutura;
    private double ticketMedioDia;

    /**
     * Construtor completo para DashboardKPIsDTO.
     *
     * @param volumeVendasDia o volume de vendas do dia
     * @param faturacaoTotal a faturação total acumulada
     * @param numeroProdutosEmRutura o número de produtos em rutura de stock
     * @param ticketMedioDia o valor médio das vendas no dia
     */
    public DashboardKPIsDTO(double volumeVendasDia, double faturacaoTotal, int numeroProdutosEmRutura, double ticketMedioDia) {
        this.volumeVendasDia = volumeVendasDia;
        this.faturacaoTotal = faturacaoTotal;
        this.numeroProdutosEmRutura = numeroProdutosEmRutura;
        this.ticketMedioDia = ticketMedioDia;
    }

    /**
     * Obtém o volume de vendas do dia.
     *
     * @return o volume de vendas do dia
     */
    public double getVolumeVendasDia() { return volumeVendasDia; }

    /**
     * Obtém a faturação total.
     *
     * @return a faturação total
     */
    public double getFaturacaoTotal() { return faturacaoTotal; }

    /**
     * Obtém o número de produtos em rutura.
     *
     * @return o número de produtos em rutura
     */
    public int getNumeroProdutosEmRutura() { return numeroProdutosEmRutura; }

    /**
     * Obtém o valor do ticket médio do dia.
     *
     * @return o ticket médio do dia
     */
    public double getTicketMedioDia() { return ticketMedioDia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardKPIsDTO that = (DashboardKPIsDTO) o;
        return Double.compare(that.volumeVendasDia, volumeVendasDia) == 0 &&
               Double.compare(that.faturacaoTotal, faturacaoTotal) == 0 &&
               numeroProdutosEmRutura == that.numeroProdutosEmRutura &&
               Double.compare(that.ticketMedioDia, ticketMedioDia) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volumeVendasDia, faturacaoTotal, numeroProdutosEmRutura, ticketMedioDia);
    }
}
