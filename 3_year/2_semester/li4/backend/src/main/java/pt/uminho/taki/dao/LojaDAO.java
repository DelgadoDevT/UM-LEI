package pt.uminho.taki.dao;

import pt.uminho.taki.ln.lojas.Loja;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para a gestão de entidades de Loja.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LojaDAO extends AbstractDAO<Integer, Loja> {

    /**
     * Guarda uma entidade de Loja na base de dados.
     *
     * @param key o identificador da Loja
     * @param loja a entidade de Loja a guardar
     * @return a entidade de Loja guardada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Loja save(Integer key, Loja loja) {
        String sql = key == null
            ? "INSERT INTO Loja (nome, telefone, email, nif, rua, cidade, distrito) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_loja"
            : "UPDATE Loja SET nome=?, telefone=?, email=?, nif=?, rua=?, cidade=?, distrito=? WHERE id_loja=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loja.getNome());
            ps.setString(2, loja.getTelefone());
            ps.setString(3, loja.getEmail());
            ps.setString(4, loja.getNif());
            ps.setString(5, loja.getRua());
            ps.setString(6, loja.getCidade());
            ps.setString(7, loja.getDistrito());

            if (key == null) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loja.setIdLoja(rs.getInt("id_loja"));
                    }
                }
            } else {
                ps.setInt(8, key);
                ps.executeUpdate();
            }
            return loja;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Loja", e);
        }
    }

    /**
     * Procura uma entidade de Loja através do seu identificador.
     *
     * @param key o identificador da Loja
     * @return um Optional que contém a Loja, se encontrada, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Loja> findById(Integer key) {
        String sql = "SELECT * FROM Loja WHERE id_loja = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Loja", e);
        }
    }

    /**
     * Recupera todas as entidades de Loja da base de dados.
     *
     * @return uma coleção de todas as entidades de Loja
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Loja> findAll() {
        String sql = "SELECT * FROM Loja";
        List<Loja> lojas = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lojas.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Lojas", e);
        }
        return lojas;
    }

    /**
     * Verifica se uma entidade de Loja existe.
     *
     * @param key o identificador da Loja
     * @return true se a Loja existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(Integer key) {
        String sql = "SELECT 1 FROM Loja WHERE id_loja = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Loja existence", e);
        }
    }

    /**
     * Elimina uma entidade de Loja através do seu identificador.
     *
     * @param key o identificador da Loja a eliminar
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(Integer key) {
        String sql = "DELETE FROM Loja WHERE id_loja = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Loja", e);
        }
    }

    /**
     * Conta o número de entidades de Loja na base de dados.
     *
     * @return o número total de entidades de Loja
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Loja";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Lojas", e);
        }
    }

    /**
     * Elimina todas as entidades de Loja da base de dados.
     *
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Loja";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Lojas", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Loja.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Loja construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Loja mapResultSet(ResultSet rs) throws SQLException {
        Loja l = new Loja();
        l.setIdLoja(rs.getInt("id_loja"));
        l.setNome(rs.getString("nome"));
        l.setTelefone(rs.getString("telefone"));
        l.setEmail(rs.getString("email"));
        l.setNif(rs.getString("nif"));
        l.setRua(rs.getString("rua"));
        l.setCidade(rs.getString("cidade"));
        l.setDistrito(rs.getString("distrito"));
        return l;
    }
}
