package pt.uminho.taki.api.global.dto;

/**
 * DTO para a resposta de um valor de métrica.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class ValorMetricaResponseDto {

    private final String metrica;
    private final double valor;

    /**
     * Constrói uma nova ValorMetricaResponseDto.
     * 
     * @param metrica o nome da métrica
     * @param valor o valor da métrica
     */
    public ValorMetricaResponseDto(String metrica, double valor) {
        this.metrica = metrica;
        this.valor = valor;
    }

    /**
     * Obtém o nome da métrica.
     * 
     * @return o nome da métrica
     */
    public String getMetrica() {
        return metrica;
    }

    /**
     * Obtém o valor da métrica.
     * 
     * @return o valor da métrica
     */
    public double getValor() {
        return valor;
    }
}
