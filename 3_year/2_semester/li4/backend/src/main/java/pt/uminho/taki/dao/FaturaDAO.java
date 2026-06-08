package pt.uminho.taki.dao;

import pt.uminho.taki.ln.fatura.Fatura;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para a gestão de entidades de Fatura.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FaturaDAO extends AbstractDAO<String, Fatura> {

    /**
     * Guarda uma entidade de Fatura na base de dados.
     *
     * @param key o identificador da Fatura
     * @param fatura a entidade de Fatura a guardar
     * @return a entidade de Fatura guardada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Fatura save(String key, Fatura fatura) {
        try (Connection conn = getConnection()) {
            saveWithConnection(conn, fatura);
            return fatura;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Fatura", e);
        }
    }

    /**
     * Permite gravar uma fatura utilizando uma conexão existente, 
     * útil para manter a atomicidade em transações multi-DAO (ex: VendaDAO).
     *
     * @param conn a conexão ativa à base de dados
     * @param fatura a entidade de Fatura a guardar
     * @throws SQLException se ocorrer um erro durante a execução do SQL
     */
    public void saveWithConnection(Connection conn, Fatura fatura) throws SQLException {
        boolean isNew;
        // INFO: manual existence check intentional — uses shared connection
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Fatura WHERE id_fatura = ?")) {
            ps.setObject(1, UUID.fromString(fatura.getIdFatura()));
            isNew = !ps.executeQuery().next();
        }

        String sql = isNew
            ? "INSERT INTO Fatura (id_fatura, num_fatura, data_emissao, nif_cliente, hash, hash_control, id_venda) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Fatura SET num_fatura=?, data_emissao=?, nif_cliente=?, hash=?, hash_control=?, id_venda=? WHERE id_fatura=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isNew) {
                ps.setObject(1, UUID.fromString(fatura.getIdFatura()));
                ps.setString(2, fatura.getNumFatura());
                ps.setTimestamp(3, Timestamp.valueOf(fatura.getDataEmissao()));
                ps.setString(4, fatura.getNifCliente());
                ps.setString(5, fatura.getHash());
                ps.setString(6, fatura.getHashControl() != null ? fatura.getHashControl() : "1");
                ps.setObject(7, UUID.fromString(fatura.getIdVenda()));
            } else {
                ps.setString(1, fatura.getNumFatura());
                ps.setTimestamp(2, Timestamp.valueOf(fatura.getDataEmissao()));
                ps.setString(3, fatura.getNifCliente());
                ps.setString(4, fatura.getHash());
                ps.setString(5, fatura.getHashControl());
                ps.setObject(6, UUID.fromString(fatura.getIdVenda()));
                ps.setObject(7, UUID.fromString(fatura.getIdFatura()));
            }
            ps.executeUpdate();
        }
    }

    /**
     * Procura uma entidade de Fatura através do seu identificador.
     *
     * @param key o identificador da Fatura
     * @return um Optional que contém a Fatura, se encontrada, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Fatura> findById(String key) {
        String sql = "SELECT * FROM Fatura WHERE id_fatura = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Fatura", e);
        }
    }

    /**
     * Procura uma fatura associada a uma venda específica.
     *
     * @param idVenda o identificador da venda
     * @return um Optional que contém a Fatura, se encontrada, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<Fatura> findByVendaId(String idVenda) {
        String sql = "SELECT * FROM Fatura WHERE id_venda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Fatura by id_venda", e);
        }
    }

    /**
     * Procura uma fatura pelo seu número de fatura legível.
     *
     * @param numFatura o número da fatura
     * @return um Optional que contém a Fatura, se encontrada, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<Fatura> findByNumeroFatura(String numFatura) {
        String sql = "SELECT * FROM Fatura WHERE num_fatura = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numFatura);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Fatura by num_fatura", e);
        }
    }

    /**
     * Recupera todas as entidades de Fatura da base de dados.
     *
     * @return uma coleção de todas as entidades de Fatura
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Fatura> findAll() {
        String sql = "SELECT * FROM Fatura";
        List<Fatura> faturas = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                faturas.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Faturas", e);
        }
        return faturas;
    }

    /**
     * Verifica se uma entidade de Fatura existe.
     *
     * @param key o identificador da Fatura
     * @return true se a Fatura existir, false caso contrário
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Fatura WHERE id_fatura = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(key));
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Elimina uma entidade de Fatura através do seu identificador.
     *
     * @param key o identificador da Fatura a eliminar
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Fatura WHERE id_fatura = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Fatura", e);
        }
    }

    /**
     * Conta o número de entidades de Fatura na base de dados.
     *
     * @return o número total de entidades de Fatura
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Fatura";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Faturas", e);
        }
    }

    /**
     * Elimina todas as entidades de Fatura da base de dados.
     *
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Fatura";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Faturas", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Fatura.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Fatura construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Fatura mapResultSet(ResultSet rs) throws SQLException {
        Fatura f = new Fatura();
        f.setIdFatura(rs.getObject("id_fatura", UUID.class).toString());
        f.setNumFatura(rs.getString("num_fatura"));
        f.setDataEmissao(rs.getTimestamp("data_emissao").toLocalDateTime());
        f.setNifCliente(rs.getString("nif_cliente"));
        f.setHash(rs.getString("hash"));
        f.setHashControl(rs.getString("hash_control"));
        f.setIdVenda(rs.getObject("id_venda", UUID.class).toString());
        return f;
    }
}
