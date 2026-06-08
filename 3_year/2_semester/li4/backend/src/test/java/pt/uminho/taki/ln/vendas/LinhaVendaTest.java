package pt.uminho.taki.ln.vendas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import static org.junit.jupiter.api.Assertions.*;

public class LinhaVendaTest {

    private Produto produtoMock;

    @BeforeEach
    public void setUp() {
        // Arrange
        produtoMock = new Produto();
        produtoMock.setIdProduto("PROD01");
        produtoMock.setPrecoVenda(50.0);
        // TaxaIva.NORMAL_23 represents 23% in pt.uminho.taki.ln.lojas.TaxaIva?
        // Wait, TaxaIva is an enum. Assuming the default via setTaxaIva sets getTaxaIvaValor() to 0.23
        produtoMock.setTaxaIva(TaxaIva.NORMAL_23);
    }

    @Test
    public void testRecalcularValores_SemDesconto() {
        // Arrange
        int quantidade = 2;
        double desconto = 0.0;
        
        // Act
        LinhaVenda linhaVenda = new LinhaVenda(produtoMock, quantidade, desconto);

        // Assert
        // PrecoVenda = 50.0. Qty = 2. Subtotal = 100.0.
        // IVA 23%. Imposto = 23.0. TotalFinal = 123.0.
        assertEquals(100.0, linhaVenda.getSubtotal(), 0.001);
        assertEquals(23.0, linhaVenda.getTotalImposto(), 0.001);
        assertEquals(123.0, linhaVenda.getTotalFinal(), 0.001);
    }

    @Test
    public void testRecalcularValores_ComDescontoPercentual() {
        // Arrange
        int quantidade = 3;
        double desconto = 10.0; // 10%
        
        // Act
        LinhaVenda linhaVenda = new LinhaVenda(produtoMock, quantidade, desconto);

        // Assert
        // PrecoVenda = 50.0. Desconto: 5.0 unit. Preço pos desconto: 45.0. Qty = 3.
        // Subtotal = 135.0.
        // Imposto a 23% de 135.0 = 31.05. Total = 166.05.
        assertEquals(135.0, linhaVenda.getSubtotal(), 0.001);
        assertEquals(31.05, linhaVenda.getTotalImposto(), 0.001);
        assertEquals(166.05, linhaVenda.getTotalFinal(), 0.001);
    }
    
    @Test
    public void testRecalcularValores_QuantidadeInvalida() {
        // Arrange
        LinhaVenda linhaVenda = new LinhaVenda(produtoMock, -1, 0.0);
        
        // Act (is essentially Arrange, but let's re-trigger it)
        linhaVenda.recalcularValores();
        
        // Assert
        assertEquals(0.0, linhaVenda.getSubtotal(), 0.001);
        assertEquals(0.0, linhaVenda.getTotalFinal(), 0.001);
    }
}
