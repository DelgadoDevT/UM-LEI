package pt.uminho.taki.ln.vendas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.dao.VendaDAO;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VendaServiceTest {

    @Mock
    private IVendaObserver vendaObserverMock;
    @Mock
    private IPromocaoService promocaoServiceMock;
    @Mock
    private VendaDAO vendaDAOMock;
    @Mock
    private ITpaGateway tpaGatewayMock;

    private VendaService vendaService;
    private Produto produtoMock;

    @BeforeEach
    public void setUp() {
        // Arrange general items
        vendaService = new VendaService(promocaoServiceMock, vendaDAOMock);
        vendaService.adicionarObserver(vendaObserverMock);
        
        produtoMock = new Produto();
        produtoMock.setIdProduto("PROD-ABC");
        produtoMock.setPrecoVenda(50.0);
    }

    @Test
    public void testIniciarVenda_EstadoPendente() {
        // Act
        Venda venda = vendaService.iniciarVenda(1, "Func123");

        // Assert
        assertNotNull(venda);
        assertEquals("Pendente", venda.getEstado());
        assertEquals(1, venda.getIdLoja());
        assertEquals("Func123", venda.getIdFuncionario());
    }

    @Test
    public void testAdicionarLinha_AplicaDescontoAutomatico() {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(10.0);

        // Act
        vendaService.adicionarLinha(venda, produtoMock, 2);

        // Assert
        assertEquals(1, venda.getLinhas().size());
        assertEquals(10.0, venda.getLinhas().get(0).getDesconto());
    }

    @Test
    public void testProcessarVenda_FinalizaENotificaObservers() throws Exception {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        vendaService.adicionarLinha(venda, produtoMock, 1);

        // Act
        vendaService.processarVenda(venda, "Cartão");

        // Assert
        assertEquals("Concluída", venda.getEstado());
        // Verify observer invocation exactly once
        verify(vendaObserverMock, times(1)).onVendaConcluida(venda);
    }
    
    @Test
    public void testProcessarVenda_MetodoInvalido_LancaExcecao() {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        vendaService.adicionarLinha(venda, produtoMock, 1);

        // Act & Assert
        assertThrows(MetodoPagamentoIndisponivelException.class, () -> {
            vendaService.processarVenda(venda, "");
        });
    }

    @Test
    public void testProcessarVendaNumerarioComTroco() throws Exception {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        vendaService.adicionarLinha(venda, produtoMock, 1);

        // Act
        vendaService.processarVenda(venda, "Numerário", 100.0);

        // Assert
        assertEquals("Concluída", venda.getEstado());
        verify(vendaObserverMock, times(1)).onVendaConcluida(venda);
    }

    @Test
    public void testProcessarVendaNumerarioMesmoValorSemTrocoComPrecisaoDecimal() throws Exception {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        vendaService.adicionarLinha(venda, produtoMock, 1);
        venda.setTotal(1.7800000000000002);

        // Act + Assert
        assertDoesNotThrow(() -> vendaService.processarVenda(venda, "Numerário", 1.78));
        verify(vendaDAOMock, times(1)).saveComPagamento(
                eq(venda.getIdVenda()),
                eq(venda),
                eq("Numerário"),
                eq(1.78),
                eq(0.0)
        );
    }

    @Test
    public void testProcessarVendaDinheiroNormalizaParaNumerario() throws Exception {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        vendaService.adicionarLinha(venda, produtoMock, 1);
        double valorEntregue = 100.0;
        double trocoEsperado = valorEntregue - venda.getTotal();

        // Act
        vendaService.processarVenda(venda, "Dinheiro", valorEntregue);

        // Assert
        verify(vendaDAOMock, times(1)).saveComPagamento(
                eq(venda.getIdVenda()),
                eq(venda),
                eq("Numerário"),
                eq(valorEntregue),
                eq(trocoEsperado)
        );
    }

    @Test
    public void testProcessarVendaMultibancoNormalizaParaCartao() throws Exception {
        // Arrange
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        vendaService.adicionarLinha(venda, produtoMock, 1);
        double totalEsperado = venda.getTotal();

        // Act
        vendaService.processarVenda(venda, "Multibanco");

        // Assert
        verify(vendaDAOMock, times(1)).saveComPagamento(
                eq(venda.getIdVenda()),
                eq(venda),
                eq("Cartão"),
                eq(totalEsperado),
                eq(0.0)
        );
    }

    @Test
    public void testProcessarVendaCartaoFalhaNoTPA() throws Exception {
        // Arrange
        vendaService = new VendaService(promocaoServiceMock, vendaDAOMock, tpaGatewayMock);
        vendaService.adicionarObserver(vendaObserverMock);
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);
        when(tpaGatewayMock.autorizarPagamentoCartao(anyString(), anyDouble())).thenReturn(false);
        vendaService.adicionarLinha(venda, produtoMock, 1);

        // Act + Assert
        assertThrows(MetodoPagamentoIndisponivelException.class,
                () -> vendaService.processarVenda(venda, "Cartão"));
        verify(vendaDAOMock, never()).saveComPagamento(anyString(), any(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    public void testProcessarVendaCincoItensAbaixoDeDezSegundos() {
        Venda venda = vendaService.iniciarVenda(1, "Func123");
        when(promocaoServiceMock.calcularMelhorDesconto(produtoMock)).thenReturn(0.0);

        for (int i = 0; i < 5; i++) {
            vendaService.adicionarLinha(venda, produtoMock, 1);
        }

        assertTimeout(Duration.ofSeconds(10), () -> vendaService.processarVenda(venda, "Numerário", 1000.0));
    }
}
