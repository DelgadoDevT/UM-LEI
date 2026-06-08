package pt.uminho.taki.api.global;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pt.uminho.taki.api.global.dto.MensagemResponseDto;
import pt.uminho.taki.ln.ITakiLNGlobal;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.Loja;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o GlobalApiController.
 * Foca nas operações sobre lojas e na funcionalidade de rejeição.
 * 
 * @author TakiLN Team
 */
class GlobalApiControllerTest {

    private GlobalApiController controller;
    private ITakiLNGlobal takiLNGlobalMock;
    private ISubSistemaLojas subSistemaLojasMock;
    private Context ctxMock;

    @BeforeEach
    void setUp() {
        takiLNGlobalMock = mock(ITakiLNGlobal.class);
        subSistemaLojasMock = mock(ISubSistemaLojas.class);
        
        // O construtor do GlobalApiController requer DAOs e Services,
        // mas para testar a lógica do controller que chama ITakiLNGlobal e ISubSistemaLojas,
        // criamos uma instância e injetamos os mocks.
        // O construtor é: GlobalApiController(ITakiLNGlobal, ISubSistemaLojas, ...)
        // Vamos usar mock(GlobalApiController.class) e chamar real methods? 
        // Não, instanciamos passando mocks para as dependências necessárias.
        controller = new GlobalApiController(
            takiLNGlobalMock,
            subSistemaLojasMock,
            mock(pt.uminho.taki.dao.ProdutoDAO.class),
            mock(pt.uminho.taki.dao.VendaDAO.class),
            mock(pt.uminho.taki.dao.EncomendaDAO.class),
            mock(pt.uminho.taki.dao.FornecedorDAO.class),
            mock(pt.uminho.taki.dao.InventarioDAO.class),
            mock(pt.uminho.taki.dao.FuncionarioDAO.class),
            mock(pt.uminho.taki.dao.PromocaoDAO.class),
            mock(pt.uminho.taki.dao.DevolucaoDAO.class),
            mock(pt.uminho.taki.dao.StatisticsDAO.class),
            mock(pt.uminho.taki.ln.fatura.FaturaService.class),
            mock(pt.uminho.taki.ln.report.RelatorioService.class)
        );
        
        ctxMock = mock(Context.class);
    }

    @Test
    void listarLojas_quandoLojasExistem_respondeComLista() {
        // Arrange
        List<Loja> lojas = List.of(
            new Loja(1, "Loja A", "123", "a@taki.pt", "111", "Rua A", "Cid", "Dist"), 
            new Loja(2, "Loja B", "456", "b@taki.pt", "222", "Rua B", "Cid", "Dist")
        );
        when(takiLNGlobalMock.listarLojas()).thenReturn(lojas);

        // Act
        controller.listarLojas(ctxMock);

        // Assert
        verify(ctxMock).json(lojas);
    }

    @Test
    void buscarLoja_quandoLojaExiste_respondeComLoja() {
        // Arrange
        when(ctxMock.pathParam("idLoja")).thenReturn("1");
        Loja loja = new Loja(1, "Loja A", "123", "a@taki.pt", "111", "Rua A", "Cid", "Dist");
        when(takiLNGlobalMock.buscarLoja(1)).thenReturn(Optional.of(loja));

        // Act
        controller.buscarLoja(ctxMock);

        // Assert
        verify(ctxMock).json(loja);
    }

    @Test
    void buscarLoja_quandoLojaNaoExiste_lancaNotFoundResponse() {
        // Arrange
        when(ctxMock.pathParam("idLoja")).thenReturn("99");
        when(takiLNGlobalMock.buscarLoja(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundResponse.class, () -> {
            controller.buscarLoja(ctxMock);
        });
    }

    @Test
    void registarLoja_comCorpoValido_chamaRegistoEResponde201() {
        // Arrange
        Loja loja = new Loja(1, "Loja Nova", "123", "a@taki.pt", "111", "Rua A", "Cid", "Dist");
        when(ctxMock.bodyAsClass(Loja.class)).thenReturn(loja);
        when(ctxMock.status(201)).thenReturn(ctxMock);
        when(takiLNGlobalMock.registarLoja(loja)).thenReturn(loja);

        // Act
        controller.registarLoja(ctxMock);

        // Assert
        verify(takiLNGlobalMock).registarLoja(loja);
        verify(ctxMock).status(201);
        
        ArgumentCaptor<Loja> captor = ArgumentCaptor.forClass(Loja.class);
        verify(ctxMock).json(captor.capture());
        assertEquals("Loja Nova", captor.getValue().getNome());
    }

    @Test
    void removerLoja_quandoLojaExiste_chamaRemocaoERespondeSucesso() {
        // Arrange
        when(ctxMock.pathParam("idLoja")).thenReturn("1");

        // Act
        controller.removerLoja(ctxMock);

        // Assert
        verify(takiLNGlobalMock).removerLoja(1);
        ArgumentCaptor<MensagemResponseDto> captor = ArgumentCaptor.forClass(MensagemResponseDto.class);
        verify(ctxMock).json(captor.capture());
        assertEquals("loja_removida", captor.getValue().getCode());
    }

    @Test
    void rejeitarAlteracaoGlobal_sempre_responde501() {
        // Arrange
        when(ctxMock.status(501)).thenReturn(ctxMock);

        // Act
        controller.rejeitarAlteracaoGlobal(ctxMock);

        // Assert
        verify(ctxMock).status(501);
        ArgumentCaptor<pt.uminho.taki.api.shared.erros.ErroApiResponse> captor = ArgumentCaptor.forClass(pt.uminho.taki.api.shared.erros.ErroApiResponse.class);
        verify(ctxMock).json(captor.capture());
        assertEquals("erro_interno", captor.getValue().getCode());
    }

    @Test
    void deveUsarCookieSeguro_soEmModoProd() {
        assertTrue(GlobalApiController.deveUsarCookieSeguro("PROD"));
        assertTrue(GlobalApiController.deveUsarCookieSeguro("prod"));
        assertFalse(GlobalApiController.deveUsarCookieSeguro("local"));
        assertFalse(GlobalApiController.deveUsarCookieSeguro("central"));
        assertFalse(GlobalApiController.deveUsarCookieSeguro(null));
    }
}
