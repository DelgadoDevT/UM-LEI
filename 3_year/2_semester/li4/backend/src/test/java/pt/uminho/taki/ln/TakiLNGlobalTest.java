package pt.uminho.taki.ln;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.ln.estatisticas.DatasInvalidasException;
import pt.uminho.taki.ln.estatisticas.ISubSistemaEstatisticas;
import pt.uminho.taki.ln.fornecimentos.ISubSistemaFornecimentos;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.Loja;
import pt.uminho.taki.ln.view.ISubSistemaView;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.estatisticas.DashboardKPIsDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioInventarioDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioVendasDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TakiLNGlobalTest {

    @Mock
    private ISubSistemaLojas subSistemaLojas;

    @Mock
    private ISubSistemaFornecimentos subSistemaFornecimentos;

    @Mock
    private ISubSistemaEstatisticas subSistemaEstatisticas;

    @Mock
    private ISubSistemaView subSistemaView;

    @InjectMocks
    private TakiLNGlobal takiLNGlobal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdicionarCategoria() {
        Categoria categoria = new Categoria();
        takiLNGlobal.adicionarCategoria(categoria);
        verify(subSistemaLojas).adicionarCategoria(categoria);
    }

    @Test
    void testListarCategorias() {
        List<Categoria> lista = Collections.emptyList();
        when(subSistemaLojas.listarCategorias()).thenReturn(lista);
        List<Categoria> resultado = takiLNGlobal.listarCategorias();
        assertEquals(lista, resultado);
        verify(subSistemaLojas).listarCategorias();
    }

    @Test
    void testAdicionarProduto() {
        Produto produto = new Produto();
        takiLNGlobal.adicionarProduto(produto);
        verify(subSistemaLojas).adicionarProduto(produto);
    }

    @Test
    void testInativarProduto() {
        String idProduto = "123";
        takiLNGlobal.inativarProduto(idProduto);
        verify(subSistemaLojas).inativarProduto(idProduto);
    }

    @Test
    void testPesquisarPorCodigoBarras() {
        String codigo = "CODE123";
        Produto p = new Produto();
        when(subSistemaLojas.pesquisarPorCodigoBarras(codigo)).thenReturn(p);
        Produto resultado = takiLNGlobal.pesquisarPorCodigoBarras(codigo);
        assertEquals(p, resultado);
        verify(subSistemaLojas).pesquisarPorCodigoBarras(codigo);
    }

    @Test
    void testRegistarFuncionario() {
        Funcionario f = new Funcionario();
        takiLNGlobal.registarFuncionario(f);
        verify(subSistemaLojas).registarFuncionario(f);
    }

    @Test
    void testBloquearConta() {
        String idFuncionario = "F123";
        takiLNGlobal.bloquearConta(idFuncionario);
        verify(subSistemaLojas).bloquearConta(idFuncionario);
    }

    @Test
    void testAdicionarFornecedor() {
        Fornecedor f = new Fornecedor();
        takiLNGlobal.adicionarFornecedor(f);
        verify(subSistemaFornecimentos).adicionarFornecedor(f);
    }

    @Test
    void testInativarFornecedor() {
        String idF = "F123";
        takiLNGlobal.inativarFornecedor(idF);
        verify(subSistemaFornecimentos).inativarFornecedor(idF);
    }

    @Test
    void testCalcularVolumeVendas() throws DatasInvalidasException {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        when(subSistemaEstatisticas.calcularVolumeVendas(inicio, fim)).thenReturn(100.0);
        double res = takiLNGlobal.calcularVolumeVendas(inicio, fim);
        assertEquals(100.0, res);
        verify(subSistemaEstatisticas).calcularVolumeVendas(inicio, fim);
    }

    @Test
    void testGerarRelatorioVendas() throws DatasInvalidasException {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        RelatorioVendasDTO dto = new RelatorioVendasDTO(100.0, 10, 10.0);
        when(subSistemaEstatisticas.gerarRelatorioVendas(inicio, fim, 1, "Cat")).thenReturn(dto);
        RelatorioVendasDTO res = takiLNGlobal.gerarRelatorioVendas(inicio, fim, 1, "Cat");
        assertEquals(dto, res);
        verify(subSistemaEstatisticas).gerarRelatorioVendas(inicio, fim, 1, "Cat");
    }

    @Test
    void testGerarRelatorioInventario() {
        RelatorioInventarioDTO dto = new RelatorioInventarioDTO(100.0, Collections.emptyList());
        when(subSistemaEstatisticas.gerarRelatorioInventario(1)).thenReturn(dto);
        RelatorioInventarioDTO res = takiLNGlobal.gerarRelatorioInventario(1);
        assertEquals(dto, res);
        verify(subSistemaEstatisticas).gerarRelatorioInventario(1);
    }

    @Test
    void testCalcularTicketMedio() throws DatasInvalidasException {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        when(subSistemaEstatisticas.calcularTicketMedio(inicio, fim)).thenReturn(50.0);
        double res = takiLNGlobal.calcularTicketMedio(inicio, fim);
        assertEquals(50.0, res);
        verify(subSistemaEstatisticas).calcularTicketMedio(inicio, fim);
    }

    @Test
    void testGerarDashboardKPIs() {
        DashboardKPIsDTO dto = new DashboardKPIsDTO(100.0, 10.0, 5, 20.0);
        when(subSistemaEstatisticas.gerarDashboardKPIs(1)).thenReturn(dto);
        DashboardKPIsDTO res = takiLNGlobal.gerarDashboardKPIs(1);
        assertEquals(dto, res);
        verify(subSistemaEstatisticas).gerarDashboardKPIs(1);
    }

    @Test
    void testAtualizarView() {
        takiLNGlobal.atualizarView();
        verify(subSistemaView).atualizarView();
    }

    // --- Lojas ---

    @Test
    void testRegistarLoja() {
        Loja loja = new Loja();
        when(subSistemaLojas.registarLoja(loja)).thenReturn(loja);
        Loja resultado = takiLNGlobal.registarLoja(loja);
        assertEquals(loja, resultado);
        verify(subSistemaLojas).registarLoja(loja);
    }

    @Test
    void testBuscarLoja() {
        int idLoja = 1;
        Loja loja = new Loja();
        loja.setIdLoja(idLoja);
        when(subSistemaLojas.buscarLoja(idLoja)).thenReturn(java.util.Optional.of(loja));
        java.util.Optional<Loja> resultado = takiLNGlobal.buscarLoja(idLoja);
        assertEquals(java.util.Optional.of(loja), resultado);
        verify(subSistemaLojas).buscarLoja(idLoja);
    }

    @Test
    void testListarLojas() {
        List<Loja> lista = new java.util.ArrayList<>();
        when(subSistemaLojas.listarLojas()).thenReturn(lista);
        List<Loja> resultado = takiLNGlobal.listarLojas();
        // Nota: TakiLNGlobal adiciona a loja "Global" ao início da lista
        assertEquals(1, resultado.size());
        assertEquals(0, resultado.get(0).getIdLoja());
        verify(subSistemaLojas).listarLojas();
    }

    @Test
    void testAtualizarLoja() {
        Loja loja = new Loja();
        when(subSistemaLojas.atualizarLoja(loja)).thenReturn(loja);
        Loja resultado = takiLNGlobal.atualizarLoja(loja);
        assertEquals(loja, resultado);
        verify(subSistemaLojas).atualizarLoja(loja);
    }

    @Test
    void testRemoverLoja() {
        int idLoja = 1;
        takiLNGlobal.removerLoja(idLoja);
        verify(subSistemaLojas).removerLoja(idLoja);
    }
}
