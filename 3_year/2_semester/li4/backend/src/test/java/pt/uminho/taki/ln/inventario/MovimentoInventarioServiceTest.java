package pt.uminho.taki.ln.inventario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.ln.inventario.exceptions.DataInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.MotivoObrigatorioException;
import pt.uminho.taki.ln.inventario.exceptions.QuantidadeInvalidaException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para o MovimentoInventarioService apos limpeza de redundancias.
 */
@ExtendWith(MockitoExtension.class)
public class MovimentoInventarioServiceTest {

    @Mock
    private InventarioDAO inventarioDAO;

    @InjectMocks
    private MovimentoInventarioService movimentoService;

    private MovimentoInventario movimentoValido;

    @BeforeEach
    public void setUp() {
        this.movimentoValido = new MovimentoInventario("M1", TipoMovimento.ENTRADA, 10.0, LocalDateTime.now().minusMinutes(5), "Reposicao", "INV1", "F1");
    }

    @Test
    @DisplayName("Deve validar um movimento válido com sucesso")
    public void testRegistoSucesso() throws Exception {
        movimentoService.registarMovimento(movimentoValido);
    }

    @Test
    @DisplayName("Deve validar movimento com data nula com sucesso")
    public void testRegistoDataNulaSucesso() throws Exception {
        movimentoValido.setDataRegisto(null);
        movimentoService.registarMovimento(movimentoValido);
    }

    @Test
    @DisplayName("Deve validar um movimento de Quebra com motivo com sucesso")
    public void testRegistoQuebraComMotivoSucesso() throws Exception {
        movimentoValido.setTipo(TipoMovimento.QUEBRA);
        movimentoValido.setMotivo("Garrafa partida");
        movimentoService.registarMovimento(movimentoValido);
    }

    @Test
    @DisplayName("Deve falhar o registo se a quantidade for zero ou negativa")
    public void testRegistoFalhaQuantidadeInvalida() {
        movimentoValido.setQuantidade(0.0);
        assertThrows(QuantidadeInvalidaException.class, () -> {
            movimentoService.registarMovimento(movimentoValido);
        });
    }

    @Test
    @DisplayName("Deve falhar o registo se a data for no futuro")
    public void testRegistoFalhaDataFutura() {
        movimentoValido.setDataRegisto(LocalDateTime.now().plusDays(1));
        assertThrows(DataInvalidaException.class, () -> {
            movimentoService.registarMovimento(movimentoValido);
        });
    }

    @Test
    @DisplayName("Deve falhar se Quebra nao tiver motivo")
    public void testRegistoFalhaMotivoQuebra() {
        movimentoValido.setTipo(TipoMovimento.QUEBRA);
        movimentoValido.setMotivo("");

        assertThrows(MotivoObrigatorioException.class, () -> {
            movimentoService.registarMovimento(movimentoValido);
        });
    }
}
