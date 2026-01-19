package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Ingrediente;

import java.sql.*;
import java.util.*;

public class IngredienteDAO implements Map<String, Ingrediente> {
    private static IngredienteDAO instance = null;

    private IngredienteDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS ingrediente (" +
                    "nome VARCHAR(50) NOT NULL PRIMARY KEY," +
                    "preco DOUBLE NOT NULL)";
            stm.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static synchronized IngredienteDAO getInstance() {
        if (instance == null) {
            instance = new IngredienteDAO();
        }
        return instance;
    }

    @Override
    public Ingrediente get(Object key) {
        if (!(key instanceof String)) return null;
        String nome = (String) key;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement("SELECT * FROM ingrediente WHERE nome = ?")) {

            pstm.setString(1, nome);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                double preco = rs.getDouble("preco");
                Set<String> alergenios = new HashSet<>();
                return new Ingrediente(nome, preco, alergenios);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Ingrediente put(String key, Ingrediente value) {
        Ingrediente ingredienteExistente = get(key);

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            if (ingredienteExistente == null) {
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO ingrediente (nome, preco) VALUES (?, ?)");
                pstm.setString(1, key);
                pstm.setDouble(2, value.getPreco());
                pstm.executeUpdate();
            } else {
                PreparedStatement pstm = conn.prepareStatement(
                        "UPDATE ingrediente SET preco = ? WHERE nome = ?");
                pstm.setDouble(1, value.getPreco());
                pstm.setString(2, key);
                pstm.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return ingredienteExistente;
    }

    @Override
    public Collection<Ingrediente> values() {
        List<Ingrediente> ingredientes = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM ingrediente ORDER BY nome")) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                Set<String> alergenios = new HashSet<>();
                ingredientes.add(new Ingrediente(nome, preco, alergenios));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return ingredientes;
    }

    @Override public int size() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Ingrediente remove(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void putAll(Map<? extends String, ? extends Ingrediente> m) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void clear() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<String> keySet() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Entry<String, Ingrediente>> entrySet() { throw new UnsupportedOperationException("Não implementado"); }
}