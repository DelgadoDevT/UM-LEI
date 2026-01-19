package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.IndicadorDesempenho;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class IndicadorDAO implements Map<Integer, List<IndicadorDesempenho>> {
    private static IndicadorDAO instance = null;

    private IndicadorDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS indicador (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "data DATE NOT NULL," +
                    "faturacaoTotal DOUBLE DEFAULT 0," +
                    "totalPedidos INT DEFAULT 0," +
                    "tempoMedioPreparacao INT DEFAULT 0," +
                    "id_restaurante INT NOT NULL," +
                    "FOREIGN KEY (id_restaurante) REFERENCES restaurante(id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static synchronized IndicadorDAO getInstance() {
        if (instance == null) {
            instance = new IndicadorDAO();
        }
        return instance;
    }

    @Override
    public List<IndicadorDesempenho> get(Object key) {
        if (!(key instanceof Integer)) return null;
        int idRestaurante = (Integer) key;
        List<IndicadorDesempenho> indicadores = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement(
                     "SELECT * FROM indicador WHERE id_restaurante = ? ORDER BY data")) {

            pstm.setInt(1, idRestaurante);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("data");
                LocalDate localDate = sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now();

                IndicadorDesempenho indicador = new IndicadorDesempenho(
                        localDate,
                        rs.getDouble("faturacaoTotal"),
                        rs.getInt("totalPedidos"),
                        rs.getInt("tempoMedioPreparacao")
                );
                indicadores.add(indicador);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return indicadores;
    }

    @Override public List<IndicadorDesempenho> put(Integer key, List<IndicadorDesempenho> value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public int size() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public List<IndicadorDesempenho> remove(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void putAll(Map<? extends Integer, ? extends List<IndicadorDesempenho>> m) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void clear() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Collection<List<IndicadorDesempenho>> values() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Entry<Integer, List<IndicadorDesempenho>>> entrySet() { throw new UnsupportedOperationException("Não implementado"); }
}