package pt.uminho.taki.ln.sincronizacao;

import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Subsistema de Sincronização.
 * @author TakiLN Team
 * @since 1.0
 */
public class SubSistemaSincronizacao implements ISubSistemaSincronizacao {
    
    private final ISincronizacaoService sincronizacaoService;
    private final ScheduledExecutorService scheduler;
    private volatile boolean agendamentoAtivo;

    /**
     * Construtor para SubSistemaSincronizacao.
     * @param outboxDAO o DAO de outbox
     */
    public SubSistemaSincronizacao(OutboxDAO outboxDAO) {
        this.sincronizacaoService = new SincronizacaoService(outboxDAO);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "taki-sync-scheduler");
            t.setDaemon(true);
            return t;
        });
        iniciarAgendamentoDiario();
    }

    /**
     * Obtém o serviço de sincronização.
     * @return o serviço ISincronizacaoService
     */
    @Override
    public ISincronizacaoService getSincronizacaoService() {
        return this.sincronizacaoService;
    }

    /**
     * Sincroniza os dados com a sede.
     */
    @Override
    public void sincronizarDadosSede() {
        try {
            System.out.println("Iniciando processo de sincronizacao com a sede...");
            this.sincronizacaoService.exportarLoteParaSede(java.time.LocalDateTime.now().toString());
            this.sincronizacaoService.importarAtualizacoesGlobais("TOKEN_PADRAO_LOJA");
            System.out.println("Sincronizacao com a sede concluida com sucesso.");
        } catch (FalhaSincronizacaoException e) {
            System.err.println("Falha critica na sincronizacao: " + e.getMessage());
        }
    }

    /**
     * Inicia o agendamento diário de sincronização.
     */
    @Override
    public void iniciarAgendamentoDiario() {
        if (agendamentoAtivo) return;
        long delayInicial = segundosAteFimDoDia();
        scheduler.scheduleAtFixedRate(this::sincronizarDadosSede, delayInicial, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        agendamentoAtivo = true;
    }

    /**
     * Obtém a disponibilidade da central.
     * @return o valor decimal
     */
    @Override
    public double obterDisponibilidadeCentral() {
        return this.sincronizacaoService.obterDisponibilidadeUltimos30Dias();
    }

    /**
     * Calcula o número de segundos que faltam até ao final do dia para o agendamento da próxima sincronização.
     * 
     * @return o total de segundos até à execução planeada
     */
    private long segundosAteFimDoDia() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime proximaExecucao = LocalDateTime.of(agora.toLocalDate(), LocalTime.of(23, 59, 0));
        if (!agora.isBefore(proximaExecucao)) {
            proximaExecucao = proximaExecucao.plusDays(1);
        }
        long segundos = Duration.between(agora, proximaExecucao).getSeconds();
        return Math.max(5, segundos);
    }
}
