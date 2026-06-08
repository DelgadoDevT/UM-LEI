package pt.uminho.taki.dao;

import pt.uminho.taki.ln.lojas.Categoria;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para entidades de Categoria.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CategoriaDAO extends AbstractDAO<String, Categoria> {
    
    /**
     * Guarda a categoria com a chave fornecida.
     *
     * @param key a chave
     * @param categoria a categoria a guardar
     * @return a categoria guardada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Categoria save(String key, Categoria categoria) {
        boolean isNew = key == null || !exists(key);
        String sql;
        if (isNew) {
            // Se a chave for nula, é um novo objeto com um UUID gerado.
            key = categoria.getIdCategoria();
            sql = "INSERT INTO Categoria (id_categoria, designacao, id_categoria_pai) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE Categoria SET designacao=?, id_categoria_pai=? WHERE id_categoria=?";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            UUID idCategoriaPaiUUID = categoria.getIdCategoriaPai() != null && !categoria.getIdCategoriaPai().isEmpty()
                ? UUID.fromString(categoria.getIdCategoriaPai())
                : null;

            if (isNew) {
                ps.setObject(1, UUID.fromString(key));
                ps.setString(2, categoria.getDesignacao());
                ps.setObject(3, idCategoriaPaiUUID);
            } else {
                ps.setString(1, categoria.getDesignacao());
                ps.setObject(2, idCategoriaPaiUUID);
                ps.setObject(3, UUID.fromString(key));
            }
            ps.executeUpdate();
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Categoria", e);
        }
    }

    /**
     * Procura uma categoria pela sua chave.
     *
     * @param key a chave
     * @return um Optional que contém a categoria, se encontrada, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Optional<Categoria> findById(String key) {
        String sql = "SELECT * FROM Categoria WHERE id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Categoria", e);
        }
    }

    /**
     * Procura todas as categorias.
     *
     * @return uma coleção de todas as categorias
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Collection<Categoria> findAll() {
        String sql = "SELECT * FROM Categoria WHERE estado = 'Ativa'";
        List<Categoria> categorias = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categorias.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Categorias", e);
        }
        return categorias;
    }

    /**
     * Verifica se uma categoria existe através da sua chave.
     *
     * @param key a chave
     * @return true se existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Categoria WHERE id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Categoria existence", e);
        }
    }

    /**
     * Elimina uma categoria pela sua chave.
     *
     * @param key a chave
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Categoria WHERE id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Categoria", e);
        }
    }

    /**
     * Conta o número de categorias.
     *
     * @return a contagem das categorias
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Categoria";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Categorias", e);
        }
    }

    /**
     * Elimina todas as categorias.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Categoria";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Categorias", e);
        }
    }

    /**
     * Atualiza uma categoria.
     *
     * @param idCategoria o identificador da categoria
     * @param designacao a designação da categoria
     * @param idCategoriaPai o identificador da categoria pai
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    public void atualizarCategoria(String idCategoria, String designacao, String idCategoriaPai) {
        String sql = "UPDATE Categoria SET designacao = ?, id_categoria_pai = ? WHERE id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, designacao);
            if (idCategoriaPai == null || idCategoriaPai.isBlank()) {
                ps.setObject(2, null);
            } else {
                ps.setObject(2, UUID.fromString(idCategoriaPai));
            }
            ps.setObject(3, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Categoria", e);
        }
    }

    /**
     * Descontinuar uma categoria.
     *
     * @param idCategoria o identificador da categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    public void descontinuarCategoria(String idCategoria) {
        String sql = "UPDATE Categoria SET estado = 'Descontinuada' WHERE id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error discontinuing Categoria", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Categoria.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Categoria construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Categoria mapResultSet(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getObject("id_categoria", UUID.class).toString());
        c.setDesignacao(rs.getString("designacao"));
        UUID paiUUID = rs.getObject("id_categoria_pai", UUID.class);
        c.setIdCategoriaPai(paiUUID != null ? paiUUID.toString() : null);
        return c;
    }
}
