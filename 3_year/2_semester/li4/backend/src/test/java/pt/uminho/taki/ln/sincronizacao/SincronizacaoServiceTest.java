package pt.uminho.taki.ln.sincronizacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.ln.sincronizacao.dto.OutboxEntry;
import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SincronizacaoServiceTest {

    @Mock
    private OutboxDAO outboxDAO;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private SincronizacaoService sincronizacaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sincronizacaoService = new SincronizacaoService(outboxDAO, httpClient);
    }

    @Test
    void exportarLoteParaSedeComSucesso() throws Exception {
        // Arrange
        List<OutboxEntry> listaPendentes = new ArrayList<>();
        listaPendentes.add(new OutboxEntry("produtos", "PROD-1", "INSERT"));
        when(outboxDAO.findAll()).thenReturn(listaPendentes);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        // Act
        assertDoesNotThrow(() -> sincronizacaoService.exportarLoteParaSede("timestamp"),
                "Sincronizacao pequena nao deve estourar com TimeOut e deve enviar HTTP 200.");

        // Assert: verifica remocao apos envio com sucesso
        verify(outboxDAO, times(1)).deleteAll(listaPendentes);
    }

    @Test
    void exportarLoteBloqueiaEmTimeoutMantendoPendentes() {
        // Arrange: Criar 501 entradas para forcar timeout
        List<OutboxEntry> limiteExcedido = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            limiteExcedido.add(new OutboxEntry("produtos", "PROD-" + i, "INSERT"));
        }
        when(outboxDAO.findAll()).thenReturn(limiteExcedido);

        // Act & Assert
        Exception exception = assertThrows(FalhaSincronizacaoException.class, () -> 
                sincronizacaoService.exportarLoteParaSede("timestamp"));
        
        assertTrue(exception.getMessage().contains("Timeout ao sincronizar base de dados"));
        
        // Verifica que nenhuma entrada foi deletada (mantem pendentes)
        verify(outboxDAO, never()).deleteAll(anyList());
    }

    @Test
    void exportarLoteFalhaServidorMantendoPendentes() throws Exception {
        // Arrange
        List<OutboxEntry> listaPendentes = new ArrayList<>();
        listaPendentes.add(new OutboxEntry("produtos", "PROD-1", "INSERT"));
        when(outboxDAO.findAll()).thenReturn(listaPendentes);

        when(httpResponse.statusCode()).thenReturn(500); // Erro do servidor
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        // Act & Assert
        Exception exception = assertThrows(FalhaSincronizacaoException.class, () -> 
                sincronizacaoService.exportarLoteParaSede("timestamp"));

        assertTrue(exception.getMessage().contains("Falha ao sincronizar base de dados após retries"));

        // Verifica que nenhuma entrada foi deletada
        verify(outboxDAO, never()).deleteAll(anyList());
    }

    @Test
    void importarAtualizacoesGlobaisComSucesso() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"status\":\"ok\"}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        // Act
        assertDoesNotThrow(() -> sincronizacaoService.importarAtualizacoesGlobais("TOKEN"),
                "A importacao deve ser bem sucedida se o servidor retornar 200.");
    }

    @Test
    void importarAtualizacoesGlobaisComErroServidor() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        // Act & Assert
        Exception exception = assertThrows(FalhaSincronizacaoException.class, () -> 
                sincronizacaoService.importarAtualizacoesGlobais("TOKEN"));

        assertTrue(exception.getMessage().contains("Erro ao importar dados da sede"));
    }

    @Test
    void verificarDisponibilidadeCentralComSucesso() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponse);

        assertTrue(sincronizacaoService.verificarDisponibilidadeCentral());
    }

    @Test
    void deveBloquearEndpointSemHttps() {
        assertThrows(IllegalArgumentException.class,
                () -> new SincronizacaoService(outboxDAO, httpClient, "http://central-insegura.local/api"));
    }
}
