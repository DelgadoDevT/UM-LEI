package pt.uminho.taki.dao;

import pt.uminho.taki.ln.vendas.Promocao;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para Promoção.
 * @author TakiLN Team
 * @since 1.0
 */
public class PromocaoDAO extends AbstractDAO<String, Promocao> {
    
    /**
     * Guarda a promoção.
     * @param key a chave
     * @param promocao a promoção
     * @return a Promoção
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Promocao save(String key, Promocao promocao) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Promocao (id_promocao, designacao, desconto, data_inicio, data_fim, estado, id_loja) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Promocao SET designacao=?, desconto=?, data_inicio=?, data_fim=?, estado=?, id_loja=? WHERE id_promocao=?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (isNew) {
                ps.setObject(1, UUID.fromString(key));
                ps.setString(2, promocao.getDesignacao());
                ps.setDouble(3, promocao.getDesconto());
                ps.setTimestamp(4, Timestamp.valueOf(promocao.getDataInicio()));
                ps.setTimestamp(5, Timestamp.valueOf(promocao.getDataFim()));
                ps.setString(6, promocao.getEstado());
                ps.setInt(7, promocao.getIdLoja());
            } else {
                ps.setString(1, promocao.getDesignacao());
                ps.setDouble(2, promocao.getDesconto());
                ps.setTimestamp(3, Timestamp.valueOf(promocao.getDataInicio()));
                ps.setTimestamp(4, Timestamp.valueOf(promocao.getDataFim()));
                ps.setString(5, promocao.getEstado());
                ps.setInt(6, promocao.getIdLoja());
                ps.setObject(7, UUID.fromString(key));
            }
            ps.executeUpdate();
            return promocao;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Promocao", e);
        }
    }

    /**
     * Procura a promoção pelo identificador.
     * @param key a chave
     * @return o Optional
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Promocao> findById(String key) {
        String sql = "SELECT * FROM Promocao WHERE id_promocao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Promocao", e);
        }
    }

    /**
     * Procura todas as promoções.
     * @return a coleção
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Promocao> findAll() {
        String sqlPromocoes = "SELECT * FROM Promocao";
        String sqlProdutos = "SELECT * FROM Promocao_Produto";
        String sqlCategorias = "SELECT * FROM Promocao_Categoria";
        
        List<Promocao> promocoes = new ArrayList<>();
        Map<String, Set<String>> produtosPorPromocao = new HashMap<>();
        Map<String, Set<String>> categoriasPorPromocao = new HashMap<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery(sqlProdutos)) {
                while (rs.next()) {
                    String idPromocao = rs.getObject("id_promocao", UUID.class).toString();
                    String idProduto = rs.getObject("id_produto", UUID.class).toString();
                    produtosPorPromocao.computeIfAbsent(idPromocao, k -> new HashSet<>()).add(idProduto);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery(sqlCategorias)) {
                while (rs.next()) {
                    String idPromocao = rs.getObject("id_promocao", UUID.class).toString();
                    String idCategoria = rs.getObject("id_categoria", UUID.class).toString();
                    categoriasPorPromocao.computeIfAbsent(idPromocao, k -> new HashSet<>()).add(idCategoria);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery(sqlPromocoes)) {
                while (rs.next()) {
                    Promocao p = mapResultSet(rs);
                    p.setProdutos(produtosPorPromocao.getOrDefault(p.getIdPromocao(), new HashSet<>()));
                    p.setCategorias(categoriasPorPromocao.getOrDefault(p.getIdPromocao(), new HashSet<>()));
                    promocoes.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Promocoes", e);
        }
        return promocoes;
    }

    /**
     * Verifica se a promoção existe.
     * @param key a chave
     * @return o valor booleano
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Promocao WHERE id_promocao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Promocao existence", e);
        }
    }

    /**
     * Elimina a promoção.
     * @param key a chave
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Promocao WHERE id_promocao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Promocao", e);
        }
    }

    /**
     * Conta o número de promoções.
     * @return o valor inteiro
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Promocao";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Promocoes", e);
        }
    }

    /**
     * Limpa todas as promoções.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Promocao";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Promocoes", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Promocao.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Promocao construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Promocao mapResultSet(ResultSet rs) throws SQLException {
        Promocao p = new Promocao();
        p.setIdPromocao(rs.getObject("id_promocao", UUID.class).toString());
        p.setDesignacao(rs.getString("designacao"));
        p.setDesconto(rs.getDouble("desconto"));
        p.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());
        p.setDataFim(rs.getTimestamp("data_fim").toLocalDateTime());
        p.setEstado(rs.getString("estado"));
        p.setIdLoja(rs.getInt("id_loja"));
        return p;
    }

    /**
     * Adiciona um produto à promoção.
     * @param idPromocao o identificador da promoção
     * @param idProduto o identificador do produto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void addProduto(String idPromocao, String idProduto) {
        String sql = "INSERT INTO Promocao_Produto (id_promocao, id_produto) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ps.setObject(2, UUID.fromString(idProduto));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding produto to promocao", e);
        }
    }

    /**
     * Remove um produto da promoção.
     * @param idPromocao o identificador da promoção
     * @param idProduto o identificador do produto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void removeProduto(String idPromocao, String idProduto) {
        String sql = "DELETE FROM Promocao_Produto WHERE id_promocao = ? AND id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ps.setObject(2, UUID.fromString(idProduto));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing produto from promocao", e);
        }
    }

    /**
     * Obtém os produtos da promoção.
     * @param idPromocao o identificador da promoção
     * @return o conjunto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Set<String> getProdutos(String idPromocao) {
        String sql = "SELECT id_produto FROM Promocao_Produto WHERE id_promocao = ?";
        Set<String> produtos = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                produtos.add(rs.getObject("id_produto", UUID.class).toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting produtos from promocao", e);
        }
        return produtos;
    }

    /**
     * Adiciona uma categoria à promoção.
     * @param idPromocao o identificador da promoção
     * @param idCategoria o identificador da categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void addCategoria(String idPromocao, String idCategoria) {
        String sql = "INSERT INTO Promocao_Categoria (id_promocao, id_categoria) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ps.setObject(2, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding categoria to promocao", e);
        }
    }

    /**
     * Remove uma categoria da promoção.
     * @param idPromocao o identificador da promoção
     * @param idCategoria o identificador da categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void removeCategoria(String idPromocao, String idCategoria) {
        String sql = "DELETE FROM Promocao_Categoria WHERE id_promocao = ? AND id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ps.setObject(2, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing categoria from promocao", e);
        }
    }

    /**
     * Obtém as categorias da promoção.
     * @param idPromocao o identificador da promoção
     * @return o conjunto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Set<String> getCategorias(String idPromocao) {
        String sql = "SELECT id_categoria FROM Promocao_Categoria WHERE id_promocao = ?";
        Set<String> categorias = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idPromocao));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categorias.add(rs.getObject("id_categoria", UUID.class).toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting categorias from promocao", e);
        }
        return categorias;
    }
}
