package pt.uminho.taki.ln.sincronizacao;

/**
 * Interface do subsistema para a gestão de sincronização.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaSincronizacao {
    /**
     * Obtém o serviço de sincronização.
     * 
     * @return o serviço de sincronização
     */
    ISincronizacaoService getSincronizacaoService();

    /**
     * Sincroniza os dados com a sede central.
     */
    void sincronizarDadosSede();

    /**
     * Inicia o agendamento diário para a sincronização.
     */
    void iniciarAgendamentoDiario();

    /**
     * Obtém a disponibilidade do sistema central.
     * 
     * @return a percentagem ou o estado de disponibilidade
     */
    double obterDisponibilidadeCentral();
}
