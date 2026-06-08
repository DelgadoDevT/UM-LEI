package pt.uminho.taki.ln.sincronizacao.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma entrada na outbox de sincronização.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class OutboxEntry implements Cloneable {
    private Long idFila;
    private String nomeTabela;
    private String idEntidade;
    private String operacao;
    private LocalDateTime dataRegisto;

    /**
     * Constrói uma nova OutboxEntry com o nome da tabela, identificador da entidade e operação especificados.
     *
     * @param nomeTabela o nome da tabela.
     * @param idEntidade o identificador da entidade.
     * @param operacao a operação realizada.
     */
    public OutboxEntry(String nomeTabela, String idEntidade, String operacao) {
        this.idFila = null;
        this.nomeTabela = nomeTabela;
        this.idEntidade = idEntidade;
        this.operacao = operacao;
        this.dataRegisto = LocalDateTime.now();
    }

    /**
     * Constrói uma nova OutboxEntry com todos os detalhes.
     *
     * @param idFila o identificador da fila.
     * @param nomeTabela o nome da tabela.
     * @param idEntidade o identificador da entidade.
     * @param operacao a operação realizada.
     * @param dataRegisto o carimbo de data/hora do registo.
     */
    public OutboxEntry(Long idFila, String nomeTabela, String idEntidade, String operacao, LocalDateTime dataRegisto) {
        this.idFila = idFila;
        this.nomeTabela = nomeTabela;
        this.idEntidade = idEntidade;
        this.operacao = operacao;
        this.dataRegisto = dataRegisto;
    }

    /**
     * Obtém o identificador da fila.
     *
     * @return o identificador da fila.
     */
    public Long getIdFila() { return idFila; }

    /**
     * Define o identificador da fila.
     *
     * @param idFila o identificador da fila.
     */
    public void setIdFila(Long idFila) { this.idFila = idFila; }

    /**
     * Obtém o nome da tabela.
     *
     * @return o nome da tabela.
     */
    public String getNomeTabela() { return nomeTabela; }

    /**
     * Obtém o identificador da entidade.
     *
     * @return o identificador da entidade.
     */
    public String getIdEntidade() { return idEntidade; }

    /**
     * Obtém a operação realizada.
     *
     * @return a operação realizada.
     */
    public String getOperacao() { return operacao; }

    /**
     * Obtém o carimbo de data/hora do registo.
     *
     * @return o carimbo de data/hora do registo.
     */
    public LocalDateTime getDataRegisto() { return dataRegisto; }

    /**
     * Construtor de cópia para OutboxEntry.
     *
     * @param outra a OutboxEntry a copiar.
     */
    public OutboxEntry(OutboxEntry outra) {
        this.idFila = outra.idFila;
        this.nomeTabela = outra.nomeTabela;
        this.idEntidade = outra.idEntidade;
        this.operacao = outra.operacao;
        this.dataRegisto = outra.dataRegisto;
    }

    /**
     * Indica se algum outro objeto é "igual a" este.
     *
     * @param o o objeto de referência para comparação.
     * @return verdadeiro se este objeto seja o mesmo que o argumento; falso caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEntry that = (OutboxEntry) o;
        if (idFila != null && that.idFila != null) {
            return Objects.equals(idFila, that.idFila);
        }
        return Objects.equals(nomeTabela, that.nomeTabela)
            && Objects.equals(idEntidade, that.idEntidade)
            && Objects.equals(operacao, that.operacao);
    }

    /**
     * Retorna um valor de código hash para o objeto.
     *
     * @return um valor de código hash para este objeto.
     */
    @Override
    public int hashCode() {
        if (idFila != null) {
            return Objects.hash(idFila);
        }
        return Objects.hash(nomeTabela, idEntidade, operacao);
    }

    /**
     * Cria e retorna uma cópia deste objeto.
     *
     * @return um clone desta instância.
     */
    @Override
    public OutboxEntry clone() {
        return new OutboxEntry(this);
    }
}
