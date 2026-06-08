package pt.uminho.taki.ln.fatura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

import pt.uminho.taki.dao.FaturaDAO;
import pt.uminho.taki.ln.vendas.Venda;
import java.util.ArrayList;
import java.util.Optional;
import static org.mockito.Mockito.*;

class FaturaServiceTest {
    private FaturaService faturaService;
    private FaturaDAO faturaDAO;

    @BeforeEach
    void setUp() {
        faturaDAO = mock(FaturaDAO.class);
        faturaService = new FaturaService(faturaDAO);
    }

    @Test
    void testEmitirFatura() throws Exception {
        // Arrange
        String idVenda = "venda-1";
        String nifCliente = "999999999";
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());

        // Act
        Fatura f = faturaService.emitirFatura(idVenda, nifCliente);

        // Assert
        assertNotNull(f);
        assertEquals("FT 2024/1", f.getNumFatura());
        assertNotNull(f.getHash());
    }

    @Test
    void testOnVendaConcluidaEmiteFatura() {
        // Arrange
        Venda vendaMock = new Venda();
        vendaMock.setIdVenda("venda-observer-1");
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        
        // Act
        faturaService.onVendaConcluida(vendaMock);
        
        // Assert
        assertNotNull(vendaMock.getFatura());
        Fatura emitida = vendaMock.getFatura();
        assertEquals("venda-observer-1", emitida.getIdVenda());
        assertNull(emitida.getNifCliente());
    }

    @Test
    void testEncadeamentoHashes() throws Exception {
        // Arrange
        when(faturaDAO.count()).thenReturn(0, 1);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        
        // Act
        Fatura f1 = faturaService.emitirFatura("v1", "999999999");
        
        when(faturaDAO.findAll()).thenReturn(List.of(f1));
        Fatura f2 = faturaService.emitirFatura("v2", "999999999");

        // Assert
        assertNotEquals(f1.getHash(), f2.getHash());
        assertNotNull(f2.getHash());
        assertEquals("FT 2024/2", f2.getNumFatura());
    }

    @Test
    void testRegraSequencial() throws Exception {
        // Arrange & Act
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        Fatura f1 = faturaService.emitirFatura("v1", "999999999");
        
        when(faturaDAO.count()).thenReturn(1);
        when(faturaDAO.findAll()).thenReturn(List.of(f1));
        Fatura f2 = faturaService.emitirFatura("v2", "999999999");
        
        // Assert
        assertEquals("FT 2024/1", f1.getNumFatura());
        assertEquals("FT 2024/2", f2.getNumFatura());
    }

    @Test
    void testValidarIntegridadeDaCadeia() throws Exception {
        // Arrange
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        Fatura f1 = faturaService.emitirFatura("v1", "999999999");
        
        when(faturaDAO.count()).thenReturn(1);
        when(faturaDAO.findAll()).thenReturn(List.of(f1));
        Fatura f2 = faturaService.emitirFatura("v2", "999999999");
        
        when(faturaDAO.findAll()).thenReturn(List.of(f1, f2));

        // Act & Assert
        assertTrue(faturaService.validarIntegridade());
    }

    @Test
    void testEmitirSegundaVia() throws Exception {
        // Arrange
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        Fatura original = faturaService.emitirFatura("v1", "999999999");
        
        when(faturaDAO.findByNumeroFatura(original.getNumFatura())).thenReturn(Optional.of(original));

        // Act
        Fatura segundaVia = faturaService.emitirSegundaVia(original.getNumFatura());

        // Assert
        assertNotNull(segundaVia);
        assertEquals(original.getNumFatura(), segundaVia.getNumFatura());
        assertEquals(original.getHash(), segundaVia.getHash());
    }

    @Test
    void testExportarFaturaJsonECsv() throws Exception {
        // Arrange
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        Fatura f = faturaService.emitirFatura("v1", "999999999");
        
        when(faturaDAO.findByNumeroFatura(f.getNumFatura())).thenReturn(Optional.of(f));

        // Act
        String json = faturaService.exportarFaturaJson(f.getNumFatura());
        String csv = faturaService.exportarFaturaCsv(f.getNumFatura());

        // Assert
        assertTrue(json.contains("\"numFatura\":\"" + f.getNumFatura() + "\""));
        assertTrue(csv.startsWith("id_fatura,num_fatura,data_emissao,nif_cliente,id_venda,hash,hash_control"));
        assertTrue(csv.contains("\"" + f.getNumFatura() + "\""));
    }

    @Test
    void testExportarSaftPtFiltraPeriodo() throws Exception {
        // Arrange
        when(faturaDAO.count()).thenReturn(0);
        when(faturaDAO.findAll()).thenReturn(new ArrayList<>());
        
        Fatura antiga = faturaService.emitirFatura("00000000-0000-0000-0000-000000000001", "999999999");
        antiga.setDataEmissao(LocalDateTime.now().minusDays(40));
        
        when(faturaDAO.count()).thenReturn(1);
        when(faturaDAO.findAll()).thenReturn(List.of(antiga));
        
        Fatura atual = faturaService.emitirFatura("00000000-0000-0000-0000-000000000002", "999999999");
        when(faturaDAO.findAll()).thenReturn(List.of(antiga, atual));

        // Act
        String saft = faturaService.exportarSaftPt(LocalDate.now().minusDays(30), LocalDate.now());

        // Assert
        assertTrue(saft.contains("<AuditFile xmlns="));
        assertTrue(saft.contains("<InvoiceNo>" + atual.getNumFatura() + "</InvoiceNo>"));
        assertFalse(saft.contains("<InvoiceNo>" + antiga.getNumFatura() + "</InvoiceNo>"));
    }
}
