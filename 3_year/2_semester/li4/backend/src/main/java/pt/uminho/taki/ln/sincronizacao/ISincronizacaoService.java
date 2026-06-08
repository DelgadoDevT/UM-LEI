package pt.uminho.taki.ln.sincronizacao;

import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;

/**
 * Interface que define o contrato para a sincronização de dados entre lojas e a sede.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISincronizacaoService {
    /**
     * Inicia o processo de exportacao noturna (consolidacao em batch da outbox local).
     * @param timestampFecho marcador temporal de inicio
     */
    void exportarLoteParaSede(String timestampFecho) throws FalhaSincronizacaoException;

    /**
     * Consome atualizacoes globais de catalogos da gestao central (PULL temporizado).
     * @param tokenIdentificacaoLoja certificado API
     */
    void importarAtualizacoesGlobais(String tokenIdentificacaoLoja) throws FalhaSincronizacaoException;

    /**
     * Verifica se existe pelo menos um endpoint central disponível por HTTPS.
     * @return true quando existir resposta válida do endpoint central
     */
    boolean verificarDisponibilidadeCentral();

    /**
     * Obtém o indicador percentual de disponibilidade observado nos últimos 30 dias.
     * @return percentagem de disponibilidade (0-100)
     */
    double obterDisponibilidadeUltimos30Dias();
}
