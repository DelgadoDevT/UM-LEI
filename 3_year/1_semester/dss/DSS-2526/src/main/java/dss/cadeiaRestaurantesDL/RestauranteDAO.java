package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaRestaurantes.*;
import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Ingrediente;

import java.sql.*;
import java.util.*;

public class RestauranteDAO implements Map<Integer, Restaurante> {
    private static RestauranteDAO instance = null;

    private RestauranteDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            String sql1 = "CREATE TABLE IF NOT EXISTS restaurante (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(100) NOT NULL," +
                    "localizacao VARCHAR(200))";
            stm.executeUpdate(sql1);

            String sql2 = "CREATE TABLE IF NOT EXISTS posto (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "tipo VARCHAR(50) NOT NULL," +
                    "id_restaurante INT," +
                    "FOREIGN KEY (id_restaurante) REFERENCES restaurante(id))";
            stm.executeUpdate(sql2);

            String sql3 = "CREATE TABLE IF NOT EXISTS mensagem (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "id_posto INT NOT NULL," +
                    "texto TEXT NOT NULL," +
                    "data_hora TIMESTAMP DEFAULT NOW()," +
                    "FOREIGN KEY (id_posto) REFERENCES posto(id))";
            stm.executeUpdate(sql3);

            String sql4 = "CREATE TABLE IF NOT EXISTS stock (" +
                    "id_restaurante INT," +
                    "nome_ingrediente VARCHAR(50)," +
                    "quantidade INT NOT NULL," +
                    "PRIMARY KEY (id_restaurante, nome_ingrediente)," +
                    "FOREIGN KEY (id_restaurante) REFERENCES restaurante(id)," +
                    "FOREIGN KEY (nome_ingrediente) REFERENCES ingrediente(nome))";
            stm.executeUpdate(sql4);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static synchronized RestauranteDAO getInstance() {
        if (instance == null) {
            instance = new RestauranteDAO();
        }
        return instance;
    }

    @Override
    public Restaurante get(Object key) {
        if (!(key instanceof Integer)) return null;
        int idRestaurante = (Integer) key;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            PreparedStatement pstmRestaurante = conn.prepareStatement("SELECT * FROM restaurante WHERE id = ?");
            pstmRestaurante.setInt(1, idRestaurante);
            ResultSet rsRestaurante = pstmRestaurante.executeQuery();

            if (rsRestaurante.next()) {
                String nome = rsRestaurante.getString("nome");
                String localizacao = rsRestaurante.getString("localizacao");
                List<Posto> postos = new ArrayList<>();
                Map<Ingrediente, Integer> stock = new HashMap<>();
                List<IndicadorDesempenho> indicadores = new ArrayList<>();

                PreparedStatement pstmPostos = conn.prepareStatement("SELECT * FROM posto WHERE id_restaurante = ?");
                pstmPostos.setInt(1, idRestaurante);
                ResultSet rsPostos = pstmPostos.executeQuery();

                while (rsPostos.next()) {
                    int idPosto = rsPostos.getInt("id");
                    String tipo = rsPostos.getString("tipo");
                    Posto posto = new Posto();
                    posto.setTipo(tipo);

                    PreparedStatement pstmMensagens = conn.prepareStatement("SELECT texto FROM mensagem WHERE id_posto = ? ORDER BY data_hora");
                    pstmMensagens.setInt(1, idPosto);
                    ResultSet rsMensagens = pstmMensagens.executeQuery();

                    while (rsMensagens.next()) {
                        String texto = rsMensagens.getString("texto");
                        posto.adicionarMensagem(texto);
                    }

                    PreparedStatement pstmFuncionarios = conn.prepareStatement(
                            "SELECT f.id, f.nome, f.nif, " +
                                    "CASE " +
                                    "  WHEN EXISTS (SELECT 1 FROM atendente a WHERE a.id = f.id) THEN 'ATENDENTE' " +
                                    "  WHEN EXISTS (SELECT 1 FROM cozinheiro c WHERE c.id = f.id) THEN 'COZINHEIRO' " +
                                    "  WHEN EXISTS (SELECT 1 FROM gestor g WHERE g.id = f.id) THEN 'GESTOR' " +
                                    "  ELSE 'DESCONHECIDO' " +
                                    "END as tipo " +
                                    "FROM funcionario f WHERE f.id_posto = ?");
                    pstmFuncionarios.setInt(1, idPosto);
                    ResultSet rsFuncionarios = pstmFuncionarios.executeQuery();

                    while (rsFuncionarios.next()) {
                        int idFunc = rsFuncionarios.getInt("id");
                        String nomeFunc = rsFuncionarios.getString("nome");
                        int nifFunc = rsFuncionarios.getInt("nif");
                        String tipoFunc = rsFuncionarios.getString("tipo");

                        Funcionario funcionario;
                        switch (tipoFunc.toUpperCase()) {
                            case "GESTOR" -> funcionario = new Gestor(idFunc, nomeFunc, nifFunc);
                            case "COZINHEIRO" -> funcionario = new Cozinheiro(idFunc, nomeFunc, nifFunc);
                            case "ATENDENTE" -> funcionario = new Atendente(idFunc, nomeFunc, nifFunc);
                            default -> funcionario = null;
                        }

                        if (funcionario != null) {
                            List<Funcionario> funcionariosPosto = posto.getFuncionarios();
                            funcionariosPosto.add(funcionario);
                            posto.setFuncionarios(funcionariosPosto);
                        }
                    }

                    postos.add(posto);
                }

                PreparedStatement pstmStock = conn.prepareStatement(
                        "SELECT i.nome, i.preco, s.quantidade " +
                                "FROM stock s " +
                                "JOIN ingrediente i ON s.nome_ingrediente = i.nome " +
                                "WHERE s.id_restaurante = ?");
                pstmStock.setInt(1, idRestaurante);
                ResultSet rsStock = pstmStock.executeQuery();

                while (rsStock.next()) {
                    String nomeIngrediente = rsStock.getString("nome");
                    double preco = rsStock.getDouble("preco");
                    Set<String> alergenios = new HashSet<>();
                    Ingrediente ingrediente = new Ingrediente(nomeIngrediente, preco, alergenios);
                    int quantidade = rsStock.getInt("quantidade");
                    stock.put(ingrediente, quantidade);
                }

                IndicadorDAO indicadorDAO = IndicadorDAO.getInstance();
                List<IndicadorDesempenho> indicadoresRestaurante = indicadorDAO.get(idRestaurante);
                if (indicadoresRestaurante != null)
                    indicadores.addAll(indicadoresRestaurante);

                return new Restaurante(idRestaurante, nome, localizacao, postos, stock, indicadores);
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Restaurante put(Integer key, Restaurante value) {
        Restaurante restauranteExistente = get(key);

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            conn.setAutoCommit(false);

            try {
                if (restauranteExistente == null) {
                    PreparedStatement pstm = conn.prepareStatement("INSERT INTO restaurante (id, nome, localizacao) VALUES (?, ?, ?)");
                    pstm.setInt(1, key);
                    pstm.setString(2, value.getNome());
                    pstm.setString(3, value.getLocalizacao());
                    pstm.executeUpdate();
                } else {
                    PreparedStatement pstm = conn.prepareStatement("UPDATE restaurante SET nome = ?, localizacao = ? WHERE id = ?");
                    pstm.setString(1, value.getNome());
                    pstm.setString(2, value.getLocalizacao());
                    pstm.setInt(3, key);
                    pstm.executeUpdate();
                }

                for (Posto posto : value.getPostos()) {
                    PreparedStatement pstmBuscarPosto = conn.prepareStatement("SELECT id FROM posto WHERE id_restaurante = ? AND tipo = ?");
                    pstmBuscarPosto.setInt(1, key);
                    pstmBuscarPosto.setString(2, posto.getTipo());
                    ResultSet rsPosto = pstmBuscarPosto.executeQuery();

                    int postoId;
                    if (rsPosto.next()) {
                        postoId = rsPosto.getInt("id");
                    } else {
                        PreparedStatement pstmPosto = conn.prepareStatement("INSERT INTO posto (tipo, id_restaurante) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                        pstmPosto.setString(1, posto.getTipo());
                        pstmPosto.setInt(2, key);
                        pstmPosto.executeUpdate();
                        ResultSet rs = pstmPosto.getGeneratedKeys();
                        postoId = rs.next() ? rs.getInt(1) : 0;
                    }

                    for (String mensagem : posto.getMensagens()) {
                        PreparedStatement pstmMensagem = conn.prepareStatement(
                                "INSERT INTO mensagem (id_posto, texto) VALUES (?, ?) " +
                                        "ON DUPLICATE KEY UPDATE texto = texto");
                        pstmMensagem.setInt(1, postoId);
                        pstmMensagem.setString(2, mensagem);
                        pstmMensagem.executeUpdate();
                    }
                }

                for (Map.Entry<Ingrediente, Integer> entry : value.getStock().entrySet()) {
                    Ingrediente ingrediente = entry.getKey();

                    PreparedStatement pstmIngrediente = conn.prepareStatement("INSERT INTO ingrediente (nome, preco) VALUES (?, ?) ON DUPLICATE KEY UPDATE preco = ?");
                    pstmIngrediente.setString(1, ingrediente.getNome());
                    pstmIngrediente.setDouble(2, ingrediente.getPreco());
                    pstmIngrediente.setDouble(3, ingrediente.getPreco());
                    pstmIngrediente.executeUpdate();

                    PreparedStatement pstmStock = conn.prepareStatement("INSERT INTO stock (id_restaurante, nome_ingrediente, quantidade) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantidade = ?");
                    pstmStock.setInt(1, key);
                    pstmStock.setString(2, ingrediente.getNome());
                    pstmStock.setInt(3, entry.getValue());
                    pstmStock.setInt(4, entry.getValue());
                    pstmStock.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return restauranteExistente;
    }

    @Override
    public Collection<Restaurante> values() {
        List<Restaurante> restaurantesList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT id FROM restaurante ORDER BY id")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Restaurante restaurante = get(id);
                if (restaurante != null) {
                    restaurantesList.add(restaurante);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return restaurantesList;
    }

    @Override public int size() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Restaurante remove(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void putAll(Map<? extends Integer, ? extends Restaurante> m) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void clear() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Entry<Integer, Restaurante>> entrySet() { throw new UnsupportedOperationException("Não implementado"); }
}