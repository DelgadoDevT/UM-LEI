package pt.uminho.taki.ln.sincronizacao.dto;

import java.util.List;

/**
 * Representa um pacote de fecho integrado que contém dados de sincronização.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PacoteFechoIntegrado {
    private String idLoja;
    private String timestampFecho;
    private List<String> idRecebimentosPendentes;
    private double valorApurado;
    
    /**
     * Constrói um novo PacoteFechoIntegrado com os detalhes especificados.
     *
     * @param idLoja o identificador da loja.
     * @param timestampFecho o carimbo de data/hora do fecho.
     * @param ids a lista de identificadores de recebimentos pendentes.
     * @param valorApurado o valor apurado.
     */
    public PacoteFechoIntegrado(String idLoja, String timestampFecho, List<String> ids, double valorApurado) {
        this.idLoja = idLoja;
        this.timestampFecho = timestampFecho;
        this.idRecebimentosPendentes = ids;
        this.valorApurado = valorApurado;
    }

    /**
     * Obtém o identificador da loja.
     *
     * @return o identificador da loja.
     */
    public String getIdLoja() { return idLoja; }

    /**
     * Obtém o carimbo de data/hora do fecho.
     *
     * @return o carimbo de data/hora do fecho.
     */
    public String getTimestampFecho() { return timestampFecho; }

    /**
     * Obtém a lista de identificadores de recebimentos pendentes.
     *
     * @return a lista de identificadores de recebimentos pendentes.
     */
    public List<String> getIdRecebimentosPendentes() { return idRecebimentosPendentes; }

    /**
     * Obtém o valor apurado.
     *
     * @return o valor apurado.
     */
    public double getValorApurado() { return valorApurado; }
}
