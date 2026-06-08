package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProdutoTest {

    @Test
    void deveArredondarPrecoVendaParaDuasCasasDecimais() {
        Produto produto = new Produto("P1", "5600000000001", "Produto Teste", "Descricao",
                1.01, 0.00, TaxaIva.NORMAL_23, "unidade", "Ativo");

        assertEquals(1.24, produto.calcularPrecoVenda(), 0.0001);
    }

    @Test
    void deveRejeitarPrecoCustoComMaisDeDuasCasasDecimais() {
        Produto produto = new Produto();

        assertThrows(IllegalArgumentException.class, () -> produto.setPrecoCusto(10.999));
    }

    @Test
    void deveRejeitarPrecoVendaComMaisDeDuasCasasDecimais() {
        Produto produto = new Produto();

        assertThrows(IllegalArgumentException.class, () -> produto.setPrecoVenda(19.999));
    }
}
