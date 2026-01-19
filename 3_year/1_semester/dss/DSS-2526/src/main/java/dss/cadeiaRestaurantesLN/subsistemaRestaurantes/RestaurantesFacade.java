package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

import dss.cadeiaRestaurantesDL.FuncionarioDAO;
import dss.cadeiaRestaurantesDL.RestauranteDAO;

import java.time.LocalDate;
import java.util.*;

/**
 * Facade for the Restaurant Management subsystem.
 * Handles employee management, messaging between positions, and performance indicators.
 * Implements IGestRestaurantes interface.
 */
public class RestaurantesFacade implements IGestRestaurantes {
    private RestauranteDAO restaurantes;
    private FuncionarioDAO funcionarios;

    /**
     * Default constructor. Initializes DAOs with singleton instances.
     */
    public RestaurantesFacade() {
        this.restaurantes = RestauranteDAO.getInstance();
        this.funcionarios = FuncionarioDAO.getInstance();
    }

    /**
     * Constructor with specific DAO instances.
     * @param restaurantes Restaurant DAO
     * @param funcionarios Employee DAO
     */
    public RestaurantesFacade(RestauranteDAO restaurantes, FuncionarioDAO funcionarios) {
        this.restaurantes = restaurantes;
        this.funcionarios = funcionarios;
    }

    /**
     * Copy constructor.
     * @param rf Facade instance to copy
     */
    public RestaurantesFacade(RestaurantesFacade rf) {
        this.restaurantes = rf.getRestaurantes();
        this.funcionarios = rf.getFuncionarios();
    }

    public RestauranteDAO getRestaurantes() {
        return restaurantes;
    }

    public void setRestaurantes(RestauranteDAO restaurantes) {
        this.restaurantes = restaurantes;
    }

    public FuncionarioDAO getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(FuncionarioDAO funcionarios) {
        this.funcionarios = funcionarios;
    }

    public List<String> consultarIndicadoresDesempenho(LocalDate dataInicio, LocalDate dataFim, Integer funcionarioId, List<Integer> restaurantesId) {
        List<String> indicadores = new ArrayList<>();
        List<Restaurante> res = new ArrayList<>();

        if (isGestor(funcionarioId)) {
            if (isGestorGeral(funcionarioId)) {
                // Gestor geral pode ver indicadores de todos os restaurantes
                for (Integer rId : restaurantesId)
                    res.add(restaurantes.get(rId));
            } else {
                // Gestor normal só pode ver indicadores do restaurante no qual trabalha
                res.add(restaurantes.get(funcionarios.buscarRestaurante(funcionarioId)));
            }

            for (Restaurante r : res) {
                indicadores.add(r.consultarIndicadoresRestaurante(dataInicio, dataFim));
            }
        }

        return indicadores;
    }

    public void enviarMensagem(Integer funcionarioId, String mensagem, List<String> postos, List<Integer> restaurantesId) {
        List<Restaurante> res = new ArrayList<>();

        if (isGestor(funcionarioId)) {
            if (isGestorGeral(funcionarioId)) {
                // Gestor geral pode enviar mensagens para todos os restaurantes
                for (Integer r : restaurantesId)
                    res.add(restaurantes.get(r));
            } else {
                // Gestor normal só pode enviar mensagens para o restaurante no qual trabalha
                res.add(restaurantes.get(funcionarios.buscarRestaurante(funcionarioId)));
            }

            for (Restaurante r : res) {
                r.enviarMensagem(mensagem, postos);
                restaurantes.put(r.getId(), r);
            }
        }
    }

    private boolean isGestor(Integer funcionarioId) {
        Funcionario f = funcionarios.get(funcionarioId);
        return f instanceof Gestor;
    }

    public boolean isGestorGeral(Integer funcionarioId) {
        Integer res = funcionarios.buscarRestaurante(funcionarioId);
        return res == null;
    }

    public boolean existeFuncionario(Integer funcionarioId) {
        return (funcionarios.get(funcionarioId) != null);
    }

    public boolean isFuncionarioRole(Integer funcionarioId, String role) {
        Funcionario f = funcionarios.get(funcionarioId);
        if (f == null) return false;
        return f.getRole().equalsIgnoreCase(role);
    }

    public List<String> getNomesRestaurantes(Integer idFuncionario) {
        List<String> nomes = new ArrayList<>();

        if (isGestorGeral(idFuncionario)) {
            Collection<Restaurante> todos = restaurantes.values();
            for (Restaurante r : todos)
                nomes.add(r.getNome() + ":" + r.getId());
        } else {
            Integer restauranteId = funcionarios.buscarRestaurante(idFuncionario);
            if (restauranteId != null) {
                Restaurante tem = restaurantes.get(restauranteId);
                nomes.add(tem.getNome() + ":" + tem.getId());
            }
        }

        return nomes;
    }

    public Set<String> getTiposPostoValidosParaGestor(Integer idFuncionario) {
        Set<String> postos = new TreeSet<>();
        List<Restaurante> res = new ArrayList<>();

        if (isGestor(idFuncionario)) {
            if (isGestorGeral(idFuncionario)) {
                res.addAll(restaurantes.values());
            } else {
                res.add(restaurantes.get(funcionarios.buscarRestaurante(idFuncionario)));
            }

            for (Restaurante r : res)
                for (Posto p : r.getPostos())
                    postos.add(p.getTipo());
        }

        return postos;
    }

    public List<String> getMensagensPosto(Integer idFuncionario) {
        List<String> mensagensComDetalhes = new ArrayList<>();

        if (isGestor(idFuncionario)) {
            if (isGestorGeral(idFuncionario)) {
                for (Restaurante restaurante : restaurantes.values())
                    for (Posto posto : restaurante.getPostos())
                        for (String mensagem : posto.getMensagens())
                            mensagensComDetalhes.add("[" + posto.getTipo() + " | " + restaurante.getNome() + "] - " + mensagem);
            } else {
                Integer idRestaurante = funcionarios.buscarRestaurante(idFuncionario);
                Restaurante restaurante = restaurantes.get(idRestaurante);
                for (Posto posto : restaurante.getPostos())
                    for (String mensagem : posto.getMensagens())
                        mensagensComDetalhes.add("[" + posto.getTipo() + " | " + restaurante.getNome() + "] - " + mensagem);
            }
        } else if (isFuncionarioRole(idFuncionario, "COZINHEIRO")) {
            Integer idRestaurante = funcionarios.buscarRestaurante(idFuncionario);
            Restaurante restaurante = restaurantes.get(idRestaurante);
            for (Posto posto : restaurante.getPostos())
                if (posto.getTipo().equals("COZINHA"))
                    for (String mensagem : posto.getMensagens())
                        mensagensComDetalhes.add("[" + posto.getTipo() + " | " + restaurante.getNome() + "] - " + mensagem);
        } else if (isFuncionarioRole(idFuncionario, "ATENDENTE")) {
            Integer idRestaurante = funcionarios.buscarRestaurante(idFuncionario);
            Restaurante restaurante = restaurantes.get(idRestaurante);
            for (Posto posto : restaurante.getPostos())
                if (posto.getTipo().equals("BALCAO"))
                    for (String mensagem : posto.getMensagens())
                        mensagensComDetalhes.add("[" + posto.getTipo() + " | " + restaurante.getNome() + "] - " + mensagem);
        }

        return mensagensComDetalhes;
    }

    public List<String> getPerfilFuncionario(Integer idFuncionario) {
        List<String> perfil = new ArrayList<>();
        Funcionario f = funcionarios.get(idFuncionario);

        if (f != null) {
            // Divide o toString() do funcionário em linhas separadas
            String[] linhasFuncionario = f.toString().split("\n");
            for (String linha : linhasFuncionario) {
                perfil.add(linha);
            }

            Integer idRestaurante = funcionarios.buscarRestaurante(idFuncionario);
            if (idRestaurante != null) {
                Restaurante r = restaurantes.get(idRestaurante);
                perfil.add("Restaurante: " + r.getNome() + " (ID: " + r.getId() + ")");
                perfil.add("Posto: " + r.getPostoFuncionario(idFuncionario));
            } else {
                perfil.add("Restaurante: Gestor Geral");
            }
        }

        return perfil;
    }

    public Integer getRestauranteIdPorFuncionario(Integer idFuncionario) {
        return funcionarios.buscarRestaurante(idFuncionario);
    }

    @Override
    public List<String> getNomesTodosRestaurantes() {
        List<String> res = new java.util.ArrayList<>();
        // Usa o DAO para buscar todos os restaurantes
        for (Restaurante r : dss.cadeiaRestaurantesDL.RestauranteDAO.getInstance().values()) {
            res.add("Restaurante: " + r.getId() + " - " + r.getNome() + " (" + r.getLocalizacao() + ")");
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantesFacade that = (RestaurantesFacade) o;
        return Objects.equals(restaurantes, that.restaurantes) && Objects.equals(funcionarios, that.funcionarios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantes, funcionarios);
    }

    @Override
    public String toString() {
        return "RestaurantesFacade{" +
                "restaurantes=" + restaurantes +
                ", funcionarios=" + funcionarios +
                '}';
    }

    @Override
    public RestaurantesFacade clone() {
        return new RestaurantesFacade(this);
    }
}
