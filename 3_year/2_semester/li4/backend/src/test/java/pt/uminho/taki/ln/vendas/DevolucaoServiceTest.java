package pt.uminho.taki.ln.vendas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.ln.lojas.Produto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DevolucaoServiceTest {

    @Mock
    private IDevolucaoObserver devolucaoObserverMock;
    @Mock
    private pt.uminho.taki.dao.DevolucaoDAO devolucaoDAOMock;
    @Mock
    private pt.uminho.taki.dao.VendaDAO vendaDAOMock;

    private DevolucaoService devolucaoService;

    @BeforeEach
    public void setUp() {
        devolucaoService = new DevolucaoService(devolucaoDAOMock);
        devolucaoService.adicionarObserver(devolucaoObserverMock);
    }

    @Test
    public void testProcessarDevolucao_PrazoValido_EmiteDevolucaoENotifica() throws Exception {
        // Arrange
        Venda vendaMock = new Venda();
        vendaMock.setIdVenda("VENDA-123");
        vendaMock.setDataHora(LocalDateTime.now().minusDays(10)); // Valida (menos de 30 dias)

        Produto p = new Produto();
        p.setIdProduto("PROD-X");
        p.setPrecoVenda(50.0);
        LinhaVenda lv = new LinhaVenda(p, 2, 0.0);
        
        List<LinhaVenda> devolvidos = new ArrayList<>();
        devolvidos.add(lv);
        vendaMock.setLinhas(devolvidos);

        // Act
        Devolucao devolucao = devolucaoService.processarDevolucao(vendaMock, devolvidos);

        // Assert
        assertNotNull(devolucao);
        assertEquals("VENDA-123", devolucao.getIdVenda());
        assertTrue(devolucao.getValor() > 0);
        
        verify(devolucaoObserverMock, times(1)).onDevolucaoConcluida(devolucao, devolvidos);
    }

    @Test
    public void testProcessarDevolucao_PrazoExcedido_LancaExcecao() {
        // Arrange
        Venda vendaMock = new Venda();
        vendaMock.setIdVenda("VENDA-OLD");
        vendaMock.setDataHora(LocalDateTime.now().minusDays(35)); // Invalida (mais de 30 dias)
        
        List<LinhaVenda> devolvidos = new ArrayList<>();
        devolvidos.add(new LinhaVenda());

        // Act & Assert
        assertThrows(PrazoDevolucaoExcedidoException.class, () -> {
            devolucaoService.processarDevolucao(vendaMock, devolvidos);
        });
        
        verify(devolucaoObserverMock, never()).onDevolucaoConcluida(any(), anyList());
    }

    @Test
    public void testProcessarDevolucaoPorNumeroFatura() throws Exception {
        // Arrange
        DevolucaoService service = new DevolucaoService(devolucaoDAOMock, vendaDAOMock);
        service.adicionarObserver(devolucaoObserverMock);

        Venda venda = new Venda();
        venda.setIdVenda("VENDA-FT");
        venda.setDataHora(LocalDateTime.now().minusDays(1));

        Produto p = new Produto();
        p.setIdProduto("PROD-FT");
        p.setPrecoVenda(10.0);
        LinhaVenda lv = new LinhaVenda(p, 1, 0.0);
        List<LinhaVenda> devolvidos = new ArrayList<>();
        devolvidos.add(lv);
        venda.setLinhas(devolvidos);

        when(vendaDAOMock.findByNumeroFatura("FT 2024/1")).thenReturn(java.util.Optional.of(venda));
        when(vendaDAOMock.getMetodoPagamento("VENDA-FT")).thenReturn(java.util.Optional.of("Cartão"));

        // Act
        Devolucao d = service.processarDevolucaoPorNumeroFatura("FT 2024/1", devolvidos);

        // Assert
        assertNotNull(d);
        assertEquals("VENDA-FT", d.getIdVenda());
        assertEquals("Cartão", d.getMetodoReembolso());
    }

    @Test
    public void testProcessarDevolucaoComMetodoAlternativo() throws Exception {
        // Arrange
        DevolucaoService service = new DevolucaoService(devolucaoDAOMock, vendaDAOMock);
        Venda venda = new Venda();
        venda.setIdVenda("VENDA-ALT");
        venda.setDataHora(LocalDateTime.now().minusDays(2));

        Produto p = new Produto();
        p.setIdProduto("PROD-ALT");
        p.setPrecoVenda(15.0);
        List<LinhaVenda> devolvidos = new ArrayList<>();
        devolvidos.add(new LinhaVenda(p, 1, 0.0));
        venda.setLinhas(devolvidos);

        when(vendaDAOMock.getMetodoPagamento("VENDA-ALT")).thenReturn(java.util.Optional.of("Cartão"));

        // Act
        Devolucao d = service.processarDevolucao(venda, devolvidos, "Numerário");

        // Assert
        assertEquals("Numerário", d.getMetodoReembolso());
    }
}
