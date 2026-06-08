package pt.uminho.taki.ln.lojas.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para a classe BCryptPasswordHasher.
 */
public class SHA256PasswordHasherTest {

    private BCryptPasswordHasher hasher;
    private final String passwordOriginal = "PasswordSegura123";

    @BeforeEach
    public void setUp() {
        this.hasher = new BCryptPasswordHasher();
    }

    @Test
    @DisplayName("Deve gerar um hash nao nulo e diferente da password original")
    public void testHashGeraStringDiferente() {
        // Arrange & Act
        String hash = hasher.hash(passwordOriginal);

        // Assert
        assertNotNull(hash, "O hash nao deve ser nulo.");
        assertNotEquals(passwordOriginal, hash, "O hash deve ser diferente da password original.");
        assertFalse(hash.isEmpty(), "O hash nao deve ser uma string vazia.");
        assertTrue(hash.startsWith("$2"), "O hash deve seguir o formato BCrypt.");
    }

    @Test
    @DisplayName("Deve gerar hashes diferentes para a mesma password (salt dinâmico)")
    public void testHashComSaltDinamico() {
        String hash1 = hasher.hash(passwordOriginal);
        String hash2 = hasher.hash(passwordOriginal);
        assertNotEquals(hash1, hash2, "Hashes BCrypt para a mesma password devem ser diferentes.");
    }

    @Test
    @DisplayName("Deve devolver true quando a password corresponde ao hash")
    public void testMatchesComSucesso() {
        // Arrange
        String hash = hasher.hash(passwordOriginal);

        // Act
        boolean result = hasher.matches(passwordOriginal, hash);

        // Assert
        assertTrue(result, "O metodo matches deve devolver true para a password correta.");
    }

    @Test
    @DisplayName("Deve devolver false quando a password nao corresponde ao hash")
    public void testMatchesComFalha() {
        // Arrange
        String hash = hasher.hash(passwordOriginal);
        String passwordErrada = "OutraSenha123";

        // Act
        boolean result = hasher.matches(passwordErrada, hash);

        // Assert
        assertFalse(result, "O metodo matches deve devolver false para uma password incorreta.");
    }

    @Test
    @DisplayName("Deve devolver false quando os argumentos sao nulos")
    public void testMatchesComNulos() {
        // Arrange
        String hash = hasher.hash(passwordOriginal);

        // Act & Assert
        assertFalse(hasher.matches(null, hash), "Matches deve devolver false se a password for nula.");
        assertFalse(hasher.matches(passwordOriginal, null), "Matches deve devolver false se o hash for nulo.");
        assertFalse(hasher.matches(null, null), "Matches deve devolver false se ambos forem nulos.");
    }
}
