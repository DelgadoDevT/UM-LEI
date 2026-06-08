package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.dao.CategoriaDAO;
import pt.uminho.taki.ln.lojas.exceptions.CategoriaInvalidaException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CategoriaServiceTest {

    @Mock
    private CategoriaDAO categoriaDAO;

    @InjectMocks
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validarHierarquiaComSucesso() {
        // Arrange
        String idCategoriaPai = "C_123";
        Categoria categoriaPai = new Categoria(idCategoriaPai, "Categoria Pai Teste", "");
        when(categoriaDAO.findById(idCategoriaPai)).thenReturn(Optional.of(categoriaPai));

        // Act & Assert
        assertDoesNotThrow(() -> categoriaService.validarHierarquia(idCategoriaPai),
                "A valida\u00e7\u00e3o n\u00e3o deve lan\u00e7ar nenhuma exce\u00e7\u00e3o se a categoria pai existir.");
        verify(categoriaDAO, times(1)).findById(idCategoriaPai);
    }

    @Test
    @DisplayName("Deve validar hierarquia com sucesso quando ID pai é nulo ou vazio")
    void validarHierarquiaSemPaiComSucesso() {
        assertDoesNotThrow(() -> categoriaService.validarHierarquia(null));
        assertDoesNotThrow(() -> categoriaService.validarHierarquia("  "));
        verify(categoriaDAO, never()).findById(anyString());
    }

    @Test
    @DisplayName("Deve falhar ao validar hierarquia se a categoria pai não existir")
    void rejeitarCategoriaPaiInexistente() {
        // Arrange
        String idCategoriaPaiInvalido = "C_999";
        when(categoriaDAO.findById(idCategoriaPaiInvalido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoriaInvalidaException.class, 
                () -> categoriaService.validarHierarquia(idCategoriaPaiInvalido),
                "Deve lan\u00e7ar CategoriaInvalidaException se a categoria pai n\u00e3o existir no DAO.");
        verify(categoriaDAO, times(1)).findById(idCategoriaPaiInvalido);
    }

    @Test
    void listarCategoriasRetornaListaVazia() {
        // Arrange
        when(categoriaDAO.findAll()).thenReturn(List.of());

        // Act
        List<Categoria> resultado = categoriaService.listarCategorias();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(categoriaDAO, times(1)).findAll();
    }

    @Test
    void listarCategoriasRetornaTodasCategorias() {
        // Arrange
        Categoria cat1 = new Categoria("C_001", "Eletrónica", "");
        Categoria cat2 = new Categoria("C_002", "Alimentação", "");
        List<Categoria> categoriasEsperadas = List.of(cat1, cat2);
        when(categoriaDAO.findAll()).thenReturn(categoriasEsperadas);

        // Act
        List<Categoria> resultado = categoriaService.listarCategorias();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals(categoriasEsperadas, resultado);
        verify(categoriaDAO, times(1)).findAll();
    }
}
