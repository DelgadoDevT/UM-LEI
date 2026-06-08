package pt.uminho.taki.api.global.dto;

/**
 * Objeto de Transferência de Dados (DTO) para respostas de mensagem.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MensagemResponseDto {

    private final String code;
    private final String message;

    /**
     * Constrói uma instância de MensagemResponseDto.
     *
     * @param code o código da mensagem
     * @param message o conteúdo da mensagem
     */
    public MensagemResponseDto(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Cria uma resposta de mensagem de sucesso.
     *
     * @param code o código da mensagem
     * @param message o conteúdo da mensagem
     * @return uma nova instância de MensagemResponseDto
     */
    public static MensagemResponseDto sucesso(String code, String message) {
        return new MensagemResponseDto(code, message);
    }

    /**
     * Obtém o código da mensagem.
     *
     * @return o código da mensagem
     */
    public String getCode() {
        return code;
    }

    /**
     * Obtém o conteúdo da mensagem.
     *
     * @return o conteúdo da mensagem
     */
    public String getMessage() {
        return message;
    }
}
