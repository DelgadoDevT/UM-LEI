package dss.cadeiaRestaurantesDL;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;

import java.sql.*;
import java.util.*;

public class PedidoDAO implements Map<Integer, Pedido> {
    private static PedidoDAO instance = null;

    private PedidoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // 1. Tabela de Pedidos
            String sql = "CREATE TABLE IF NOT EXISTS pedido (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "estado VARCHAR(50) NOT NULL," +
                    "tipo_consumo VARCHAR(50) NOT NULL," +
                    "id_restaurante INT NOT NULL," +
                    "valor_total DECIMAL(10,2) DEFAULT 0.0," +
                    "FOREIGN KEY (id_restaurante) REFERENCES restaurante(id))";
            stm.executeUpdate(sql);

            // 2. Tabela de Linhas de Pedido (Ligação 1-N com Pedido)
            String sqlLinhas = "CREATE TABLE IF NOT EXISTS linha_pedido (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "id_pedido INT NOT NULL," +
                    "quantidade INT NOT NULL," +
                    "preco_venda DECIMAL(10,2) NOT NULL," +
                    "id_artigo VARCHAR(50) NOT NULL," +
                    "tipo_artigo VARCHAR(20) NOT NULL," +
                    "nota TEXT," +
                    "ingredientes_personalizados TEXT," +
                    "FOREIGN KEY (id_pedido) REFERENCES pedido(id) ON DELETE CASCADE)";
            stm.executeUpdate(sqlLinhas);

            // 3. Tabela de Pagamentos (Ligação 1-1 ou 1-N com Pedido)
            String sqlPagamento = "CREATE TABLE IF NOT EXISTS pagamento (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "id_pedido INT NOT NULL UNIQUE," +
                    "valor DECIMAL(10,2) NOT NULL," +
                    "metodo VARCHAR(30) NOT NULL," +
                    "detalhes VARCHAR(255)," +
                    "FOREIGN KEY (id_pedido) REFERENCES pedido(id) ON DELETE CASCADE)";
            stm.executeUpdate(sqlPagamento);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static synchronized PedidoDAO getInstance() {
        if (instance == null) {
            instance = new PedidoDAO();
        }
        return instance;
    }

    private List<LinhaDePedido> carregarLinhas(int idPedido, Connection conn) throws SQLException {
        List<LinhaDePedido> linhas = new ArrayList<>();
        PreparedStatement pstm = conn.prepareStatement("SELECT * FROM linha_pedido WHERE id_pedido = ?");
        pstm.setInt(1, idPedido);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            LinhaDePedido lp = new LinhaDePedido();
            lp.setId(rs.getInt("id"));
            lp.setIdPedido(idPedido);
            lp.setQuantidade(rs.getInt("quantidade"));
            lp.setPreco(rs.getDouble("preco_venda"));

            int idArtigo = Integer.parseInt(rs.getString("id_artigo"));
            String tipo = rs.getString("tipo_artigo");

            if (tipo.equals("PRODUTO")) {
                Produto produtoOriginal = ProdutoDAO.getInstance().get(idArtigo);
                Produto produtoCopia = new Produto(produtoOriginal); // Criar cópia

                // Aplicar ingredientes personalizados se existirem
                String ingredientesPersonalizados = rs.getString("ingredientes_personalizados");
                if (ingredientesPersonalizados != null && !ingredientesPersonalizados.isEmpty()) {
                    Set<Ingrediente> novosIngredientes = deserializarIngredientes(ingredientesPersonalizados);
                    produtoCopia.setIngredientes(novosIngredientes);
                }

                lp.setArtigo(produtoCopia);
            } else {
                Menu m = MenuDAO.getInstance().get(idArtigo);
                if (m instanceof MenuPack) lp.setArtigo((MenuPack) m);
                else System.err.println("Erro: Menu #" + idArtigo + " não é um MenuPack vendável.");
            }

            lp.setNota(rs.getString("nota"));
            linhas.add(lp);
        }
        return linhas;
    }

    private Pagamento carregarPagamento(int idPedido, Connection conn) throws SQLException {
        PreparedStatement pstm = conn.prepareStatement("SELECT * FROM pagamento WHERE id_pedido = ?");
        pstm.setInt(1, idPedido);
        ResultSet rs = pstm.executeQuery();

        if (rs.next()) {
            String metodo = rs.getString("metodo");
            double valor = rs.getDouble("valor");
            String detalhes = rs.getString("detalhes");

            switch (metodo) {
                case "MBWay":
                    return new MBWay(valor);
                case "Cartão":
                    return new Cartao_Bancario(valor);
                case "Dinheiro":
                    return new Dinheiro(valor);
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public Pedido get(Object key) {
        if (!(key instanceof Integer)) return null;
        int id = (Integer) key;

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement("SELECT * FROM pedido WHERE id = ?")) {

            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Pedido p = new Pedido();
                p.setId(id);
                p.setEstado(Estado.valueOf(rs.getString("estado")));
                p.setTipoConsumo(Tipo.valueOf(rs.getString("tipo_consumo")));
                p.setIdRestaurante(rs.getInt("id_restaurante"));
                p.setValorTotal(rs.getDouble("valor_total"));

                p.setLinhas(carregarLinhas(id, conn));
                p.setPagamento(carregarPagamento(id, conn));

                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return null;
    }

    @Override
    public Pedido put(Integer key, Pedido value) {
        Pedido pedidoExistente = null;
        if (key != null && key != 0) {
            pedidoExistente = get(key);
        }

        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD)) {
            conn.setAutoCommit(false); // Iniciar transação

            try {
                // 1. Gravar Pedido (Básico)
                if (pedidoExistente == null) {
                    // Importante: Statement.RETURN_GENERATED_KEYS para obter o novo ID
                    PreparedStatement pstm = conn.prepareStatement(
                            "INSERT INTO pedido (id, estado, tipo_consumo, id_restaurante, valor_total) VALUES (?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    pstm.setInt(1, value.getId());
                    pstm.setString(2, value.getEstado().name());
                    pstm.setString(3, value.getTipoConsumo().name());
                    pstm.setInt(4, value.getIdRestaurante());
                    pstm.setDouble(5, value.getValorTotal());
                    pstm.executeUpdate();

                    try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int novoId = generatedKeys.getInt(1);
                            value.setId(novoId);
                        } else {
                            throw new SQLException("Falha ao criar pedido, nenhum ID obtido.");
                        }
                    }
                } else {
                    PreparedStatement pstm = conn.prepareStatement(
                            "UPDATE pedido SET estado = ?, tipo_consumo = ?, id_restaurante = ?, valor_total = ? WHERE id = ?");
                    pstm.setString(1, value.getEstado().name());
                    pstm.setString(2, value.getTipoConsumo().name());
                    pstm.setInt(3, value.getIdRestaurante());
                    pstm.setDouble(4, value.getValorTotal());
                    pstm.setInt(5, key);
                    pstm.executeUpdate();
                }

                PreparedStatement delLinhas = conn.prepareStatement("DELETE FROM linha_pedido WHERE id_pedido = ?");
                delLinhas.setInt(1, value.getId());
                delLinhas.executeUpdate();

                for (LinhaDePedido lp : value.getLinhas()) {
                    PreparedStatement insLinha = conn.prepareStatement(
                            "INSERT INTO linha_pedido (id_pedido, quantidade, preco_venda, id_artigo, tipo_artigo, nota, ingredientes_personalizados) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    insLinha.setInt(1, value.getId());
                    insLinha.setInt(2, lp.getQuantidade());
                    insLinha.setDouble(3, lp.getPreco());
                    insLinha.setInt(4, lp.getArtigo() != null ? lp.getArtigo().getId() : 0);
                    insLinha.setString(5, (lp.getArtigo() instanceof Produto) ? "PRODUTO" : "MENU");
                    insLinha.setString(6, lp.getNota());

                    String ingredientesSerialized = null;
                    if (lp.getArtigo() instanceof Produto) {
                        Produto produto = (Produto) lp.getArtigo();
                        ingredientesSerialized = serializarIngredientes(produto.getIngredientes());
                    }
                    insLinha.setString(7, ingredientesSerialized);

                    insLinha.executeUpdate();
                }

                if (value.getPagamento() != null) {
                    PreparedStatement insPag = conn.prepareStatement(
                            "INSERT INTO pagamento (id_pedido, valor, metodo, detalhes) VALUES (?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE valor=VALUES(valor), metodo=VALUES(metodo), detalhes=VALUES(detalhes)");
                    insPag.setInt(1, value.getId());
                    insPag.setDouble(2, value.getPagamento().getPagamento());
                    insPag.setString(3, value.getPagamento().getMethod());
                    insPag.setString(4, "");
                    insPag.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return pedidoExistente;
    }

    public List<Pedido> getPedidosPorRestaurante(Integer idRestaurante) {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement("SELECT id FROM pedido WHERE id_restaurante = ?")) {
            pstm.setInt(1, idRestaurante);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                pedidos.add(this.get(rs.getInt("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    @Override
    public Collection<Pedido> values() {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOConfig.URL, DAOConfig.USERNAME, DAOConfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT id FROM pedido ORDER BY id")) {
            while (rs.next()) {
                pedidos.add(this.get(rs.getInt("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    private String serializarIngredientes(Set<Ingrediente> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Ingrediente ing : ingredientes) {
            if (sb.length() > 0) sb.append(",");
            sb.append(ing.getNome()).append(":").append(ing.getPreco());
        }
        return sb.toString();
    }

    private Set<Ingrediente> deserializarIngredientes(String ingredientesStr) {
        Set<Ingrediente> ingredientes = new HashSet<>();
        if (ingredientesStr == null || ingredientesStr.isEmpty()) {
            return ingredientes;
        }

        String[] parts = ingredientesStr.split(",");
        for (String part : parts) {
            String[] dados = part.split(":");
            if (dados.length == 2) {
                String nome = dados[0];
                double preco = Double.parseDouble(dados[1]);
                ingredientes.add(new Ingrediente(nome, preco, new HashSet<>()));
            }
        }
        return ingredientes;
    }

    @Override public int size() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Pedido remove(Object key) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void putAll(Map<? extends Integer, ? extends Pedido> m) { throw new UnsupportedOperationException("Não implementado"); }
    @Override public void clear() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Integer> keySet() { throw new UnsupportedOperationException("Não implementado"); }
    @Override public Set<Entry<Integer, Pedido>> entrySet() { throw new UnsupportedOperationException("Não implementado"); }
}