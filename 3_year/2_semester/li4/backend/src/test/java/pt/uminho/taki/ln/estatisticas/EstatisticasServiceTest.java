package pt.uminho.taki.ln.estatisticas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.vendas.Venda;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstatisticasServiceTest {

    @Mock
    private VendaDAO vendaDAOMock;

    @Mock
    private InventarioDAO inventarioDAOMock;

    @Mock
    private pt.uminho.taki.dao.ProdutoDAO produtoDAOMock;

    private EstatisticasService service;

    @BeforeEach
    void setUp() {
        service = new EstatisticasService(vendaDAOMock, inventarioDAOMock, produtoDAOMock);
    }

    // -----------------------------------------------------------------------
    // RF16 — calcularVolumeVendas
    // -----------------------------------------------------------------------

    @Test
    void testCalcularVolumeVendas_PeriodoValido_RetornaSomaCorreta() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 1, 31, 23, 59);

        Venda v1 = new Venda(); v1.setDataHora(inicio.plusDays(5));  v1.setSubtotal(100.0); v1.setTotal(100.0);
        Venda v2 = new Venda(); v2.setDataHora(inicio.plusDays(10)); v2.setSubtotal(200.0); v2.setTotal(200.0);
        when(vendaDAOMock.findAll()).thenReturn(Arrays.asList(v1, v2));

        // Act
        double volume = service.calcularVolumeVendas(inicio, fim);

        // Assert
        assertEquals(300.0, volume, 0.001);
    }

    @Test
    void testCalcularVolumeVendas_DatasInvalidas_LancaExcecao() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 1, 1, 0, 0); // início > fim — inválido

        // Act & Assert
        assertThrows(DatasInvalidasException.class, () -> service.calcularVolumeVendas(inicio, fim));
        verifyNoInteractions(vendaDAOMock);
    }

    @Test
    void testCalcularVolumeVendas_SemVendas_RetornaZero() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 3, 31, 23, 59);
        when(vendaDAOMock.findAll()).thenReturn(Collections.emptyList());

        // Act
        double volume = service.calcularVolumeVendas(inicio, fim);

        // Assert
        assertEquals(0.0, volume, 0.001);
    }

    // -----------------------------------------------------------------------
    // RF16 — gerarRelatorioVendas
    // -----------------------------------------------------------------------

    @Test
    void testGerarRelatorioVendas_ComFiltroLoja_RetornaApenasVendasDaLoja() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 1, 31, 23, 59);

        Produto produto = new Produto();
        produto.setIdProduto("P1");
        produto.setPrecoVenda(50.0);

        Venda v1 = new Venda(); v1.setDataHora(inicio.plusDays(1)); v1.setSubtotal(100.0); v1.setTotal(100.0); v1.setIdLoja(1);
        v1.adicionarLinhaVenda(new LinhaVenda(produto, 2, 0.0));

        Venda v2 = new Venda(); v2.setDataHora(inicio.plusDays(2)); v2.setSubtotal(200.0); v2.setTotal(200.0); v2.setIdLoja(2);
        when(vendaDAOMock.findAll()).thenReturn(Arrays.asList(v1, v2));

        // Act
        RelatorioVendasDTO relatorio = service.gerarRelatorioVendas(inicio, fim, 1, null);

        // Assert
        assertEquals(100.0, relatorio.getVolumeTotalVendas(), 0.001);
        assertEquals(2, relatorio.getQuantidadeTotalArtigos());
        assertEquals(100.0, relatorio.getTicketMedio(), 0.001);
    }

    @Test
    void testGerarRelatorioVendas_SemVendas_RetornaZeros() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 3, 31, 23, 59);
        when(vendaDAOMock.findAll()).thenReturn(Collections.emptyList());

        // Act
        RelatorioVendasDTO relatorio = service.gerarRelatorioVendas(inicio, fim, null, null);

        // Assert
        assertEquals(0.0, relatorio.getVolumeTotalVendas(), 0.001);
        assertEquals(0, relatorio.getQuantidadeTotalArtigos());
        assertEquals(0.0, relatorio.getTicketMedio(), 0.001);
    }

    @Test
    void testGerarRelatorioVendas_ComFiltroCategoria() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 1, 31, 23, 59);

        Produto produto = new Produto();
        produto.setIdProduto("P-CAT");
        produto.setPrecoVenda(10.0);

        Venda v1 = new Venda();
        v1.setDataHora(inicio.plusDays(1));
        v1.setIdLoja(1);
        v1.adicionarLinhaVenda(new LinhaVenda(produto, 3, 0.0));

        when(vendaDAOMock.findAll()).thenReturn(List.of(v1));
        when(produtoDAOMock.getCategorias("P-CAT")).thenReturn(java.util.Set.of("CAT-A"));

        // Act
        RelatorioVendasDTO relatorio = service.gerarRelatorioVendas(inicio, fim, 1, "CAT-A");

        // Assert
        assertEquals(3, relatorio.getQuantidadeTotalArtigos());
        assertTrue(relatorio.getVolumeTotalVendas() > 0);
    }

    // -----------------------------------------------------------------------
    // RF17 — gerarRelatorioInventario
    // -----------------------------------------------------------------------

    @Test
    void testGerarRelatorioInventario_IdentificaProdutosEmRutura() {
        // Arrange
        Inventario ok     = new Inventario("INV-1", 100.0, 10.0, 1, "PROD-A"); // ok
        Inventario rutura = new Inventario("INV-2",   5.0, 10.0, 1, "PROD-B"); // abaixo do mínimo
        Inventario limite = new Inventario("INV-3",  10.0, 10.0, 1, "PROD-C"); // igual ao mínimo = rutura
        when(inventarioDAOMock.findAll()).thenReturn(Arrays.asList(ok, rutura, limite));

        Produto pA = new Produto(); pA.setIdProduto("PROD-A"); pA.setPrecoCusto(10.0);
        Produto pB = new Produto(); pB.setIdProduto("PROD-B"); pB.setPrecoCusto(20.0);
        Produto pC = new Produto(); pC.setIdProduto("PROD-C"); pC.setPrecoCusto(30.0);
        
        when(produtoDAOMock.findById("PROD-A")).thenReturn(Optional.of(pA));
        when(produtoDAOMock.findById("PROD-B")).thenReturn(Optional.of(pB));
        when(produtoDAOMock.findById("PROD-C")).thenReturn(Optional.of(pC));

        // Act
        RelatorioInventarioDTO relatorio = service.gerarRelatorioInventario(1);

        // Assert
        // Valorizacao: (100 * 10) + (5 * 20) + (10 * 30) = 1000 + 100 + 300 = 1400.0
        assertEquals(1400.0, relatorio.getValorizacaoTotalStock(), 0.001);
        List<String> emRutura = relatorio.getProdutosEmRutura();
        assertEquals(2, emRutura.size());
        assertTrue(emRutura.contains("PROD-B"));
        assertTrue(emRutura.contains("PROD-C"));
        assertFalse(emRutura.contains("PROD-A"));
    }

    @Test
    void testGerarRelatorioInventario_SemProdutos_ListaVazia() {
        // Arrange
        when(inventarioDAOMock.findAll()).thenReturn(Collections.emptyList());

        // Act
        RelatorioInventarioDTO relatorio = service.gerarRelatorioInventario(null);

        // Assert
        assertTrue(relatorio.getProdutosEmRutura().isEmpty());
        assertEquals(0.0, relatorio.getValorizacaoTotalStock(), 0.001);
    }

    // -----------------------------------------------------------------------
    // RF18 — calcularTicketMedio
    // -----------------------------------------------------------------------

    @Test
    void testCalcularTicketMedio_ComVendas_RetornaMedia() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 1, 31, 23, 59);

        Venda v1 = new Venda(); v1.setDataHora(inicio.plusDays(1)); v1.setSubtotal(100.0); v1.setTotal(100.0);
        Venda v2 = new Venda(); v2.setDataHora(inicio.plusDays(2)); v2.setSubtotal(300.0); v2.setTotal(300.0);
        when(vendaDAOMock.findAll()).thenReturn(Arrays.asList(v1, v2));

        // Act
        double ticket = service.calcularTicketMedio(inicio, fim);

        // Assert
        assertEquals(200.0, ticket, 0.001);
    }

    @Test
    void testCalcularTicketMedio_SemVendas_RetornaZero() throws Exception {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 3, 31, 23, 59);
        when(vendaDAOMock.findAll()).thenReturn(Collections.emptyList());

        // Act
        double ticket = service.calcularTicketMedio(inicio, fim);

        // Assert
        assertEquals(0.0, ticket, 0.001);
    }

    @Test
    void testCalcularTicketMedio_DatasInvalidas_LancaExcecao() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2026, 4, 1, 0, 0); // início > fim — inválido

        // Act & Assert
        assertThrows(DatasInvalidasException.class, () -> service.calcularTicketMedio(inicio, fim));
    }
}
