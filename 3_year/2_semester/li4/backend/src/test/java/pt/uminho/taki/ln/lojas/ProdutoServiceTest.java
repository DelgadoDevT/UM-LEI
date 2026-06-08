package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInativoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProdutoServiceTest {

    @Mock
    private ProdutoDAO produtoDAO;

    @InjectMocks
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void adicionarProdutoComCalculoDeIvaCorreto() throws ProdutoExistenteException {
        // Arrange
        Produto produto = new Produto("P_1", "123456789", "Teclado Mecanico", "Descrição teste", 
                                       10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        when(produtoDAO.findByCodigoBarras("123456789")).thenReturn(Optional.empty());

        // Act
        produtoService.adicionarProduto(produto);

        // Assert
        ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoDAO, times(1)).save(eq("P_1"), produtoCaptor.capture());
        
        Produto guardado = produtoCaptor.getValue();
        assertEquals(12.30, guardado.calcularPrecoVenda(), 0.001, "O cálculo do preco de venda (com IVA) está incorreto.");
        assertTrue(guardado.isAtivo(), "O produto deve ser considerado ativo aquando da sua criação.");
    }

    @Test
    void rejeitarCriacaoDeProdutoComEanDuplicado() {
        // Arrange
        Produto produtoNovo = new Produto("P_2", "111222333", "Rato Wireless", "Descrição teste",
                                          20.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        Produto existente = new Produto("P_0", "111222333", "Outro Rato", "Descrição teste",
                                        15.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        when(produtoDAO.findByCodigoBarras("111222333")).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThrows(ProdutoExistenteException.class, 
                () -> produtoService.adicionarProduto(produtoNovo),
                "Deve lançar ProdutoExistenteException se o código EAN já estiver registado.");
        verify(produtoDAO, never()).save(anyString(), any(Produto.class));
    }

    @Test
    void inativarProdutoComSucessoSoftDelete() throws ProdutoInativoException {
        // Arrange
        String idProduto = "P_3";
        Produto produtoAtivo = new Produto(idProduto, "987654321", "Monitor LCD", "Descrição teste",
                                           150.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        when(produtoDAO.findById(idProduto)).thenReturn(Optional.of(produtoAtivo));

        // Act
        produtoService.inativarProduto(idProduto);

        // Assert
        ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoDAO, times(1)).save(eq(idProduto), produtoCaptor.capture());

        Produto alterado = produtoCaptor.getValue();
        assertFalse(alterado.isAtivo(), "A inativação lógica deve alterar a flag 'ativo' para falso.");
    }

    @Test
    void editarProdutoComSucesso() throws ProdutoInativoException, ProdutoExistenteException, ProdutoInexistenteException {
        // Arrange
        Produto original = new Produto("P_1", "123456789", "Teclado", "Descrição teste",
                                       10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        Produto atualizado = new Produto("P_1", "123456789", "Teclado RGB", "Descrição teste",
                                         15.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");

        when(produtoDAO.findById("P_1")).thenReturn(Optional.of(original));

        // Act
        produtoService.editarProduto(atualizado);

        // Assert
        verify(produtoDAO, times(1)).save(eq("P_1"), eq(atualizado));
    }

    @Test
    void editarProdutoAlterandoEanParaEanLivre() throws ProdutoInativoException, ProdutoExistenteException, ProdutoInexistenteException {
        // Arrange
        Produto original = new Produto("P_1", "123456789", "Teclado", "Descrição teste",
                                       10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        Produto atualizado = new Produto("P_1", "999888777", "Teclado", "Descrição teste",
                                         10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");

        when(produtoDAO.findById("P_1")).thenReturn(Optional.of(original));
        when(produtoDAO.findByCodigoBarras("999888777")).thenReturn(Optional.empty());

        // Act
        produtoService.editarProduto(atualizado);

        // Assert
        verify(produtoDAO, times(1)).save(eq("P_1"), eq(atualizado));
    }

    @Test
    void editarProdutoAlterandoEanParaEanExistenteLancaExcecao() {
        // Arrange
        Produto original = new Produto("P_1", "123456789", "Teclado", "Descrição teste",
                                       10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        Produto atualizado = new Produto("P_1", "111222333", "Teclado", "Descrição teste",
                                         10.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");
        Produto existente = new Produto("P_2", "111222333", "Rato", "Descrição teste",
                                        5.0, 0.0, TaxaIva.NORMAL_23, "unidade", "Ativo");

        when(produtoDAO.findById("P_1")).thenReturn(Optional.of(original));
        when(produtoDAO.findByCodigoBarras("111222333")).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThrows(ProdutoExistenteException.class,
                () -> produtoService.editarProduto(atualizado),
                "Deve lançar ProdutoExistenteException se o novo código de barras já estiver em uso.");
        verify(produtoDAO, never()).save(anyString(), any(Produto.class));
    }

    @Test
    @DisplayName("Deve falhar a edição se o produto não existir")
    void editarProdutoInexistenteLancaExcecao() {
        // Arrange
        Produto p = new Produto("P_NAO_EXISTE", "123", "Nome", "Desc", 10.0, 0.0, TaxaIva.NORMAL_23, "un", "Ativo");
        when(produtoDAO.findById("P_NAO_EXISTE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProdutoInexistenteException.class, () -> produtoService.editarProduto(p));
    }

    @Test
    @DisplayName("Deve falhar inativação se o produto já estiver inativo")
    void falharInativacaoProdutoJaInativo() {
        // Arrange
        String idProduto = "P_INATIVO";
        Produto produtoInativo = new Produto(idProduto, "987654321", "Monitor", "Desc", 150.0, 0.0, TaxaIva.NORMAL_23, "un", "Inativo");
        when(produtoDAO.findById(idProduto)).thenReturn(Optional.of(produtoInativo));

        // Act & Assert
        assertThrows(ProdutoInativoException.class, () -> produtoService.inativarProduto(idProduto));
    }

    @Test
    @DisplayName("Não deve fazer nada ao inativar produto inexistente")
    void naoFazNadaAoInativarProdutoInexistente() throws ProdutoInativoException {
        // Arrange
        when(produtoDAO.findById("X")).thenReturn(Optional.empty());

        // Act
        produtoService.inativarProduto("X");

        // Assert
        verify(produtoDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve pesquisar produto por código de barras com sucesso")
    void pesquisarPorCodigoBarrasSucesso() {
        // Arrange
        Produto p = new Produto("P1", "123", "Nome", "Desc", 10.0, 0.0, TaxaIva.NORMAL_23, "un", "Ativo");
        when(produtoDAO.findByCodigoBarras("123")).thenReturn(Optional.of(p));

        // Act
        Produto result = produtoService.pesquisarPorCodigoBarras("123");

        // Assert
        assertNotNull(result);
        assertEquals("P1", result.getIdProduto());
    }

    @Test
    @DisplayName("Deve devolver nulo se o código de barras não existir")
    void pesquisarPorCodigoBarrasInexistenteRetornaNull() {
        // Arrange
        when(produtoDAO.findByCodigoBarras("X")).thenReturn(Optional.empty());

        // Act
        Produto result = produtoService.pesquisarPorCodigoBarras("X");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve associar produto a categoria")
    void associarProdutoCategoria() {
        when(produtoDAO.exists("P1")).thenReturn(true);
        produtoService.associarCategoria("P1", "C1");
        verify(produtoDAO).addCategoria("P1", "C1");
    }

    @Test
    @DisplayName("Deve listar produtos por categoria")
    void listarProdutosPorCategoria() {
        Produto p = new Produto("P1", "123", "Nome", "Desc", 10.0, 0.0, TaxaIva.NORMAL_23, "un", "Ativo");
        when(produtoDAO.findAll()).thenReturn(java.util.List.of(p));
        when(produtoDAO.getCategorias("P1")).thenReturn(Set.of("C1"));
        assertEquals(1, produtoService.listarProdutosPorCategoria("C1").size());
    }
}
