package pt.uminho.taki.ln.vendas;

import java.util.Objects;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.uminho.taki.ln.fatura.Fatura;

/**
 * Representa uma venda.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class Venda {
    private String idVenda;
    private LocalDateTime dataHora;
    private double subtotal;
    private double imposto;
    private double total;
    private String estado;
    private int idLoja;
    private String idFuncionario;
    private Fatura fatura;
    private List<LinhaVenda> linhas;

    /**
     * Constrói uma nova Venda vazia.
     */
    public Venda() {
        this.idVenda = "";
        this.dataHora = LocalDateTime.now();
        this.subtotal = 0.0;
        this.imposto = 0.0;
        this.total = 0.0;
        this.estado = "Concluída";
        this.linhas = new ArrayList<>();
    }

    /**
     * Constrói uma nova Venda a partir de uma existente.
     * 
     * @param v a venda existente a copiar
     */
    public Venda(Venda v) {
        this.idVenda = v.idVenda;
        this.dataHora = v.dataHora;
        this.subtotal = v.subtotal;
        this.imposto = v.imposto;
        this.total = v.total;
        this.estado = v.estado;
        this.idLoja = v.idLoja;
        this.idFuncionario = v.idFuncionario;
        this.fatura = v.fatura != null ? v.fatura.clone() : null;
        this.linhas = v.linhas != null ? v.linhas.stream().map(LinhaVenda::clone).collect(Collectors.toList()) : new ArrayList<>();
    }

    /**
     * Adiciona uma linha de venda a esta venda.
     * 
     * @param linhaVenda a linha de venda a adicionar
     */
    public void adicionarLinhaVenda(LinhaVenda linhaVenda) {
        if (this.linhas == null) {
            this.linhas = new ArrayList<>();
        }
        this.linhas.add(linhaVenda);
        recalcularVenda();
    }

    /**
     * Recalcula os totais da venda.
     */
    public void recalcularVenda() {
        if (this.linhas == null || this.linhas.isEmpty()) {
            this.subtotal = 0.0;
            this.imposto = 0.0;
            this.total = 0.0;
            return;
        }

        this.subtotal = this.linhas.stream().mapToDouble(LinhaVenda::getSubtotal).sum();
        this.imposto = this.linhas.stream().mapToDouble(LinhaVenda::getTotalImposto).sum();
        this.total = this.linhas.stream().mapToDouble(LinhaVenda::getTotalFinal).sum();
    }

    /**
     * Obtém o identificador da venda.
     * 
     * @return o identificador da venda
     */
    public String getIdVenda() { return idVenda; }
    /**
     * Define o identificador da venda.
     * 
     * @param idVenda o identificador da venda
     */
    public void setIdVenda(String idVenda) { this.idVenda = idVenda; }
    /**
     * Obtém a data e hora.
     * 
     * @return a data e hora
     */
    public LocalDateTime getDataHora() { return dataHora; }
    /**
     * Define a data e hora.
     * 
     * @param dataHora a data e hora
     */
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    /**
     * Obtém o subtotal.
     * 
     * @return o subtotal
     */
    public double getSubtotal() { return subtotal; }
    /**
     * Define o subtotal.
     * 
     * @param subtotal o subtotal
     */
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    /**
     * Obtém o imposto.
     * 
     * @return o imposto
     */
    public double getImposto() { return imposto; }
    /**
     * Define o imposto.
     * 
     * @param imposto o imposto
     */
    public void setImposto(double imposto) { this.imposto = imposto; }
    /**
     * Obtém o total.
     * 
     * @return o total
     */
    public double getTotal() { return total; }
    /**
     * Define o total.
     * 
     * @param total o total
     */
    public void setTotal(double total) { this.total = total; }
    /**
     * Obtém o estado.
     * 
     * @return o estado
     */
    public String getEstado() { return estado; }
    /**
     * Define o estado.
     * 
     * @param estado o estado
     */
    public void setEstado(String estado) { this.estado = estado; }
    /**
     * Obtém o identificador da loja.
     * 
     * @return o identificador da loja
     */
    public int getIdLoja() { return idLoja; }
    /**
     * Define o identificador da loja.
     * 
     * @param idLoja o identificador da loja
     */
    public void setIdLoja(int idLoja) { this.idLoja = idLoja; }
    /**
     * Obtém o identificador do funcionário.
     * 
     * @return o identificador do funcionário
     */
    public String getIdFuncionario() { return idFuncionario; }
    /**
     * Define o identificador do funcionário.
     * 
     * @param idFuncionario o identificador do funcionário
     */
    public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }
    /**
     * Obtém a fatura.
     * 
     * @return a fatura
     */
    public Fatura getFatura() { return fatura; }
    /**
     * Define a fatura.
     * 
     * @param fatura a fatura
     */
    public void setFatura(Fatura fatura) { this.fatura = fatura; }
    /**
     * Obtém as linhas de venda.
     * 
     * @return as linhas de venda
     */
    public List<LinhaVenda> getLinhas() { return linhas; }
    /**
     * Define as linhas de venda.
     * 
     * @param linhas as linhas de venda
     */
    public void setLinhas(List<LinhaVenda> linhas) { 
        this.linhas = linhas; 
        recalcularVenda();
    }

    /**
     * Compara esta venda com outro objeto.
     * 
     * @param o o outro objeto
     * @return verdadeiro se forem iguais, falso caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venda venda = (Venda) o;
        return Double.compare(venda.subtotal, subtotal) == 0 &&
               Double.compare(venda.imposto, imposto) == 0 &&
               Double.compare(venda.total, total) == 0 &&
               idLoja == venda.idLoja &&
               Objects.equals(idVenda, venda.idVenda) &&
               Objects.equals(dataHora, venda.dataHora) &&
               Objects.equals(estado, venda.estado) &&
               Objects.equals(idFuncionario, venda.idFuncionario) &&
               Objects.equals(fatura, venda.fatura) &&
               Objects.equals(linhas, venda.linhas);
    }

    /**
     * Calcula o código hash desta venda.
     * 
     * @return o código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(idVenda, dataHora, subtotal, imposto, total, estado, idLoja, idFuncionario, fatura, linhas);
    }

    /**
     * Clona esta venda.
     * 
     * @return a venda clonada
     */
    @Override
    public Venda clone() {
        return new Venda(this);
    }
}
