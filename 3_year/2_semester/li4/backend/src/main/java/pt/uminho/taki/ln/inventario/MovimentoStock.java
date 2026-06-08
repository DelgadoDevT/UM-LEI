package pt.uminho.taki.ln.inventario;

import java.util.Objects;
import java.time.LocalDateTime;

/**
 * Representa um movimento de stock no inventário.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MovimentoStock {
    private String idMovimento;
    private String idProduto;
    private String tipo; // Entrada, Saida, Acerto
    private int quantidade;
    private LocalDateTime dataRegisto;

    /**
     * Construtor por omissão para MovimentoStock.
     */
    public MovimentoStock() {
        this.idMovimento = "";
        this.idProduto = "";
        this.tipo = "";
        this.quantidade = 0;
        this.dataRegisto = LocalDateTime.now();
    }

    /**
     * Construtor parametrizado para MovimentoStock.
     *
     * @param idMovimento o identificador do movimento.
     * @param idProduto o identificador do produto.
     * @param tipo o tipo do movimento (por exemplo, Entrada, Saída, Acerto).
     * @param quantidade a quantidade do produto movimentada.
     * @param dataRegisto a data e hora em que o movimento foi registado.
     */
    public MovimentoStock(String idMovimento, String idProduto, String tipo, int quantidade, LocalDateTime dataRegisto) {
        this.idMovimento = idMovimento;
        this.idProduto = idProduto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataRegisto = dataRegisto;
    }

    /**
     * Construtor de cópia para MovimentoStock.
     *
     * @param m o objeto MovimentoStock a copiar.
     */
    public MovimentoStock(MovimentoStock m) {
        this.idMovimento = m.getIdMovimento();
        this.idProduto = m.getIdProduto();
        this.tipo = m.getTipo();
        this.quantidade = m.getQuantidade();
        this.dataRegisto = m.getDataRegisto();
    }

    /**
     * Obtém o identificador do movimento.
     *
     * @return o identificador do movimento.
     */
    public String getIdMovimento() { return idMovimento; }

    /**
     * Define o identificador do movimento.
     *
     * @param idMovimento o identificador do movimento.
     */
    public void setIdMovimento(String idMovimento) { this.idMovimento = idMovimento; }

    /**
     * Obtém o identificador do produto.
     *
     * @return o identificador do produto.
     */
    public String getIdProduto() { return idProduto; }

    /**
     * Define o identificador do produto.
     *
     * @param idProduto o identificador do produto.
     */
    public void setIdProduto(String idProduto) { this.idProduto = idProduto; }

    /**
     * Obtém o tipo do movimento.
     *
     * @return o tipo do movimento.
     */
    public String getTipo() { return tipo; }

    /**
     * Define o tipo do movimento.
     *
     * @param tipo o tipo do movimento.
     */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /**
     * Obtém a quantidade do produto movimentada.
     *
     * @return a quantidade do produto movimentada.
     */
    public int getQuantidade() { return quantidade; }

    /**
     * Define a quantidade do produto movimentada.
     *
     * @param quantidade a quantidade do produto movimentada.
     */
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    /**
     * Obtém a data e hora em que o movimento foi registado.
     *
     * @return a data e hora em que o movimento foi registado.
     */
    public LocalDateTime getDataRegisto() { return dataRegisto; }

    /**
     * Define a data e hora em que o movimento foi registado.
     *
     * @param dataRegisto a data e hora em que o movimento foi registado.
     */
    public void setDataRegisto(LocalDateTime dataRegisto) { this.dataRegisto = dataRegisto; }

    /**
     * Indica se algum outro objeto é "igual a" este.
     *
     * @param o o objeto de referência para comparação.
     * @return verdadeiro se este objeto for o mesmo que o argumento; falso caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimentoStock that = (MovimentoStock) o;
        return quantidade == that.quantidade &&
               Objects.equals(idMovimento, that.idMovimento) &&
               Objects.equals(idProduto, that.idProduto) &&
               Objects.equals(tipo, that.tipo) &&
               Objects.equals(dataRegisto, that.dataRegisto);
    }

    /**
     * Retorna uma representação em cadeia de caracteres do objeto.
     *
     * @return uma representação em cadeia de caracteres do objeto.
     */
    @Override
    public String toString() {
        return "MovimentoStock{" +
               "idMovimento='" + idMovimento + "'" +
               ", idProduto='" + idProduto + "'" +
               ", tipo='" + tipo + "'" +
               ", quantidade=" + quantidade +
               ", dataRegisto=" + dataRegisto +
               "}";
    }

    /**
     * Cria e retorna uma cópia deste objeto.
     *
     * @return um clone desta instância.
     */
    @Override
    public MovimentoStock clone() {
        return new MovimentoStock(this);
    }
}
