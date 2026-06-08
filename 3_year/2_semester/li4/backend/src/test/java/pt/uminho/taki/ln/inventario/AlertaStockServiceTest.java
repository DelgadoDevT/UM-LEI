package pt.uminho.taki.ln.inventario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.StatisticsDAO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para o AlertaStockService.
 */
@ExtendWith(MockitoExtension.class)
public class AlertaStockServiceTest {

    @Mock
    private StatisticsDAO statisticsDAO;

    @InjectMocks
    private AlertaStockService alertaStockService;

    @Test
    @DisplayName("Deve processar e formatar corretamente os alertas de stock da loja")
    public void testVerificarAlertasComSucesso() {
        // Arrange
        int idLoja = 1;
        Map<String, Object> alertaRaw = new HashMap<>();
        alertaRaw.put("idLoja", idLoja);
        alertaRaw.put("codigoBarras", "123");
        alertaRaw.put("produto", "Cafe Moagem");
        alertaRaw.put("stockAtual", 2.0);
        alertaRaw.put("limiteMinimo", 5.0);

        when(statisticsDAO.getAlertasStockCritico(idLoja)).thenReturn(List.of(alertaRaw));

        // Act
        List<AlertaStock> result = alertaStockService.verificarAlertasLoja(idLoja);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        AlertaStock processado = result.get(0);
        assertNotNull(processado.getDataAlerta(), "O servico deve injetar a data do alerta.");
        assertTrue(processado.getMensagem().contains("Cafe Moagem"), "A mensagem deve conter o nome do produto.");
        assertTrue(processado.getMensagem().contains("2.00"), "A mensagem deve conter o stock atual.");
        
        verify(statisticsDAO, times(1)).getAlertasStockCritico(idLoja);
    }

    @Test
    @DisplayName("Deve devolver lista vazia se nao houver produtos em rutura")
    public void testVerificarAlertasListaVazia() {
        // Arrange
        int idLoja = 2;
        when(statisticsDAO.getAlertasStockCritico(idLoja)).thenReturn(Collections.emptyList());

        // Act
        List<AlertaStock> result = alertaStockService.verificarAlertasLoja(idLoja);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(statisticsDAO, times(1)).getAlertasStockCritico(idLoja);
    }
}
