package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Ingrediente;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a restaurant in the chain.
 * Each restaurant has work positions, ingredient stock, and performance indicators.
 */
public class Restaurante {
    private int id;
    private String nome;
    private String localizacao;
    private List<Posto> postos;
    private Map<Ingrediente, Integer> stock;
    private List<IndicadorDesempenho> indicadores;

    /**
     * Default constructor. Creates a restaurant with empty values.
     */
    public Restaurante() {
        this.id = 0;
        this.nome = "";
        this.localizacao = "";
        this.postos = new ArrayList<>();
        this.stock = new HashMap<>();
        this.indicadores = new ArrayList<>();
    }

    /**
     * Parameterized constructor.
     * @param id Restaurant ID
     * @param nome Restaurant name
     * @param localizacao Restaurant location
     * @param postos List of work positions
     * @param stock Map of ingredient stock quantities
     * @param indicadores List of performance indicators
     */
    public Restaurante(int id, String nome, String localizacao, List<Posto> postos, Map<Ingrediente, Integer> stock, List<IndicadorDesempenho> indicadores) {
        this.id = id;
        this.nome = nome;
        this.localizacao = localizacao;
        this.postos = new ArrayList<>();
        for (Posto p : postos) {
            this.postos.add(p.clone());
        }
        this.stock = new HashMap<>();
        for (Map.Entry<Ingrediente, Integer> entry : stock.entrySet()) {
            this.stock.put(entry.getKey().clone(), entry.getValue());
        }
        this.indicadores = new ArrayList<>();
        for (IndicadorDesempenho i : indicadores)
            this.indicadores.add(i.clone());
    }

    /**
     * Copy constructor.
     * @param r Restaurant to copy
     */
    public Restaurante(Restaurante r) {
        this.id = r.getId();
        this.nome = r.getNome();
        this.localizacao = r.getLocalizacao();
        this.postos = new ArrayList<>();
        for (Posto p : r.getPostos()) {
            this.postos.add(p.clone());
        }
        this.stock = new HashMap<>();
        for (Map.Entry<Ingrediente, Integer> entry : r.getStock().entrySet()) {
            this.stock.put(entry.getKey().clone(), entry.getValue());
        }
        this.indicadores = new ArrayList<>();
        for (IndicadorDesempenho i : r.getIndicadoresDesempenho())
            this.indicadores.add(i.clone());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public List<Posto> getPostos() {
        List<Posto> result = new ArrayList<>();
        for (Posto p : postos) {
            result.add(p.clone());
        }
        return result;
    }

    public void setPostos(List<Posto> postos) {
        this.postos = new ArrayList<>();
        for (Posto p : postos) {
            this.postos.add(p.clone());
        }
    }

    public Map<Ingrediente, Integer> getStock() {
        Map<Ingrediente, Integer> result = new HashMap<>();
        for (Map.Entry<Ingrediente, Integer> entry : stock.entrySet()) {
            result.put(entry.getKey().clone(), entry.getValue());
        }
        return result;
    }

    public void setStock(Map<Ingrediente, Integer> stock) {
        this.stock = new HashMap<>();
        for (Map.Entry<Ingrediente, Integer> entry : stock.entrySet()) {
            this.stock.put(entry.getKey().clone(), entry.getValue());
        }
    }

    public List<IndicadorDesempenho> getIndicadoresDesempenho() {
        List<IndicadorDesempenho> result = new ArrayList<>();
        for (IndicadorDesempenho i : indicadores) {
            result.add(i.clone());
        }
        return result;
    }

    public void setIndicadoresDesempenho(List<IndicadorDesempenho> indicadores) {
        this.indicadores = new ArrayList<>();
        for (IndicadorDesempenho i : indicadores) {
            this.indicadores.add(i.clone());
        }
    }

    /**
     * Generates a report of performance indicators within a date range.
     * @param dataInicio Start date
     * @param dataFim End date
     * @return Formatted string with performance indicators
     */
    public String consultarIndicadoresRestaurante(LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sb = new StringBuilder();
        boolean encontrou = false;

        for (IndicadorDesempenho i : indicadores) {
            if (i.indicadorValido(dataInicio, dataFim)) {
                if (!encontrou) {
                    sb.append("Restaurante: ").append(nome).append("\n");
                    encontrou = true;
                }
                sb.append(i).append("\n");
            }
        }

        if (!encontrou) {
            sb.append("Restaurante: ").append(nome).append(" - Nenhum indicador no período\n");
        }

        return sb.toString();
    }

    /**
     * Sends a message to specific work positions.
     * @param mensagem Message content
     * @param postos List of position types to receive the message
     */
    public void enviarMensagem(String mensagem, List<String> postos) {
        for (Posto p : this.postos) {
            if (postos.contains(p.getTipo()))
                p.adicionarMensagem(mensagem);
        }
    }

    /**
     * Gets the position type of an employee.
     * @param idFuncionario Employee ID
     * @return Position type, or null if employee not found
     */
    public String getPostoFuncionario(Integer idFuncionario) {
        for (Posto p : this.postos)
            for (Funcionario func : p.getFuncionarios())
                if (func.getId() == idFuncionario)
                    return p.getTipo();
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Restaurante that = (Restaurante) o;
        return id == that.id &&
                Objects.equals(nome, that.nome) &&
                Objects.equals(localizacao, that.localizacao) &&
                Objects.equals(postos, that.postos) &&
                Objects.equals(stock, that.stock) &&
                Objects.equals(indicadores, that.indicadores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, localizacao, postos, stock, indicadores);
    }

    @Override
    public String toString() {
        return "Restaurante{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", localizacao='" + localizacao + '\'' +
                ", postos=" + postos.size() +
                ", stock=" + stock.size() +
                ", indicadores" + indicadores +
                '}';
    }

    @Override
    public Restaurante clone() {
        return new Restaurante(this);
    }
}