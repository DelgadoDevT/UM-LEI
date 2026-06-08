package pt.uminho.taki.ln.fornecimentos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncomendaEstadoTest {

    @Test
    public void testeBloqueioAdicionarLinhaForaDeRascunho() {
        Encomenda enc = new Encomenda("ENC-1", "F1", "L1");
        
        // Em rascunho deve aceitar
        assertDoesNotThrow(() -> enc.adicionarLinha(new LinhaEncomenda("ENC-1", "P1", 10.0, 5.0)));
        
        // Avanca para pendente
        enc.avancarEstado();
        
        // Fora de rascunho deve estoirar (Domain protection)
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            enc.adicionarLinha(new LinhaEncomenda("ENC-1", "P2", 20.0, 2.0))
        );
        assertTrue(exception.getMessage().contains("bloqueada/assinada"));
    }
    
    @Test
    public void testAvancoDeEstadoConcluidoNaoPermitido() {
        Encomenda enc = new Encomenda("ENC-2", "F2", "L1");
        enc.avancarEstado(); // Rascunho -> Pendente
        enc.avancarEstado(); // Pendente -> Enviada
        enc.avancarEstado(); // Enviada -> Concluida
        
        // Tentar avancar Concluida 
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            enc.avancarEstado()
        );
        assertTrue(exception.getMessage().contains("concluída"));
    }

    @Test
    void cancelarEncomendaPendenteTransicionaParaCancelada() {
        // Arrange
        Encomenda enc = new Encomenda("ENC-1", "F1", "L1");
        enc.avancarEstado(); // Rascunho -> Pendente

        // Act
        enc.getEstadoAtual().cancelar(enc);

        // Assert
        assertEquals("Cancelada", enc.getEstadoAtual().getDesignacao());
    }

    @Test
    void cancelarEncomendaRascunhoLancaExcecao() {
        // Arrange
        Encomenda enc = new Encomenda("ENC-1", "F1", "L1");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> enc.getEstadoAtual().cancelar(enc));
    }
}
