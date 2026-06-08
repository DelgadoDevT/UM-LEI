package pt.uminho.taki.ln.vendas;

import java.time.LocalDateTime;

/**
 * Representa uma devolução.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class Devolucao {
    private String idDevolucao;
    private LocalDateTime dataHora;
    private double valor;
    private String metodoReembolso;
    private String numNotaCredito;
    private String idVenda;
    private String idFuncionario;

    /**
     * Construtor por omissão.
     */
    public Devolucao() {
    }

    /**
     * Constrói uma Devolucao.
     * 
     * @param idDevolucao o identificador da devolução
     * @param dataHora a data e hora
     * @param valor o valor
     * @param idVenda o identificador da venda
     */
    public Devolucao(String idDevolucao, LocalDateTime dataHora, double valor, String idVenda) {
        this(idDevolucao, dataHora, valor, "Original", null, idVenda, null);
    }

    /**
     * Constrói uma Devolucao.
     * 
     * @param idDevolucao o identificador da devolução
     * @param dataHora a data e hora
     * @param valor o valor
     * @param idVenda o identificador da venda
     * @param idFuncionario o identificador do funcionário
     */
    public Devolucao(String idDevolucao, LocalDateTime dataHora, double valor, String idVenda, String idFuncionario) {
        this(idDevolucao, dataHora, valor, "Original", null, idVenda, idFuncionario);
    }

    /**
     * Constrói uma Devolucao.
     * 
     * @param idDevolucao o identificador da devolução
     * @param dataHora a data e hora
     * @param valor o valor
     * @param metodoReembolso o método de reembolso
     * @param numNotaCredito o número da nota de crédito
     * @param idVenda o identificador da venda
     * @param idFuncionario o identificador do funcionário
     */
    public Devolucao(String idDevolucao, LocalDateTime dataHora, double valor, String metodoReembolso, String numNotaCredito, String idVenda, String idFuncionario) {
        this.idDevolucao = idDevolucao;
        this.dataHora = dataHora;
        this.valor = valor;
        this.metodoReembolso = metodoReembolso;
        this.numNotaCredito = numNotaCredito;
        this.idVenda = idVenda;
        this.idFuncionario = idFuncionario;
    }

    // Getters and Setters
    /**
     * Obtém o identificador da devolução.
     * 
     * @return o identificador da devolução
     */
    public String getIdDevolucao() {
        return idDevolucao;
    }

    /**
     * Define o identificador da devolução.
     * 
     * @param idDevolucao o identificador da devolução
     */
    public void setIdDevolucao(String idDevolucao) {
        this.idDevolucao = idDevolucao;
    }

    /**
     * Obtém a data e hora.
     * 
     * @return a data e hora
     */
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /**
     * Define a data e hora.
     * 
     * @param dataHora a data e hora
     */
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    /**
     * Obtém o valor.
     * 
     * @return o valor
     */
    public double getValor() {
        return valor;
    }

    /**
     * Define o valor.
     * 
     * @param valor o valor
     */
    public void setValor(double valor) {
        this.valor = valor;
    }

    /**
     * Obtém o identificador da venda.
     * 
     * @return o identificador da venda
     */
    public String getIdVenda() {
        return idVenda;
    }

    /**
     * Define o identificador da venda.
     * 
     * @param idVenda o identificador da venda
     */
    public void setIdVenda(String idVenda) {
        this.idVenda = idVenda;
    }

    /**
     * Obtém o método de reembolso.
     * 
     * @return o método de reembolso
     */
    public String getMetodoReembolso() {
        return metodoReembolso;
    }

    /**
     * Define o método de reembolso.
     * 
     * @param metodoReembolso o método de reembolso
     */
    public void setMetodoReembolso(String metodoReembolso) {
        this.metodoReembolso = metodoReembolso;
    }

    /**
     * Obtém o número da nota de crédito.
     * 
     * @return o número da nota de crédito
     */
    public String getNumNotaCredito() {
        return numNotaCredito;
    }

    /**
     * Define o número da nota de crédito.
     * 
     * @param numNotaCredito o número da nota de crédito
     */
    public void setNumNotaCredito(String numNotaCredito) {
        this.numNotaCredito = numNotaCredito;
    }

    /**
     * Obtém o identificador do funcionário.
     * 
     * @return o identificador do funcionário
     */
    public String getIdFuncionario() {
        return idFuncionario;
    }

    /**
     * Define o identificador do funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     */
    public void setIdFuncionario(String idFuncionario) {
        this.idFuncionario = idFuncionario;
    }
}
