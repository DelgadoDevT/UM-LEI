package pt.uminho.taki.ln.vendas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.PromocaoDAO;
import pt.uminho.taki.ln.lojas.Produto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PromocaoServiceTest {

    @Mock
    private PromocaoDAO promocaoDAOMock;
    @Mock
    private pt.uminho.taki.dao.ProdutoDAO produtoDAOMock;
    @Mock
    private pt.uminho.taki.ln.lojas.ICategoriaService categoriaServiceMock;

    private PromocaoService promocaoService;
    private Produto produtoMock;

    @BeforeEach
    public void setUp() {
        // Arrange general
        promocaoService = new PromocaoService(promocaoDAOMock, produtoDAOMock, categoriaServiceMock);
        
        produtoMock = new Produto();
        produtoMock.setIdProduto("PROD01");
        produtoMock.setPrecoVenda(100.0);
    }

    @Test
    public void testCalcularMelhorDesconto_SemPromocoesAtivas() {
        // Arrange
        when(promocaoDAOMock.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        double desconto = promocaoService.calcularMelhorDesconto(produtoMock);

        // Assert
        assertEquals(0.0, desconto, 0.001);
    }

    @Test
    public void testCalcularMelhorDesconto_ComPromocaoCaducadaEValida() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();
        
        Promocao promocaoCaducada = new Promocao("1", "Promo Antiga", 20.0, agora.minusDays(10), agora.minusDays(1), "Ativa", 1);
        Promocao promocaoAtiva = new Promocao("2", "Promo Atual", 10.0, agora.minusDays(1), agora.plusDays(5), "Ativa", 1);
        
        when(promocaoDAOMock.findAll()).thenReturn(Arrays.asList(promocaoCaducada, promocaoAtiva));
        // Mock getProdutos because PromocaoService uses DAO to populate the objects inside getPromocoesAtivas
        when(promocaoDAOMock.getProdutos("2")).thenReturn(new HashSet<>(Arrays.asList("PROD01")));

        // Act
        double desconto = promocaoService.calcularMelhorDesconto(produtoMock);

        // Assert
        assertEquals(10.0, desconto, 0.001); // O desconto de 10% deve ganhar porque a outra caducou
    }

    @Test
    public void testCalcularMelhorDesconto_VariasPromocoesAtivas_EscolheAMaior() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();
        
        Promocao promoPequena = new Promocao("1", "Promo Baixa", 5.0, agora.minusDays(1), agora.plusDays(5), "Ativa", 1);
        // Promo Global com 15% (sem produtos específicos associados = aplica a todos)
        Promocao promoGlobalMaior = new Promocao("2", "Promo Global Maior", 15.0, agora.minusDays(1), agora.plusDays(5), "Ativa", 1);
        Promocao promoEspecificaEnorme = new Promocao("3", "Promo Enorme Produto", 25.0, agora.minusDays(1), agora.plusDays(5), "Ativa", 1);
        
        when(promocaoDAOMock.findAll()).thenReturn(Arrays.asList(promoPequena, promoGlobalMaior, promoEspecificaEnorme));
        
        when(promocaoDAOMock.getProdutos("1")).thenReturn(new HashSet<>(Arrays.asList("PROD01")));
        when(promocaoDAOMock.getProdutos("2")).thenReturn(Collections.emptySet());
        when(promocaoDAOMock.getProdutos("3")).thenReturn(new HashSet<>(Arrays.asList("PROD01")));

        // Act
        double desconto = promocaoService.calcularMelhorDesconto(produtoMock);

        // Assert
        assertEquals(25.0, desconto, 0.001); // Deve escolher a de 25% porque se aplica a este PROD01
    }

    @Test
    public void testAdicionarPromocaoComDatasInvalidasLancaExcecao() {
        LocalDateTime agora = LocalDateTime.now();
        Promocao invalida = new Promocao("10", "Inválida", 10.0, agora.plusDays(1), agora, "Ativa", 1);
        assertThrows(IllegalArgumentException.class, () -> promocaoService.adicionarPromocao(invalida));
    }

    @Test
    public void testAdicionarPromocaoComDescontoInvalidoLancaExcecao() {
        LocalDateTime agora = LocalDateTime.now();
        Promocao invalida = new Promocao("11", "Inválida", 150.0, agora, agora.plusDays(1), "Ativa", 1);
        assertThrows(IllegalArgumentException.class, () -> promocaoService.adicionarPromocao(invalida));
    }

    @Test
    public void testCancelarPromocaoAtualizaEstadoParaCancelada() {
        Promocao promocao = new Promocao("22", "Promo", 10.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), "Ativa", 1);
        when(promocaoDAOMock.findById("22")).thenReturn(Optional.of(promocao));

        promocaoService.cancelarPromocao("22", "Fim antecipado");

        verify(promocaoDAOMock).save(eq("22"), any(Promocao.class));
        assertEquals("Cancelada", promocao.getEstado());
    }

    @Test
    public void testCancelarPromocaoSemMotivoFalha() {
        assertThrows(IllegalArgumentException.class, () -> promocaoService.cancelarPromocao("22", " "));
    }
}
