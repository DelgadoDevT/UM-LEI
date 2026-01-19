package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a performance indicator for a restaurant on a specific date.
 * Tracks revenue, number of orders, and average preparation time.
 */
public class IndicadorDesempenho {
    private LocalDate data;
    private double faturacaoTotal;
    private int totalPedidos;
    private int tempoMedioPreparacao;

    /**
     * Default constructor. Creates an indicator for today with zero values.
     */
    public IndicadorDesempenho() {
        this.data = LocalDate.now();
        this.faturacaoTotal = 0.0;
        this.totalPedidos = 0;
        this.tempoMedioPreparacao = 0;
    }

    /**
     * Parameterized constructor.
     * @param data Date of the indicator
     * @param faturacaoTotal Total revenue
     * @param totalPedidos Total number of orders
     * @param tempoMedioPreparacao Average preparation time in minutes
     */
    public IndicadorDesempenho(LocalDate data, double faturacaoTotal, int totalPedidos, int tempoMedioPreparacao) {
        this.data = data;
        this.faturacaoTotal = faturacaoTotal;
        this.totalPedidos = totalPedidos;
        this.tempoMedioPreparacao = tempoMedioPreparacao;
    }

    /**
     * Copy constructor.
     * @param ind Performance indicator to copy
     */
    public IndicadorDesempenho(IndicadorDesempenho ind) {
        this.data = ind.data;
        this.faturacaoTotal = ind.getFaturacaoTotal();
        this.totalPedidos = ind.getTotalPedidos();
        this.tempoMedioPreparacao = ind.getTempoMedioPreparacao();
    }

    public LocalDate getData() {
        return data;
    }

    public double getFaturacaoTotal() {
        return faturacaoTotal;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public int getTempoMedioPreparacao() {
        return tempoMedioPreparacao;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setFaturacaoTotal(double faturacaoTotal) {
        this.faturacaoTotal = faturacaoTotal;
    }

    public void setTotalPedidos(int totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public void setTempoMedioPreparacao(int tempoMedioPreparacao) {
        this.tempoMedioPreparacao = tempoMedioPreparacao;
    }

    /**
     * Checks if this indicator falls within a date range.
     * @param dataInicio Start date
     * @param dataFim End date
     * @return true if indicator date is within range, false otherwise
     */
    public boolean indicadorValido(LocalDate dataInicio, LocalDate dataFim) {
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IndicadorDesempenho that = (IndicadorDesempenho) o;
        return Double.compare(faturacaoTotal, that.getFaturacaoTotal()) == 0 && totalPedidos == that.getTotalPedidos() && tempoMedioPreparacao == that.getTempoMedioPreparacao() && Objects.equals(data, that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, faturacaoTotal, totalPedidos, tempoMedioPreparacao);
    }

    @Override
    public String toString() {
        return String.format(
                "Indicador Desempenho | Data: %s \n" +
                        "   Faturação Total: %.2f €\n" +
                        "   Total Pedidos: %d\n" +
                        "   Tempo Médio Preparação: %d min\n" +
                        "   Média por Pedido: %.2f €",
                data,
                faturacaoTotal,
                totalPedidos,
                tempoMedioPreparacao,
                totalPedidos > 0 ? faturacaoTotal / totalPedidos : 0
        );
    }

    @Override
    public IndicadorDesempenho clone() {
        return new IndicadorDesempenho(this);
    }
}
