package pt.uminho.taki.ln.sincronizacao.exceptions;

/**
 * Exceção lançada quando ocorre uma falha de sincronização.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FalhaSincronizacaoException extends Exception {
    /**
     * Constrói uma nova instância de FalhaSincronizacaoException com a mensagem de detalhe especificada.
     *
     * @param message a mensagem de detalhe
     */
    public FalhaSincronizacaoException(String message) {
        super(message);
    }
}
