package pt.uminho.taki.ln.fatura;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma Fatura emitida para uma Venda.
 * Segue os requisitos de conformidade SAF-T (PT).
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class Fatura {
    private String idFatura;
    private String numFatura;
    private LocalDateTime dataEmissao;
    private String nifCliente;
    private String idVenda;
    private String hash;
    private String hashControl;

    /**
     * Construtor por omissão que gera um novo ID e define a data de emissão atual.
     */
    public Fatura() {
        this.idFatura = UUID.randomUUID().toString();
        this.dataEmissao = LocalDateTime.now();
        this.hashControl = "1"; // Valor padrão para SAF-T (PT)
    }

    /**
     * Construtor com os campos principais da fatura.
     *
     * @param numFatura o número da fatura
     * @param nifCliente o NIF do cliente
     * @param idVenda o identificador da venda associada
     */
    public Fatura(String numFatura, String nifCliente, String idVenda) {
        this.idFatura = UUID.randomUUID().toString();
        this.numFatura = numFatura;
        this.dataEmissao = LocalDateTime.now();
        this.nifCliente = nifCliente;
        this.idVenda = idVenda;
        this.hashControl = "1";
    }

    /**
     * Construtor de cópia para a classe Fatura.
     *
     * @param f a fatura a copiar
     */
    public Fatura(Fatura f) {
        this.idFatura = f.idFatura;
        this.numFatura = f.numFatura;
        this.dataEmissao = f.dataEmissao;
        this.nifCliente = f.nifCliente;
        this.idVenda = f.idVenda;
        this.hash = f.hash;
        this.hashControl = f.hashControl;
    }

    // Getters e Setters
    /**
     * Obtém o identificador interno da fatura.
     *
     * @return o identificador da fatura
     */
    public String getIdFatura() { return idFatura; }
    /**
     * Define o identificador interno da fatura.
     *
     * @param idFatura o identificador da fatura
     */
    public void setIdFatura(String idFatura) { this.idFatura = idFatura; }

    /**
     * Obtém o número sequencial/fiscal da fatura.
     *
     * @return o número da fatura
     */
    public String getNumFatura() { return numFatura; }
    /**
     * Define o número sequencial/fiscal da fatura.
     *
     * @param numFatura o número da fatura
     */
    public void setNumFatura(String numFatura) { this.numFatura = numFatura; }

    /**
     * Obtém a data e hora de emissão da fatura.
     *
     * @return a data de emissão
     */
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    /**
     * Define a data e hora de emissão da fatura.
     *
     * @param dataEmissao a data de emissão
     */
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    /**
     * Obtém o NIF do cliente associado à fatura.
     *
     * @return o NIF do cliente
     */
    public String getNifCliente() { return nifCliente; }
    /**
     * Define o NIF do cliente associado à fatura.
     *
     * @param nifCliente o NIF do cliente
     */
    public void setNifCliente(String nifCliente) { this.nifCliente = nifCliente; }

    /**
     * Obtém o identificador da venda que originou a fatura.
     *
     * @return o identificador da venda
     */
    public String getIdVenda() { return idVenda; }
    /**
     * Define o identificador da venda que originou a fatura.
     *
     * @param idVenda o identificador da venda
     */
    public void setIdVenda(String idVenda) { this.idVenda = idVenda; }

    /**
     * Obtém a assinatura digital (hash) da fatura.
     *
     * @return a hash da fatura
     */
    public String getHash() { return hash; }
    /**
     * Define a assinatura digital (hash) da fatura.
     *
     * @param hash a hash da fatura
     */
    public void setHash(String hash) { this.hash = hash; }

    /**
     * Obtém o identificador da chave de controlo da hash.
     *
     * @return a versão da hash control
     */
    public String getHashControl() { return hashControl; }
    /**
     * Define o identificador da chave de controlo da hash.
     *
     * @param hashControl a versão da hash control
     */
    public void setHashControl(String hashControl) { this.hashControl = hashControl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fatura fatura = (Fatura) o;
        return Objects.equals(idFatura, fatura.idFatura) &&
               Objects.equals(numFatura, fatura.numFatura) &&
               Objects.equals(dataEmissao, fatura.dataEmissao) &&
               Objects.equals(nifCliente, fatura.nifCliente) &&
               Objects.equals(idVenda, fatura.idVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFatura, numFatura, dataEmissao, nifCliente, idVenda);
    }

    @Override
    public Fatura clone() {
        return new Fatura(this);
    }
}
