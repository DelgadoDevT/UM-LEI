package pt.uminho.taki.ln.lojas.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Implementacao do algoritmo de hashing SHA-256 para protecao de credenciais.
 * Esta classe utiliza Base64 para a representacao textual do hash, o que
 * facilita o seu armazenamento em colunas de texto na base de dados.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class SHA256PasswordHasher implements IPasswordHasher {

    @Override
    public String hash(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            // O algoritmo SHA-256 e obrigatorio em todas as implementacoes da JVM.
            throw new RuntimeException("Erro critico: Algoritmo SHA-256 nao encontrado.", e);
        }
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        String hashOfRaw = this.hash(rawPassword);
        return hashOfRaw.equals(hashedPassword);
    }
}
