package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Funcionario;
import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Atendente;
import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Cozinheiro;
import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Gestor;

import java.sql.*;
import java.util.*;

public class FuncionarioDAO implements Map<Integer, Funcionario> {
    private static FuncionarioDAO instance = null;

    private FuncionarioDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            String sql1 = "CREATE TABLE IF NOT EXISTS funcionario (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(100) NOT NULL," +
                    "nif INT NOT NULL," +
                    "id_posto INT," +
                    "FOREIGN KEY (id_posto) REFERENCES posto(id))";
            stm.executeUpdate(sql1);

            String sql2 = "CREATE TABLE IF NOT EXISTS atendente (" +
                    "id INT PRIMARY KEY," +
                    "FOREIGN KEY (id) REFERENCES funcionario(id))";
            stm.executeUpdate(sql2);

            String sql3 = "CREATE TABLE IF NOT EXISTS cozinheiro (" +
                    "id INT PRIMARY KEY," +
                    "FOREIGN KEY (id) REFERENCES funcionario(id))";
            stm.executeUpdate(sql3);

            String sql4 = "CREATE TABLE IF NOT EXISTS gestor (" +
                    "id INT PRIMARY KEY," +
                    "FOREIGN KEY (id) REFERENCES funcionario(id))";
            stm.executeUpdate(sql4);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static synchronized FuncionarioDAO getInstance() {
        if (instance == null) {
            instance = new FuncionarioDAO();
        }
        return instance;
    }

    @Override
    public Funcionario get(Object key) {
        if (!(key instanceof Integer)) return null;
        int id = (Integer) key;
        Funcionario funcionario = null;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement(
                     "SELECT f.*, " +
                             "CASE " +
                             "  WHEN a.id IS NOT NULL THEN 'ATENDENTE' " +
                             "  WHEN c.id IS NOT NULL THEN 'COZINHEIRO' " +
                             "  WHEN g.id IS NOT NULL THEN 'GESTOR' " +
                             "  ELSE 'DESCONHECIDO' " +
                             "END as tipo " +
                             "FROM funcionario f " +
                             "LEFT JOIN atendente a ON f.id = a.id " +
                             "LEFT JOIN cozinheiro c ON f.id = c.id " +
                             "LEFT JOIN gestor g ON f.id = g.id " +
                             "WHERE f.id = ?")) {

            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String nome = rs.getString("nome");
                int nif = rs.getInt("nif");
                String tipo = rs.getString("tipo");

                switch (tipo) {
                    case "ATENDENTE":
                        funcionario = new Atendente(id, nome, nif);
                        break;
                    case "COZINHEIRO":
                        funcionario = new Cozinheiro(id, nome, nif);
                        break;
                    case "GESTOR":
                        funcionario = new Gestor(id, nome, nif);
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return funcionario;
    }

    public Integer buscarRestaurante(Integer idFuncionario) {
        Integer idRestaurante = null;
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement(
                     "SELECT p.id_restaurante " +
                             "FROM funcionario f " +
                             "LEFT JOIN posto p ON f.id_posto = p.id " +
                             "WHERE f.id = ?")) {
            pstm.setInt(1, idFuncionario);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                idRestaurante = rs.getInt("id_restaurante");
                if (rs.wasNull()) {
                    idRestaurante = null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return idRestaurante;
    }

    @Override public int size() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Funcionario put(Integer key, Funcionario value) { throw new UnsupportedOperationException("Não implementado");}
    @Override public Funcionario remove(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void putAll(Map<? extends Integer, ? extends Funcionario> m) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void clear() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Collection<Funcionario> values() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Entry<Integer, Funcionario>> entrySet() { throw new UnsupportedOperationException("Não implementado"); }
}