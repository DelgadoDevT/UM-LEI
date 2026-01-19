package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;

import java.sql.*;
import java.util.*;

public class MenuDAO implements Map<Integer, Menu> {
    private static MenuDAO instance = null;

    private MenuDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // 1. Tabela de Menus
            String sqlMenu = "CREATE TABLE IF NOT EXISTS menu (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(100) NOT NULL," +
                    "tipo VARCHAR(20) NOT NULL," + // 'PACK' ou 'CATEGORIA'
                    "preco DECIMAL(10,2)," +        // Apenas para MenuPack
                    "categoria VARCHAR(50))";       // Apenas para MenuCategoria
            stm.executeUpdate(sqlMenu);

            // 2. Tabela de ligação Menu-Produto (Relação N-M)
            String sqlMenuProdutos = "CREATE TABLE IF NOT EXISTS menu_produtos (" +
                    "id_menu INT NOT NULL," +
                    "id_produto INT NOT NULL," +
                    "PRIMARY KEY (id_menu, id_produto)," +
                    "FOREIGN KEY (id_menu) REFERENCES menu(id) ON DELETE CASCADE)";
            stm.executeUpdate(sqlMenuProdutos);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static synchronized MenuDAO getInstance() {
        if (instance == null) instance = new MenuDAO();
        return instance;
    }

    @Override
    public Menu get(Object key) {
        if (!(key instanceof Integer)) return null;
        int id = (Integer) key;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            PreparedStatement pstm = conn.prepareStatement("SELECT * FROM menu WHERE id = ?");
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                String tipo = rs.getString("tipo");
                String nome = rs.getString("nome");
                Set<Produto> produtos = carregarProdutosDoMenu(id, conn);

                if ("PACK".equals(tipo)) {
                    double preco = rs.getDouble("preco");
                    return new MenuPack(id, nome, produtos, preco);
                } else {
                    String categoria = rs.getString("categoria");
                    return new MenuCategoria(id, nome, produtos, categoria);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<Produto> carregarProdutosDoMenu(int idMenu, Connection conn) throws SQLException {
        Set<Produto> produtos = new HashSet<>();
        String sql = "SELECT id_produto FROM menu_produtos WHERE id_menu = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idMenu);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Produto p = ProdutoDAO.getInstance().get(rs.getInt("id_produto"));
                if (p != null) produtos.add(p);
            }
        }
        return produtos;
    }

    @Override
    public Menu put(Integer key, Menu value) {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                // 1. UPSERT do Menu
                String sql = "INSERT INTO menu (id, nome, tipo, preco, categoria) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE nome=VALUES(nome), preco=VALUES(preco), categoria=VALUES(categoria)";
                PreparedStatement pstm = conn.prepareStatement(sql);
                pstm.setInt(1, value.getId());
                pstm.setString(2, value.getNome());
                pstm.setString(3, (value instanceof MenuPack) ? "PACK" : "CATEGORIA");

                if (value instanceof MenuPack) {
                    pstm.setDouble(4, ((MenuPack) value).getPreco());
                    pstm.setNull(5, Types.VARCHAR);
                } else {
                    pstm.setNull(4, Types.DECIMAL);
                    pstm.setString(5, ((MenuCategoria) value).getNomeCategoria());
                }
                pstm.executeUpdate();

                // 2. Atualizar Produtos do Menu
                PreparedStatement del = conn.prepareStatement("DELETE FROM menu_produtos WHERE id_menu = ?");
                del.setInt(1, value.getId());
                del.executeUpdate();

                for (Produto p : value.getProdutos()) {
                    PreparedStatement ins = conn.prepareStatement("INSERT INTO menu_produtos (id_menu, id_produto) VALUES (?, ?)");
                    ins.setInt(1, value.getId());
                    ins.setInt(2, p.getId());
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

    @Override public int size() { return values().size(); }
    @Override public boolean isEmpty() { return size() == 0; }

    @Override
    public Collection<Menu> values() {
        List<Menu> res = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT id FROM menu")) {
            while (rs.next()) {
                res.add(this.get(rs.getInt("id")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return res;
    }

    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public boolean containsKey(Object key) { return get(key) != null; }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException(); }
    @Override public Set<Entry<Integer, Menu>> entrySet() { throw new UnsupportedOperationException(); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException(); }
    @Override public void putAll(Map<? extends Integer, ? extends Menu> m) { throw new UnsupportedOperationException(); }
    @Override public Menu remove(Object key) { throw new UnsupportedOperationException(); }
}