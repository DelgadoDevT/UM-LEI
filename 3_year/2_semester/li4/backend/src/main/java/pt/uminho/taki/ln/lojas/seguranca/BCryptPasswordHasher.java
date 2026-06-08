package pt.uminho.taki.ln.lojas.seguranca;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Implementação do gerador de hashes de palavra-passe BCrypt.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class BCryptPasswordHasher implements IPasswordHasher {

    private static final int WORK_FACTOR = 12;

    /**
     * Gera a hash de uma palavra-passe.
     *
     * @param password a palavra-passe para a geração da hash
     * @return a palavra-passe em formato hash
     */
    @Override
    public String hash(String password) {
        if (password == null) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifica se uma palavra-passe em texto limpo coincide com uma palavra-passe em formato hash.
     *
     * @param rawPassword a palavra-passe em texto limpo
     * @param hashedPassword a palavra-passe em formato hash
     * @return true se as palavras-passe coincidirem, false caso contrário
     */
    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
