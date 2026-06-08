package pt.uminho.taki.ln.estatisticas;

import java.util.Objects;

/**
 * DTO de resposta para o Relatório de Vendas (RF16).
 * Agrega os totais de faturação para um dado conjunto de filtros.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class RelatorioVendasDTO {
    /** Volume total faturado em vendas. */
    private double volumeTotalVendas;
    /** Quantidade total de artigos vendidos. */
    private int quantidadeTotalArtigos;
    /** Valor médio das transações (Ticket Médio). */
    private double ticketMedio;

    public RelatorioVendasDTO(double volumeTotalVendas, int quantidadeTotalArtigos, double ticketMedio) {
        this.volumeTotalVendas = volumeTotalVendas;
        this.quantidadeTotalArtigos = quantidadeTotalArtigos;
        this.ticketMedio = ticketMedio;
    }

    public double getVolumeTotalVendas() { return volumeTotalVendas; }
    public int getQuantidadeTotalArtigos() { return quantidadeTotalArtigos; }
    public double getTicketMedio() { return ticketMedio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatorioVendasDTO that = (RelatorioVendasDTO) o;
        return Double.compare(that.volumeTotalVendas, volumeTotalVendas) == 0 &&
               quantidadeTotalArtigos == that.quantidadeTotalArtigos &&
               Double.compare(that.ticketMedio, ticketMedio) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volumeTotalVendas, quantidadeTotalArtigos, ticketMedio);
    }
}
