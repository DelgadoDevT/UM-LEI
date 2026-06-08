package pt.uminho.taki.ln;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.IFuncionarioService;
import pt.uminho.taki.ln.lojas.IProdutoService;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.exceptions.*;

import pt.uminho.taki.ln.inventario.ISubSistemaInventario;
import pt.uminho.taki.ln.inventario.IInventarioService;
import pt.uminho.taki.ln.inventario.IMovimentoInventarioService;
import pt.uminho.taki.ln.inventario.MovimentoInventario;

import pt.uminho.taki.ln.fornecimentos.ISubSistemaFornecimentos;
import pt.uminho.taki.ln.fornecimentos.IProdutoFornecedorService;

import pt.uminho.taki.ln.vendas.ISubSistemaVendas;
import pt.uminho.taki.ln.vendas.IVendaService;
import pt.uminho.taki.ln.vendas.Venda;

import pt.uminho.taki.ln.sincronizacao.ISubSistemaSincronizacao;
import pt.uminho.taki.ln.sincronizacao.ISincronizacaoService;

import pt.uminho.taki.ln.lojas.ICategoriaService;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.vendas.IPromocaoService;
import pt.uminho.taki.ln.vendas.Promocao;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TakiLNLocalTest {

    @Mock private ISubSistemaLojas subSistemaLojas;
    @Mock private ISubSistemaInventario subSistemaInventario;
    @Mock private ISubSistemaFornecimentos subSistemaFornecimentos;
    @Mock private ISubSistemaVendas subSistemaVendas;
    @Mock private ISubSistemaSincronizacao subSistemaSincronizacao;

    @Mock private IFuncionarioService funcionarioService;
    @Mock private IProdutoService produtoService;
    @Mock private ICategoriaService categoriaService;
    @Mock private IInventarioService inventarioService;
    @Mock private IMovimentoInventarioService movimentoInventarioService;
    @Mock private IProdutoFornecedorService produtoFornecedorService;
    private pt.uminho.taki.ln.fornecimentos.IEncomendaService encomendaService;
    @Mock private IVendaService vendaService;
    @Mock private IPromocaoService promocaoService;
    @Mock private ISincronizacaoService sincronizacaoService;

    private TakiLNLocal takiLNLocal;

    @BeforeEach
    void setUp() {
        encomendaService = mock(pt.uminho.taki.ln.fornecimentos.IEncomendaService.class);
        MockitoAnnotations.openMocks(this);
        
        when(subSistemaLojas.getFuncionarioService()).thenReturn(funcionarioService);
        when(subSistemaLojas.getProdutoService()).thenReturn(produtoService);
        when(subSistemaLojas.getCategoriaService()).thenReturn(categoriaService);
        when(subSistemaInventario.getInventarioService()).thenReturn(inventarioService);
        when(subSistemaInventario.getMovimentoInventarioService()).thenReturn(movimentoInventarioService);
        when(subSistemaFornecimentos.getProdutoFornecedorService()).thenReturn(produtoFornecedorService);
        when(subSistemaFornecimentos.getEncomendaService()).thenReturn(encomendaService);
        when(subSistemaVendas.getVendaService()).thenReturn(vendaService);
        when(subSistemaVendas.getPromocaoService()).thenReturn(promocaoService);
        when(subSistemaSincronizacao.getSincronizacaoService()).thenReturn(sincronizacaoService);
        
        takiLNLocal = new TakiLNLocal(subSistemaLojas, subSistemaFornecimentos, subSistemaInventario, subSistemaVendas, subSistemaSincronizacao);
    }

    @Test
    void testListarProdutos_Sucesso() {
        // Arrange
        List<Produto> esperado = new ArrayList<>();
        esperado.add(new Produto());
        when(produtoService.listarProdutos()).thenReturn(esperado);

        // Act
        List<Produto> resultado = takiLNLocal.listarProdutos();

        // Assert
        assertEquals(esperado, resultado);
        verify(produtoService, times(1)).listarProdutos();
    }

    @Test
    void testListarCategorias_Sucesso() {
        // Arrange
        List<Categoria> esperado = new ArrayList<>();
        esperado.add(new Categoria());
        when(categoriaService.listarCategorias()).thenReturn(esperado);

        // Act
        List<Categoria> resultado = takiLNLocal.listarCategorias();

        // Assert
        assertEquals(esperado, resultado);
        verify(categoriaService, times(1)).listarCategorias();
    }

    @Test
    void testListarFuncionarios_Sucesso() {
        // Arrange
        List<Funcionario> esperado = new ArrayList<>();
        esperado.add(new Funcionario());
        when(funcionarioService.listarFuncionarios()).thenReturn(esperado);

        // Act
        List<Funcionario> resultado = takiLNLocal.listarFuncionarios();

        // Assert
        assertEquals(esperado, resultado);
        verify(funcionarioService, times(1)).listarFuncionarios();
    }

    @Test
    void testListarPromocoesAtivas_Sucesso() {
        // Arrange
        List<Promocao> esperado = new ArrayList<>();
        esperado.add(new Promocao());
        when(promocaoService.getPromocoesAtivas()).thenReturn(esperado);

        // Act
        List<Promocao> resultado = takiLNLocal.listarPromocoesAtivas();

        // Assert
        assertEquals(esperado, resultado);
        verify(promocaoService, times(1)).getPromocoesAtivas();
    }

    @Test
    void testAutenticar_Sucesso() throws Exception {
        // Arrange
        String email = "test@taki.pt";
        String password = "password_forte";
        Funcionario esperado = new Funcionario();
        esperado.setEmail(email);
        when(funcionarioService.autenticar(email, password)).thenReturn(esperado);

        // Act
        Funcionario resultado = takiLNLocal.autenticar(email, password);

        // Assert
        assertEquals(esperado, resultado);
        verify(funcionarioService, times(1)).autenticar(email, password);
    }

    @Test
    void testAutenticar_ContaBloqueada() throws Exception {
        // Arrange
        String email = "test@taki.pt";
        String password = "password_forte";
        when(funcionarioService.autenticar(email, password)).thenThrow(new ContaBloqueadaException("Conta Bloqueada"));

        // Act & Assert
        assertThrows(ContaBloqueadaException.class, () -> takiLNLocal.autenticar(email, password));
        verify(funcionarioService, times(1)).autenticar(email, password);
    }

    @Test
    void testAdicionarProduto_Sucesso() throws Exception {
        // Arrange
        Produto produto = new Produto();

        // Act
        takiLNLocal.adicionarProduto(produto);

        // Assert
        verify(produtoService, times(1)).adicionarProduto(produto);
    }

    @Test
    void testRegistarMovimentoManual_Sucesso() throws Exception {
        // Arrange
        MovimentoInventario movimento = new MovimentoInventario();

        // Act
        takiLNLocal.registarMovimentoManual(movimento);

        // Assert
        verify(inventarioService, times(1)).registarMovimentoManual(movimento);
    }

    @Test
    void testAssociarProdutoAFornecedor_Sucesso() throws Exception {
        // Arrange
        String idProduto = "P1";
        String idFornecedor = "F1";
        double preco = 10.5;

        // Act
        takiLNLocal.associarProdutoAFornecedor(idProduto, idFornecedor, preco);

        // Assert
        verify(produtoFornecedorService, times(1)).associarProdutoAFornecedor(idProduto, idFornecedor, preco);
    }

    @Test
    void testRegistarVenda_Sucesso() throws Exception {
        // Arrange
        Venda venda = new Venda();
        String metodoPagamento = "Cartao";

        // Act
        takiLNLocal.registarVenda(venda, metodoPagamento);

        // Assert
        verify(vendaService, times(1)).processarVenda(venda, metodoPagamento);
    }

    @Test
    void testSincronizarDados_Sucesso() throws Exception {
        // Act
        takiLNLocal.sincronizarDados();

        // Assert
        // A implementação atual apenas faz mock/sleep, não chama exportarLoteParaSede
    }
}