package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;

import java.sql.*;
import java.util.*;

public class ProdutoDAO implements Map<Integer, Produto> {
    private static ProdutoDAO instance = null;

    private ProdutoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // 1. Tabela de Produtos
            String sqlProduto = "CREATE TABLE IF NOT EXISTS produto (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(100) NOT NULL," +
                    "preco DECIMAL(10,2) NOT NULL," +
                    "tamanho VARCHAR(20))";
            stm.executeUpdate(sqlProduto);

            // 2. Tabela de ligação Produto-Ingrediente (N-M)
            String sqlProdIng = "CREATE TABLE IF NOT EXISTS produto_ingredientes (" +
                    "id_produto INT NOT NULL," +
                    "nome_ingrediente VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY (id_produto, nome_ingrediente)," +
                    "FOREIGN KEY (id_produto) REFERENCES produto(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (nome_ingrediente) REFERENCES ingrediente(nome) ON DELETE CASCADE)";
            stm.executeUpdate(sqlProdIng);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static synchronized ProdutoDAO getInstance() {
        if (instance == null) instance = new ProdutoDAO();
        return instance;
    }

    @Override
    public Produto get(Object key) {
        if (!(key instanceof Integer)) return null;
        int id = (Integer) key;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            PreparedStatement pstm = conn.prepareStatement("SELECT * FROM produto WHERE id = ?");
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");

                String tamanhoStr = rs.getString("tamanho");
                Tamanho tamanho = (tamanhoStr != null) ? Tamanho.valueOf(tamanhoStr) : Tamanho.M;

                Set<Ingrediente> ingredientes = carregarIngredientes(id, conn);

                return new Produto(id, nome, preco, tamanho, ingredientes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<Ingrediente> carregarIngredientes(int idProduto, Connection conn) throws SQLException {
        Set<Ingrediente> ingredientes = new HashSet<>();
        String sql = "SELECT nome_ingrediente FROM produto_ingredientes WHERE id_produto = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idProduto);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Ingrediente ing = IngredienteDAO.getInstance().get(rs.getString("nome_ingrediente"));
                if (ing != null) ingredientes.add(ing);
            }
        }
        return ingredientes;
    }

    @Override
    public Produto put(Integer key, Produto value) {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                // 1. Gravar Produto
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO produto (id, nome, preco, tamanho) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE nome=VALUES(nome), preco=VALUES(preco), tamanho=VALUES(tamanho)");
                pstm.setInt(1, value.getId());
                pstm.setString(2, value.getNome());
                pstm.setDouble(3, value.getPreco());
                pstm.setString(4, value.getTamanho().name());
                pstm.executeUpdate();

                // 2. Atualizar Ingredientes
                PreparedStatement del = conn.prepareStatement("DELETE FROM produto_ingredientes WHERE id_produto = ?");
                del.setInt(1, value.getId());
                del.executeUpdate();

                for (Ingrediente ing : value.getIngredientes()) {
                    PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO produto_ingredientes (id_produto, nome_ingrediente) VALUES (?, ?)");
                    ins.setInt(1, value.getId());
                    ins.setString(2, ing.getNome());
                    ins.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public Collection<Produto> values() {
        List<Produto> res = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT id FROM produto")) {
            while (rs.next()) {
                res.add(this.get(rs.getInt("id")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return res;
    }

    // Restantes métodos do Map
    @Override public int size() { return values().size(); }
    @Override public boolean isEmpty() { return size() == 0; }
    @Override public boolean containsKey(Object key) { return get(key) != null; }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException(); }
    @Override public Produto remove(Object key) { throw new UnsupportedOperationException(); }
    @Override public void putAll(Map<? extends Integer, ? extends Produto> m) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException(); }
    @Override public Set<Entry<Integer, Produto>> entrySet() { throw new UnsupportedOperationException(); }
}