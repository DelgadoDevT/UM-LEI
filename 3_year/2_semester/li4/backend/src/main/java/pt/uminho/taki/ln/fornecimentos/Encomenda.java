package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.ln.fornecimentos.estados.IEstadoEncomenda;
import pt.uminho.taki.ln.fornecimentos.estados.EstadoRascunho;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa uma encomenda.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class Encomenda {
    /** Identificador da encomenda. */
    private String idEncomenda;
    /** Identificador do fornecedor. */
    private String idFornecedor;
    /** Identificador da loja. */
    private String idLoja;
    /** Data e hora de criação da encomenda. */
    private LocalDateTime dataCriacao;
    /** Data e hora de entrega da encomenda. */
    private LocalDateTime dataEntrega;
    /** Lista de linhas de encomenda. */
    private List<LinhaEncomenda> linhas;
    /** Estado atual da encomenda. */
    private IEstadoEncomenda estadoAtual;

    /**
     * Constrói uma Encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     */
    public Encomenda(String idEncomenda, String idFornecedor, String idLoja) {
        this.idEncomenda = idEncomenda;
        this.idFornecedor = idFornecedor;
        this.idLoja = idLoja;
        this.dataCriacao = LocalDateTime.now();
        this.dataEntrega = null;
        this.linhas = new ArrayList<>();
        this.estadoAtual = new EstadoRascunho();
    }

    /**
     * Constrói uma cópia de uma Encomenda.
     * 
     * @param outra a outra encomenda a copiar
     */
    public Encomenda(Encomenda outra) {
        this.idEncomenda = outra.idEncomenda;
        this.idFornecedor = outra.idFornecedor;
        this.idLoja = outra.idLoja;
        this.dataCriacao = outra.dataCriacao;
        this.dataEntrega = outra.dataEntrega;
        this.linhas = new ArrayList<>(outra.linhas);
        this.estadoAtual = another(outra.estadoAtual);
    }

    private IEstadoEncomenda another(IEstadoEncomenda e) {
        return e; // Simple copy for now
    }

    /**
     * Obtém o identificador da encomenda.
     * 
     * @return o identificador da encomenda
     */
    public String getIdEncomenda() { return idEncomenda; }

    /**
     * Obtém o identificador do fornecedor.
     * 
     * @return o identificador do fornecedor
     */
    public String getIdFornecedor() { return idFornecedor; }

    /**
     * Obtém o identificador da loja.
     * 
     * @return o identificador da loja
     */
    public String getIdLoja() { return idLoja; }

    /**
     * Define o identificador da loja.
     * 
     * @param idLoja o identificador da loja
     */
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }

    /**
     * Define o identificador da loja a partir de um inteiro.
     * 
     * @param idLoja o identificador da loja como inteiro
     */
    public void setIdLoja(int idLoja) { this.idLoja = String.valueOf(idLoja); }

    /**
     * Obtém a data e hora de criação.
     * 
     * @return a data e hora de criação
     */
    public LocalDateTime getDataCriacao() { return dataCriacao; }

    /**
     * Define a data e hora de criação.
     * 
     * @param dataCriacao a data e hora de criação
     */
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    /**
     * Obtém a data e hora de entrega.
     * 
     * @return a data e hora de entrega
     */
    public LocalDateTime getDataEntrega() { return dataEntrega; }

    /**
     * Define a data e hora de entrega.
     * 
     * @param dataEntrega a data e hora de entrega
     */
    public void setDataEntrega(LocalDateTime dataEntrega) { this.dataEntrega = dataEntrega; }

    /**
     * Obtém a data de entrega como LocalDate.
     * 
     * @return a data de entrega
     */
    public java.time.LocalDate getDataEntregaAsDate() { 
        return dataEntrega != null ? dataEntrega.toLocalDate() : null; 
    }

    /**
     * Define a data de entrega a partir de um LocalDate.
     * 
     * @param data a data de entrega
     */
    public void setDataEntrega(java.time.LocalDate data) { 
        this.dataEntrega = data != null ? data.atStartOfDay() : null; 
    }

    /**
     * Obtém as linhas da encomenda.
     * 
     * @return as linhas da encomenda
     */
    public List<LinhaEncomenda> getLinhas() { return Collections.unmodifiableList(linhas); }
    
    /**
     * Define as linhas da encomenda.
     * 
     * @param novasLinhas as novas linhas da encomenda
     */
    public void setLinhas(List<LinhaEncomenda> novasLinhas) {
        this.linhas = new ArrayList<>(novasLinhas);
    }

    /**
     * Adiciona uma linha à encomenda.
     * 
     * @param linha a linha da encomenda a adicionar
     */
    public void adicionarLinha(LinhaEncomenda linha) {
        if (!this.estadoAtual.podeModificarLinhas()) {
            throw new IllegalStateException("Não é possível modificar as linhas de uma encomenda já bloqueada/assinada.");
        }
        this.linhas.add(linha);
    }

    /**
     * Obtém o estado atual.
     * 
     * @return o estado atual
     */
    public IEstadoEncomenda getEstadoAtual() {
        return estadoAtual;
    }

    /**
     * Define o estado atual.
     * 
     * @param estadoAtual o estado atual
     */
    public void setEstadoAtual(IEstadoEncomenda estadoAtual) {
        this.estadoAtual = estadoAtual;
    }

    /**
     * Obtém o valor total da encomenda.
     * 
     * @return o valor total
     */
    public double getValorTotal() {
        return linhas.stream().mapToDouble(LinhaEncomenda::getSubTotal).sum();
    }
    
    /**
     * Avança o estado da encomenda.
     */
    public void avancarEstado() {
        this.estadoAtual.avancar(this);
    }
}
