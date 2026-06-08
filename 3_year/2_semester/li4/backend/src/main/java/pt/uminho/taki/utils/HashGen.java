package pt.uminho.taki.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário gerador de hashes.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class HashGen {
    /**
     * Método principal.
     *
     * @param args os argumentos
     */
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("admin", BCrypt.gensalt(10));
        System.out.println(hash);
    }
}
