package pt.uminho.taki.ln.fornecimentos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.DisplayName;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;

import java.util.List;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EncomendaServiceTest {

    @Mock
    private EncomendaDAO encomendaDAO;

    @Mock
    private FornecedorDAO fornecedorDAO;

    @Mock
    private ProdutoFornecedorDAO produtoFornecedorDAO;

    @InjectMocks
    private EncomendaService encomendaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar guia com sucesso quando fornecedor está ativo e há linhas")
    void criarGuiaComSucesso() throws FornecedorInativoException, CamposObrigatoriosEmFaltaException {
        // Arrange
        String idEncomenda = "ENC-001";
        String idFornecedor = "F1";
        String idLoja = "L1";
        List<LinhaEncomenda> linhas = List.of(
            new LinhaEncomenda(idEncomenda, "P1", 10.0, 5.0),
            new LinhaEncomenda(idEncomenda, "P2", 5.0, 10.0)
        );
        
        Fornecedor fornecedorAtivo = new Fornecedor(idFornecedor, "Fornecedor X", "501234567", "+351 912345678", "f@f.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorAtivo));
        when(produtoFornecedorDAO.findByIdProdutoAndIdFornecedor(anyString(), eq(idFornecedor)))
                .thenReturn(Optional.of(mock(ProdutoFornecedor.class)));

        // Act
        encomendaService.criarGuia(idEncomenda, idFornecedor, idLoja, linhas);

        // Assert
        ArgumentCaptor<Encomenda> captor = ArgumentCaptor.forClass(Encomenda.class);
        verify(encomendaDAO).save(eq(idEncomenda), captor.capture());
        
        Encomenda salva = captor.getValue();
        assertEquals(idEncomenda, salva.getIdEncomenda());
        assertEquals(idFornecedor, salva.getIdFornecedor());
        assertEquals(idLoja, salva.getIdLoja());
        assertEquals(2, salva.getLinhas().size());
        assertEquals("Rascunho", salva.getEstadoAtual().getDesignacao());
    }

    @Test
    @DisplayName("Deve falhar criação de guia se o fornecedor estiver inativo")
    void criarGuiaComFornecedorInativoLancaExcecao() {
        // Arrange
        String idFornecedor = "F_INATIVO";
        Fornecedor fornecedorInativo = new Fornecedor(idFornecedor, "Inativo SA", "509999999", "+351 912345678", "i@i.pt", "Inativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorInativo));

        // Act & Assert
        assertThrows(FornecedorInativoException.class, 
            () -> encomendaService.criarGuia("ENC-002", idFornecedor, "L1", List.of(new LinhaEncomenda("E2", "P1", 1.0, 1.0))));
        verify(encomendaDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve falhar criação de guia se o fornecedor não existir")
    void criarGuiaComFornecedorInexistenteLancaExcecao() {
        // Arrange
        when(fornecedorDAO.findById("F404")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class,
                () -> encomendaService.criarGuia("ENC-404", "F404", "1", List.of(new LinhaEncomenda("ENC-404", "P1", 1.0, 1.0))));
        verify(encomendaDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve falhar criação de guia se a lista de linhas estiver vazia")
    void criarGuiaSemLinhasLancaExcecao() {
        // Arrange
        String idFornecedor = "F1";
        Fornecedor fornecedorAtivo = new Fornecedor(idFornecedor, "Fornecedor X", "501234567", "+351 912345678", "f@f.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorAtivo));

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class, 
            () -> encomendaService.criarGuia("ENC-002", idFornecedor, "L1", Collections.emptyList()));
        verify(encomendaDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve falhar criação de guia se a lista de linhas for nula")
    void criarGuiaComLinhasNulasLancaExcecao() {
        // Arrange
        String idFornecedor = "F1";
        Fornecedor fornecedorAtivo = new Fornecedor(idFornecedor, "Fornecedor X", "501234567", "+351 912345678", "f@f.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorAtivo));

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class, 
            () -> encomendaService.criarGuia("ENC-002", idFornecedor, "L1", null));
        verify(encomendaDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve processar transição de estado com sucesso")
    void processarTransicaoEstadoComSucesso() {
        // Arrange
        String idEncomenda = "ENC-003";
        Encomenda encomenda = new Encomenda(idEncomenda, "F1", "L1");
        when(encomendaDAO.findById(idEncomenda)).thenReturn(Optional.of(encomenda));

        // Act - Avança de Rascunho para Pendente
        encomendaService.processarTransicaoEstado(idEncomenda);

        // Assert
        assertEquals("Pendente", encomenda.getEstadoAtual().getDesignacao());
        verify(encomendaDAO).save(eq(idEncomenda), eq(encomenda));
    }

    @Test
    @DisplayName("Deve falhar transição de estado se encomenda não existir")
    void processarTransicaoEstadoEncomendaInexistenteLancaExcecao() {
        // Arrange
        String idEncomenda = "ENC-INEXISTENTE";
        when(encomendaDAO.findById(idEncomenda)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> encomendaService.processarTransicaoEstado(idEncomenda));
    }

    @Test
    @DisplayName("Deve falhar criação de guia quando linha contém produto não associado ao fornecedor")
    void criarGuiaProdutoNaoAssociadoAoFornecedorLancaExcecao() {
        // Arrange
        String idFornecedor = "F1";
        Fornecedor fornecedorAtivo = new Fornecedor(idFornecedor, "Fornecedor X", "501234567", "+351 912345678", "f@f.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorAtivo));
        when(produtoFornecedorDAO.findByIdProdutoAndIdFornecedor("P999", idFornecedor)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class,
                () -> encomendaService.criarGuia("ENC-005", idFornecedor, "L1",
                        List.of(new LinhaEncomenda("ENC-005", "P999", 1.0, 5.0))));
        verify(encomendaDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve calcular o total da guia corretamente")
    void calcularTotalGuiaComLinhas() {
        // Arrange
        String idEncomenda = "ENC-004";
        Encomenda encomenda = new Encomenda(idEncomenda, "F1", "L1");
        encomenda.adicionarLinha(new LinhaEncomenda(idEncomenda, "P1", 10.0, 5.0));  // 50.0
        encomenda.adicionarLinha(new LinhaEncomenda(idEncomenda, "P2", 3.0, 20.0)); // 60.0
        when(encomendaDAO.findById(idEncomenda)).thenReturn(Optional.of(encomenda));

        // Act
        double total = encomendaService.calcularTotalGuia(idEncomenda);

        // Assert
        assertEquals(110.0, total, 0.001);
    }

    @Test
    @DisplayName("Deve devolver total zero se a encomenda não existir")
    void calcularTotalGuiaEncomendaInexistenteRetornaZero() {
        // Arrange
        when(encomendaDAO.findById("ENC-INEXISTENTE")).thenReturn(Optional.empty());

        // Act
        double total = encomendaService.calcularTotalGuia("ENC-INEXISTENTE");

        // Assert
        assertEquals(0.0, total);
    }
}
