package pt.uminho.taki.ln.inventario;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que regista um movimento fisico de stock.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MovimentoInventario {
    private String id;
    private TipoMovimento tipo;
    private double quantidade;
    private LocalDateTime dataRegisto;
    private String motivo;
    private String idInventario;
    private String idFuncionario;

    /**
     * Construtor vazio para a classe MovimentoInventario.
     */
    public MovimentoInventario() {
        this.id = "";
        this.tipo = null;
        this.quantidade = 0.0;
        this.dataRegisto = LocalDateTime.now();
        this.motivo = "";
        this.idInventario = "";
        this.idFuncionario = "";
    }

    /**
     * Construtor completo para a classe MovimentoInventario.
     * @param id o id do movimento
     * @param tipo o tipo de movimento (Entrada, Saida, Quebra)
     * @param quantidade o valor do movimento
     * @param dataRegisto a data do movimento
     * @param motivo o motivo do movimento manual
     * @param idInventario o identificador do stock afetado
     * @param idFuncionario o identificador do funcionario que efetuou o registo
     */
    public MovimentoInventario(String id, TipoMovimento tipo, double quantidade, LocalDateTime dataRegisto, String motivo, String idInventario, String idFuncionario) {
        this.id = id;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataRegisto = dataRegisto;
        this.motivo = motivo;
        this.idInventario = idInventario;
        this.idFuncionario = idFuncionario;
    }

    /**
     * Construtor de cópia para a classe MovimentoInventario.
     * @param m o movimento a copiar
     */
    public MovimentoInventario(MovimentoInventario m) {
        this.id = m.getId();
        this.tipo = m.getTipo();
        this.quantidade = m.getQuantidade();
        this.dataRegisto = m.getDataRegisto();
        this.motivo = m.getMotivo();
        this.idInventario = m.getIdInventario();
        this.idFuncionario = m.getIdFuncionario();
    }

    /**
     * Obtém o identificador do movimento.
     * @return o identificador
     */
    public String getId() { return id; }
    /**
     * Define o identificador do movimento.
     * @param id o novo identificador
     */
    public void setId(String id) { this.id = id; }
    /**
     * Obtém o tipo de movimento.
     * @return o tipo de movimento
     */
    public TipoMovimento getTipo() { return tipo; }
    /**
     * Define o tipo de movimento.
     * @param tipo o novo tipo
     */
    public void setTipo(TipoMovimento tipo) { this.tipo = tipo; }
    /**
     * Obtém a quantidade movimentada.
     * @return a quantidade
     */
    public double getQuantidade() { return quantidade; }
    /**
     * Define a quantidade movimentada.
     * @param quantidade a nova quantidade
     */
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    /**
     * Obtém a data e hora do registo do movimento.
     * @return a data de registo
     */
    public LocalDateTime getDataRegisto() { return dataRegisto; }
    /**
     * Define a data e hora do registo do movimento.
     * @param dataRegisto a nova data de registo
     */
    public void setDataRegisto(LocalDateTime dataRegisto) { this.dataRegisto = dataRegisto; }
    /**
     * Obtém o motivo do movimento manual.
     * @return o motivo
     */
    public String getMotivo() { return motivo; }
    /**
     * Define o motivo do movimento manual.
     * @param motivo o novo motivo
     */
    public void setMotivo(String motivo) { this.motivo = motivo; }
    /**
     * Obtém o identificador do inventário afetado.
     * @return o identificador do inventário
     */
    public String getIdInventario() { return idInventario; }
    /**
     * Define o identificador do inventário afetado.
     * @param idInventario o novo identificador do inventário
     */
    public void setIdInventario(String idInventario) { this.idInventario = idInventario; }
    /**
     * Obtém o identificador do funcionário que realizou o movimento.
     * @return o identificador do funcionário
     */
    public String getIdFuncionario() { return idFuncionario; }
    /**
     * Define o identificador do funcionário que realizou o movimento.
     * @param idFuncionario o novo identificador do funcionário
     */
    public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimentoInventario that = (MovimentoInventario) o;
        return Double.compare(that.quantidade, quantidade) == 0 &&
               Objects.equals(id, that.id) &&
               tipo == that.tipo &&
               Objects.equals(dataRegisto, that.dataRegisto) &&
               Objects.equals(motivo, that.motivo) &&
               Objects.equals(idInventario, that.idInventario) &&
               Objects.equals(idFuncionario, that.idFuncionario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tipo, quantidade, dataRegisto, motivo, idInventario, idFuncionario);
    }

    @Override
    public String toString() {
        return "MovimentoInventario{" +
               "id='" + id + '\'' +
               ", tipo=" + tipo +
               ", quantidade=" + quantidade +
               ", dataRegisto=" + dataRegisto +
               ", idInventario='" + idInventario + '\'' +
               '}';
    }

    @Override
    public MovimentoInventario clone() {
        return new MovimentoInventario(this);
    }
}
