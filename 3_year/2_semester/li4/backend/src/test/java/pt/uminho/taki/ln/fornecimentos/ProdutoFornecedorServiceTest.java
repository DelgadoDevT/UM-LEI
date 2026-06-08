package pt.uminho.taki.ln.fornecimentos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProdutoFornecedorServiceTest {

    @Mock
    private ProdutoFornecedorDAO pfDAO;

    @Mock
    private FornecedorDAO fornecedorDAO;

    @InjectMocks
    private ProdutoFornecedorService produtoFornecedorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void associarProdutoAFornecedorComSucesso() throws FornecedorInativoException {
        // Arrange
        String idProduto = "P1";
        String idFornecedor = "F1";
        double precoCusto = 5.0;
        
        Fornecedor fornecedor = new Fornecedor(idFornecedor, "Fornecedor X", "501234567", "+351 912345678", "f@f.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedor));
        when(pfDAO.findByIdProduto(idProduto)).thenReturn(new ArrayList<>());
        when(pfDAO.findByIdProdutoAndIdFornecedor(idProduto, idFornecedor)).thenReturn(Optional.empty());

        // Act
        produtoFornecedorService.associarProdutoAFornecedor(idProduto, idFornecedor, precoCusto);

        // Assert
        ArgumentCaptor<ProdutoFornecedor> captor = ArgumentCaptor.forClass(ProdutoFornecedor.class);
        verify(pfDAO).save(isNull(), captor.capture());
        
        ProdutoFornecedor salvo = captor.getValue();
        assertEquals(idProduto, salvo.getIdProduto());
        assertEquals(idFornecedor, salvo.getIdFornecedor());
        assertEquals(precoCusto, salvo.getPrecoCusto());
        assertTrue(salvo.isPreferencial(), "Primeiro fornecedor deve ser preferencial");
    }

    @Test
    void associarProdutoAFornecedorInativoLancaExcecao() {
        // Arrange
        String idFornecedor = "F_INATIVO";
        Fornecedor fornecedorInativo = new Fornecedor(idFornecedor, "Inativo SA", "509999999", "+351 912345678", "i@i.pt", "Inativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(fornecedorInativo));

        // Act & Assert
        assertThrows(FornecedorInativoException.class, 
            () -> produtoFornecedorService.associarProdutoAFornecedor("P1", idFornecedor, 10.0));
        verify(pfDAO, never()).save(any(), any());
    }

    @Test
    @DisplayName("Deve falhar se o fornecedor não existir")
    void falharSeFornecedorInexistente() throws FornecedorInativoException {
        // Arrange
        String idFornecedor = "F_FANTASMA";
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> produtoFornecedorService.associarProdutoAFornecedor("P1", idFornecedor, 10.0));
        verify(pfDAO, never()).save(any(), any());
    }

    @Test
    @DisplayName("Deve associar novo fornecedor com preço mais baixo e torná-lo preferencial")
    void associarFornecedorComMenorPrecoTornaSePreferencial() throws FornecedorInativoException {
        // Arrange
        String idProduto = "P1";
        String idFornecedorNovo = "F_NOVO";
        double precoMaisBaixo = 3.0;
        
        Fornecedor fornecedorNovo = new Fornecedor(idFornecedorNovo, "Barato SA", "501111111", "+351 912345678", "b@b.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedorNovo)).thenReturn(Optional.of(fornecedorNovo));
        when(pfDAO.findByIdProdutoAndIdFornecedor(idProduto, idFornecedorNovo)).thenReturn(Optional.empty());
        
        // Já existe um fornecedor com preço mais alto
        ProdutoFornecedor existente = new ProdutoFornecedor(idProduto, "F_ANTIGO", 10.0);
        existente.setPreferencial(true);
        when(pfDAO.findByIdProduto(idProduto)).thenReturn(List.of(existente));

        // Act
        produtoFornecedorService.associarProdutoAFornecedor(idProduto, idFornecedorNovo, precoMaisBaixo);

        // Assert
        // O novo ganha preferencial
        ArgumentCaptor<ProdutoFornecedor> captor = ArgumentCaptor.forClass(ProdutoFornecedor.class);
        verify(pfDAO, times(2)).save(isNull(), captor.capture());
        
        List<ProdutoFornecedor> salvos = captor.getAllValues();
        
        // O antigo perde preferencial (primeiro save)
        assertFalse(existente.isPreferencial());
        assertEquals("F_ANTIGO", salvos.get(0).getIdFornecedor());
        
        // O novo ganha preferencial (segundo save)
        ProdutoFornecedor novoSalvo = salvos.get(1);
        assertTrue(novoSalvo.isPreferencial());
        assertEquals(idFornecedorNovo, novoSalvo.getIdFornecedor());
    }
}
