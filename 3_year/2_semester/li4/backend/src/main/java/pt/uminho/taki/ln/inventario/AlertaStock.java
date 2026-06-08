package pt.uminho.taki.ln.inventario;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um alerta de stock critico.
 * Mapeia os dados provenientes da View_Alertas_StockCritico.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class AlertaStock {
    private int idLoja;
    private String codigoBarras;
    private String nomeProduto;
    private double quantidadeAtual;
    private double limiteMinimo;
    private LocalDateTime dataAlerta;
    private String mensagem;

    /**
     * Construtor vazio para a classe AlertaStock.
     */
    public AlertaStock() {
        this.idLoja = 0;
        this.codigoBarras = "";
        this.nomeProduto = "";
        this.quantidadeAtual = 0.0;
        this.limiteMinimo = 0.0;
        this.dataAlerta = null;
        this.mensagem = "";
    }

    /**
     * Construtor completo para a classe AlertaStock.
     * @param idLoja o identificador da loja
     * @param codigoBarras o codigo EAN do produto
     * @param nomeProduto o nome do artigo
     * @param quantidadeAtual o stock presente
     * @param limiteMinimo o limiar de seguranca
     * @param dataAlerta a data/hora da deteccao
     * @param mensagem a descricao amigavel do alerta
     */
    public AlertaStock(int idLoja, String codigoBarras, String nomeProduto, double quantidadeAtual, double limiteMinimo, LocalDateTime dataAlerta, String mensagem) {
        this.idLoja = idLoja;
        this.codigoBarras = codigoBarras;
        this.nomeProduto = nomeProduto;
        this.quantidadeAtual = quantidadeAtual;
        this.limiteMinimo = limiteMinimo;
        this.dataAlerta = dataAlerta;
        this.mensagem = mensagem;
    }

    /**
     * Construtor de copia para a classe AlertaStock.
     * @param a o alerta a copiar
     */
    public AlertaStock(AlertaStock a) {
        this.idLoja = a.getIdLoja();
        this.codigoBarras = a.getCodigoBarras();
        this.nomeProduto = a.getNomeProduto();
        this.quantidadeAtual = a.getQuantidadeAtual();
        this.limiteMinimo = a.getLimiteMinimo();
        this.dataAlerta = a.getDataAlerta();
        this.mensagem = a.getMensagem();
    }

    /**
     * Obtém o identificador da loja.
     * @return o identificador
     */
    public int getIdLoja() { return idLoja; }
    /**
     * Define o identificador da loja.
     * @param idLoja o novo identificador
     */
    public void setIdLoja(int idLoja) { this.idLoja = idLoja; }
    /**
     * Obtém o código de barras do produto.
     * @return o código de barras
     */
    public String getCodigoBarras() { return codigoBarras; }
    /**
     * Define o código de barras do produto.
     * @param codigoBarras o novo código de barras
     */
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    /**
     * Obtém o nome do produto.
     * @return o nome do produto
     */
    public String getNomeProduto() { return nomeProduto; }
    /**
     * Define o nome do produto.
     * @param nomeProduto o novo nome
     */
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    /**
     * Obtém a quantidade atual em stock.
     * @return a quantidade atual
     */
    public double getQuantidadeAtual() { return quantidadeAtual; }
    /**
     * Define a quantidade atual em stock.
     * @param quantidadeAtual a nova quantidade
     */
    public void setQuantidadeAtual(double quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }
    /**
     * Obtém o limite mínimo de stock.
     * @return o limite mínimo
     */
    public double getLimiteMinimo() { return limiteMinimo; }
    /**
     * Define o limite mínimo de stock.
     * @param limiteMinimo o novo limite
     */
    public void setLimiteMinimo(double limiteMinimo) { this.limiteMinimo = limiteMinimo; }
    /**
     * Obtém a data e hora em que o alerta foi gerado.
     * @return a data do alerta
     */
    public LocalDateTime getDataAlerta() { return dataAlerta; }
    /**
     * Define a data e hora do alerta.
     * @param dataAlerta a nova data
     */
    public void setDataAlerta(LocalDateTime dataAlerta) { this.dataAlerta = dataAlerta; }
    /**
     * Obtém a mensagem descritiva do alerta.
     * @return a mensagem
     */
    public String getMensagem() { return mensagem; }
    /**
     * Define a mensagem descritiva do alerta.
     * @param mensagem a nova mensagem
     */
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertaStock that = (AlertaStock) o;
        return idLoja == that.idLoja &&
               Double.compare(that.quantidadeAtual, quantidadeAtual) == 0 &&
               Double.compare(that.limiteMinimo, limiteMinimo) == 0 &&
               Objects.equals(codigoBarras, that.codigoBarras) &&
               Objects.equals(nomeProduto, that.nomeProduto) &&
               Objects.equals(dataAlerta, that.dataAlerta) &&
               Objects.equals(mensagem, that.mensagem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLoja, codigoBarras, nomeProduto, quantidadeAtual, limiteMinimo, dataAlerta, mensagem);
    }

    @Override
    public String toString() {
        return "AlertaStock{" +
               "loja=" + idLoja +
               ", produto='" + nomeProduto + '\'' +
               ", stock=" + quantidadeAtual +
               ", limite=" + limiteMinimo +
               '}';
    }

    @Override
    public AlertaStock clone() {
        return new AlertaStock(this);
    }
}
