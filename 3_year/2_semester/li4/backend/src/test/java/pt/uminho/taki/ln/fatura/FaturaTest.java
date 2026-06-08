package pt.uminho.taki.ln.fatura;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FaturaTest {

    @Test
    void testFaturaCreation() {
        // Arrange
        String numFatura = "FT 2024/1";
        String nifCliente = "123456789";
        String idVenda = "venda-123";

        // Act
        Fatura fatura = new Fatura(numFatura, nifCliente, idVenda);

        // Assert
        assertNotNull(fatura.getIdFatura());
        assertEquals(numFatura, fatura.getNumFatura());
        assertEquals(nifCliente, fatura.getNifCliente());
        assertEquals(idVenda, fatura.getIdVenda());
        assertNotNull(fatura.getDataEmissao());
        assertEquals("1", fatura.getHashControl());
    }

    @Test
    void testFaturaClone() {
        // Arrange
        Fatura original = new Fatura("FT 2024/1", "123456789", "venda-123");
        original.setHash("some-hash");

        // Act
        Fatura clone = original.clone();

        // Assert
        assertEquals(original, clone);
        assertNotSame(original, clone);
        assertEquals(original.getHash(), clone.getHash());
    }

    @Test
    void testFaturaEquals() {
        // Arrange
        Fatura f1 = new Fatura("FT 2024/1", "123456789", "venda-123");
        Fatura f2 = new Fatura(f1);

        // Act & Assert
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }
}
