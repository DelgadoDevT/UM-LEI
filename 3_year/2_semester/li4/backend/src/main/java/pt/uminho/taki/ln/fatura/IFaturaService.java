package pt.uminho.taki.ln.fatura;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface para o serviço de gestão de faturas.
 * Define as operações necessárias para emissão e validação de faturas.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface IFaturaService {
    /**
     * Emite uma nova fatura para uma venda.
     * @param idVenda ID da venda.
     * @param nifCliente NIF do cliente (opcional).
     * @return A fatura emitida.
     * @throws Exception se houver erros na geração da assinatura ou regras de integridade.
     */
    Fatura emitirFatura(String idVenda, String nifCliente) throws Exception;

    /**
     * Obtém uma fatura pelo seu número legal.
     * @param numFatura Número da fatura.
     * @return A fatura encontrada ou null.
     */
    Fatura getFatura(String numFatura);

    /**
     * Lista todas as faturas emitidas.
     * @return Lista de faturas.
     */
    List<Fatura> getFaturas();

    /**
     * Obtém a segunda via de uma fatura existente.
     * @param numFatura Número da fatura original.
     * @return Cópia da fatura para reimpressão.
     */
    Fatura emitirSegundaVia(String numFatura);

    /**
     * Exporta uma fatura em formato JSON.
     * @param numFatura Número da fatura.
     * @return Conteúdo JSON da fatura.
     */
    String exportarFaturaJson(String numFatura);

    /**
     * Exporta uma fatura em formato CSV.
     * @param numFatura Número da fatura.
     * @return Conteúdo CSV da fatura.
     */
    String exportarFaturaCsv(String numFatura);

    /**
     * Exporta a faturação em formato SAF-T (PT) para um período.
     * @param dataInicio data inicial (inclusive)
     * @param dataFim data final (inclusive)
     * @return XML SAF-T simplificado para integração fiscal
     */
    String exportarSaftPt(LocalDate dataInicio, LocalDate dataFim);

    /**
     * Gera uma segunda via da fatura em formato PDF (simulado por texto base64/blob).
     * @param numFatura Número da fatura
     * @return o documento em bytes
     */
    byte[] gerarFaturaPDF(String numFatura);

    /**
     * Valida a integridade da sequência de faturas (hashes).
     * @return true se a integridade estiver preservada.
     */
    boolean validarIntegridade();
}
