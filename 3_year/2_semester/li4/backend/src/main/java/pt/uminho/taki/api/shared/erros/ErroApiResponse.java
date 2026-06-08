package pt.uminho.taki.api.shared.erros;

/**
 * Representa uma resposta de erro da API.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class ErroApiResponse {

    private final String code;
    private final String message;
    private final Object details;
    private final String timestamp;

    /**
     * Constrói uma instância de ErroApiResponse.
     * 
     * @param code o código de erro
     * @param message a mensagem de erro
     * @param details os detalhes do erro
     * @param timestamp o carimbo temporal (timestamp)
     */
    public ErroApiResponse(String code, String message, Object details, String timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    /**
     * Obtém o código de erro.
     * 
     * @return o código de erro
     */
    public String getCode() {
        return code;
    }

    /**
     * Obtém a mensagem de erro.
     * 
     * @return a mensagem de erro
     */
    public String getMessage() {
        return message;
    }

    /**
     * Obtém os detalhes do erro.
     * 
     * @return os detalhes do erro
     */
    public Object getDetails() {
        return details;
    }

    /**
     * Obtém o carimbo temporal (timestamp).
     * 
     * @return o carimbo temporal (timestamp)
     */
    public String getTimestamp() {
        return timestamp;
    }
}
