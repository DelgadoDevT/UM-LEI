package pt.uminho.taki.ln.inventario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.ln.inventario.exceptions.ArtigoNaoEncontradoException;
import pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para o InventarioService com tratamento de erros robusto.
 */
@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {

    @Mock
    private InventarioDAO inventarioDAO;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventarioBase;

    @BeforeEach
    public void setUp() {
        this.inventarioBase = new Inventario("INV1", 10.0, 5.0, 1, "PROD1");
    }

    @Test
    @DisplayName("Deve registar um movimento de ENTRADA com sucesso")
    public void testRegistoEntradaSucesso() throws Exception {
        MovimentoInventario entrada = new MovimentoInventario("M1", TipoMovimento.ENTRADA, 5.0, LocalDateTime.now(), "Reposicao", "INV1", "F1");
        when(inventarioDAO.findById("INV1")).thenReturn(Optional.of(new Inventario(inventarioBase)));

        inventarioService.registarMovimentoManual(entrada);

        verify(inventarioDAO, times(1)).save(eq("INV1"), argThat(i -> i.getQuantidade() == 15.0));
        verify(inventarioDAO, times(1)).addMovimento(eq("PROD1"), argThat(m ->
                Double.valueOf(5.0).equals(m.getQuantidade())
                        && m.getTipo() == TipoMovimento.ENTRADA
        ));
    }

    @Test
    @DisplayName("Deve falhar registo manual se o inventario nao existe (ArtigoNaoEncontradoException)")
    public void testRegistoManualFalhaInventarioInexistente() {
        MovimentoInventario m = new MovimentoInventario("M1", TipoMovimento.ENTRADA, 5.0, LocalDateTime.now(), "Reposicao", "INV_NAO_EXISTE", "F1");
        when(inventarioDAO.findById("INV_NAO_EXISTE")).thenReturn(Optional.empty());

        assertThrows(ArtigoNaoEncontradoException.class, () -> {
            inventarioService.registarMovimentoManual(m);
        });
        verify(inventarioDAO, never()).save(anyString(), any(Inventario.class));
    }

    @Test
    @DisplayName("Deve falhar SAIDA por stock insuficiente e nao gravar no DAO")
    public void testRegistoSaidaFalhaStockInsuficiente() {
        MovimentoInventario saida = new MovimentoInventario("M3", TipoMovimento.SAIDA, 15.0, LocalDateTime.now(), "Venda Manual", "INV1", "F1");
        when(inventarioDAO.findById("INV1")).thenReturn(Optional.of(new Inventario(inventarioBase)));

        assertThrows(StockInsuficienteException.class, () -> {
            inventarioService.registarMovimentoManual(saida);
        });
        
        verify(inventarioDAO, never()).save(anyString(), any(Inventario.class));
    }

    @Test
    @DisplayName("Deve atualizar o limite de seguranca com sucesso")
    public void testAtualizarLimiteSegurancaSucesso() throws Exception {
        when(inventarioDAO.findById("INV1")).thenReturn(Optional.of(new Inventario(inventarioBase)));

        inventarioService.definirLimiteSeguranca("INV1", 60.0);

        verify(inventarioDAO, times(1)).save(eq("INV1"), argThat(i -> i.getQuantidadeMinima() == 60.0));
    }

    @Test
    @DisplayName("Deve falhar atualizacao de limite se o inventario nao existe")
    public void testAtualizarLimiteFalhaInexistente() {
        when(inventarioDAO.findById("INV_ERRADO")).thenReturn(Optional.empty());

        assertThrows(ArtigoNaoEncontradoException.class, () -> {
            inventarioService.definirLimiteSeguranca("INV_ERRADO", 60.0);
        });
        verify(inventarioDAO, never()).save(anyString(), any(Inventario.class));
    }

    @Test
    @DisplayName("Deve exportar stock para CSV com cabeçalho")
    public void testExportarStockCsv() {
        when(inventarioDAO.findAll()).thenReturn(List.of(inventarioBase));

        String csv = inventarioService.exportarStockCsv();

        assertTrue(csv.startsWith("id_inventario,id_loja,id_produto,quantidade,quantidade_minima"));
        assertTrue(csv.contains("INV1,1,PROD1,10.0,5.0"));
    }

    @Test
    @DisplayName("Deve exportar stock para JSON")
    public void testExportarStockJson() {
        when(inventarioDAO.findAll()).thenReturn(List.of(inventarioBase));

        String json = inventarioService.exportarStockJson();

        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"idInventario\":\"INV1\""));
        assertTrue(json.contains("\"idProduto\":\"PROD1\""));
    }
}
