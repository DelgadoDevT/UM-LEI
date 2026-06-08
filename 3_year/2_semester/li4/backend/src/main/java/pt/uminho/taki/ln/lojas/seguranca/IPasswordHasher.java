package pt.uminho.taki.ln.lojas.seguranca;

/**
 * Interface que define o contrato para o hashing e validação de passwords.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IPasswordHasher {
    /**
     * Gera o hash de uma password em texto limpo.
     * @param password a password em texto limpo
     * @return o hash gerado
     */
    String hash(String password);

    /**
     * Verifica se uma password em texto limpo corresponde ao hash fornecido.
     * @param rawPassword a password em texto limpo
     * @param hashedPassword o hash de referencia
     * @return true se as passwords coincidirem, false caso contrario
     */
    boolean matches(String rawPassword, String hashedPassword);
}
