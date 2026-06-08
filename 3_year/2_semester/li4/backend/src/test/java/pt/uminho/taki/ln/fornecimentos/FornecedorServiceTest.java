package pt.uminho.taki.ln.fornecimentos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorDAO fornecedorDAO;

    @InjectMocks
    private FornecedorService fornecedorService;

    @Test
    @DisplayName("Deve registar fornecedor com sucesso quando dados são válidos")
    void deveRegistarFornecedorComSucesso() {
        // Arrange
        Fornecedor f = new Fornecedor("F1", "Bebidas SA", "501234567", "+351 912345678", "mail@mail.pt", "Ativo");
        when(fornecedorDAO.findByNif(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> {
            fornecedorService.adicionarFornecedor(f);
        });
        verify(fornecedorDAO).save(eq("F1"), any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve permitir registar fornecedor se o NIF existir mas estiver inativo")
    void devePermitirRegistoSeNifExistenteInativo() {
        // Arrange
        Fornecedor novo = new Fornecedor("F2", "Nova SA", "501234567", "912345678", "n@n.pt", "Ativo");
        Fornecedor inativo = new Fornecedor("F1", "Velha SA", "501234567", "912345678", "v@v.pt", "Inativo");
        when(fornecedorDAO.findByNif("501234567")).thenReturn(Optional.of(inativo));

        // Act & Assert
        assertDoesNotThrow(() -> fornecedorService.adicionarFornecedor(novo));
        verify(fornecedorDAO).save(eq("F2"), eq(novo));
    }

    @Test
    @DisplayName("Deve falhar registo se o NIF já pertencer a um fornecedor ativo")
    void deveFalharSeNifAtivoExiste() {
        // Arrange
        Fornecedor novo = new Fornecedor("F2", "Nova SA", "501234567", "912345678", "n@n.pt", "Ativo");
        Fornecedor ativo = new Fornecedor("F1", "Ativa SA", "501234567", "912345678", "a@a.pt", "Ativo");
        when(fornecedorDAO.findByNif("501234567")).thenReturn(Optional.of(ativo));

        // Act & Assert
        assertThrows(FornecedorExistenteException.class, () -> fornecedorService.adicionarFornecedor(novo));
    }

    @Test
    @DisplayName("Deve inativar fornecedor com sucesso")
    void inativarFornecedorComSucesso() throws FornecedorInativoException {
        // Arrange
        String idFornecedor = "F1";
        Fornecedor f = new Fornecedor(idFornecedor, "Bebidas SA", "501234567", "+351 912345678", "mail@mail.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(f));
        when(fornecedorDAO.hasEncomendasPendentes(idFornecedor)).thenReturn(false);

        // Act
        fornecedorService.inativarFornecedor(idFornecedor);

        // Assert
        ArgumentCaptor<Fornecedor> captor = ArgumentCaptor.forClass(Fornecedor.class);
        verify(fornecedorDAO).save(eq(idFornecedor), captor.capture());
        assertEquals("Inativo", captor.getValue().getEstado());
    }

    @Test
    @DisplayName("Deve falhar inativação quando existem encomendas pendentes")
    void inativarFornecedorComEncomendasPendentesFalha() {
        String idFornecedor = "F1";
        Fornecedor f = new Fornecedor(idFornecedor, "Bebidas SA", "501234567", "+351 912345678", "mail@mail.pt", "Ativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(f));
        when(fornecedorDAO.hasEncomendasPendentes(idFornecedor)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fornecedorService.inativarFornecedor(idFornecedor));
        verify(fornecedorDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve falhar inativação se o fornecedor já estiver inativo")
    void inativarFornecedorJaInativoLancaExcecao() {
        // Arrange
        String idFornecedor = "F2";
        Fornecedor f = new Fornecedor(idFornecedor, "Outro SA", "509876543", "+351 912345678", "x@x.pt", "Inativo");
        when(fornecedorDAO.findById(idFornecedor)).thenReturn(Optional.of(f));

        // Act & Assert
        assertThrows(FornecedorInativoException.class, () -> fornecedorService.inativarFornecedor(idFornecedor));
    }

    @Test
    @DisplayName("Não deve fazer nada ao inativar fornecedor inexistente")
    void naoFazNadaSeInativarInexistente() throws FornecedorInativoException {
        // Arrange
        when(fornecedorDAO.findById("X")).thenReturn(Optional.empty());

        // Act
        fornecedorService.inativarFornecedor("X");

        // Assert
        verify(fornecedorDAO, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("Deve rejeitar fornecedor com NIF inválido")
    void rejeitarFornecedorComNifInvalido() {
        // Arrange
        Fornecedor f1 = new Fornecedor("F1", "Test", "123", "912345678", "m@m.pt", "Ativo");
        Fornecedor f2 = new Fornecedor("F1", "Test", null, "912345678", "m@m.pt", "Ativo");

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class, () -> fornecedorService.adicionarFornecedor(f1));
        assertThrows(CamposObrigatoriosEmFaltaException.class, () -> fornecedorService.adicionarFornecedor(f2));
    }

    @Test
    @DisplayName("Deve rejeitar fornecedor com contacto telefónico inválido")
    void rejeitarFornecedorComContactoInvalido() {
        // Arrange
        Fornecedor f1 = new Fornecedor("F1", "Test", "501234567", "invalid", "mail@mail.pt", "Ativo");
        Fornecedor f2 = new Fornecedor("F1", "Test", "501234567", null, "mail@mail.pt", "Ativo");

        // Act & Assert
        assertThrows(CamposObrigatoriosEmFaltaException.class, () -> fornecedorService.adicionarFornecedor(f1));
        assertThrows(CamposObrigatoriosEmFaltaException.class, () -> fornecedorService.adicionarFornecedor(f2));
    }
}
