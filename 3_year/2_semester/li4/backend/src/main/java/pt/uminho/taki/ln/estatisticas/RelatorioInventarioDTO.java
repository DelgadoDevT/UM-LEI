package pt.uminho.taki.ln.estatisticas;

import java.util.List;
import java.util.Objects;

/**
 * DTO de resposta para o Relatório de Inventário (RF17).
 * Agrega a valorização financeira do stock e a lista de produtos em risco de rutura.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class RelatorioInventarioDTO {
    /** Valor total do stock em armazém. */
    private double valorizacaoTotalStock;
    /** Lista de identificadores de produtos em risco de rutura. */
    private List<String> produtosEmRutura; // Lista de idProduto em risco

    public RelatorioInventarioDTO(double valorizacaoTotalStock, List<String> produtosEmRutura) {
        this.valorizacaoTotalStock = valorizacaoTotalStock;
        this.produtosEmRutura = produtosEmRutura;
    }

    public double getValorizacaoTotalStock() { return valorizacaoTotalStock; }
    public List<String> getProdutosEmRutura() { return produtosEmRutura; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatorioInventarioDTO that = (RelatorioInventarioDTO) o;
        return Double.compare(that.valorizacaoTotalStock, valorizacaoTotalStock) == 0 &&
               Objects.equals(produtosEmRutura, that.produtosEmRutura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valorizacaoTotalStock, produtosEmRutura);
    }
}
