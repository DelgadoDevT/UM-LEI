package pt.uminho.taki.dao;

import pt.uminho.taki.ln.sincronizacao.dto.OutboxEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Objeto de Acesso a Dados (DAO) para entradas da outbox.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class OutboxDAO extends AbstractDAO<Long, OutboxEntry> {
    
    /**
     * Guarda uma entrada da outbox na base de dados.
     *
     * @param key a chave da entrada da outbox.
     * @param entry a entrada da outbox a guardar.
     * @return a entrada da outbox guardada.
     * @throws UnsupportedOperationException uma vez que as entradas da outbox são criadas exclusivamente por gatilhos (triggers) da base de dados.
     */
    @Override
    public OutboxEntry save(Long key, OutboxEntry entry) {
        throw new UnsupportedOperationException("Outbox entries are created by database triggers only");
    }

    /**
     * Procura uma entrada da outbox pela sua chave.
     *
     * @param key a chave da entrada da outbox.
     * @return um Optional que contém a entrada da outbox, caso seja encontrada.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<OutboxEntry> findById(Long key) {
        String sql = "SELECT * FROM Fila_Sincronizacao WHERE id_fila = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, key);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding OutboxEntry", e);
        }
    }

    /**
     * Procura todas as entradas da outbox.
     *
     * @return uma coleção de todas as entradas da outbox.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<OutboxEntry> findAll() {
        String sql = "SELECT * FROM Fila_Sincronizacao ORDER BY data_registo ASC";
        List<OutboxEntry> entries = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                entries.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all OutboxEntries", e);
        }
        return entries;
    }

    /**
     * Verifica se existe uma entrada da outbox para a chave fornecida.
     *
     * @param key a chave a verificar.
     * @return verdadeiro se a entrada da outbox existir, falso caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(Long key) {
        String sql = "SELECT 1 FROM Fila_Sincronizacao WHERE id_fila = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, key);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking OutboxEntry existence", e);
        }
    }

    /**
     * Elimina uma entrada da outbox pela sua chave.
     *
     * @param key a chave da entrada da outbox a eliminar.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(Long key) {
        String sql = "DELETE FROM Fila_Sincronizacao WHERE id_fila = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting OutboxEntry", e);
        }
    }

    /**
     * Elimina uma coleção de entradas da outbox.
     *
     * @param entries a coleção de entradas da outbox a eliminar.
     */
    public void deleteAll(Collection<OutboxEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<Long> ids = entries.stream().map(OutboxEntry::getIdFila).collect(Collectors.toList());
        deleteByIds(ids);
    }

    /**
     * Elimina entradas da outbox pelos seus identificadores.
     *
     * @param ids a coleção de identificadores de entradas da outbox a eliminar.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void deleteByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return;
        
        String sql = "DELETE FROM Fila_Sincronizacao WHERE id_fila = ANY(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            Array array = conn.createArrayOf("BIGINT", ids.toArray());
            ps.setArray(1, array);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting OutboxEntries by IDs", e);
        }
    }

    /**
     * Conta o número de entradas da outbox.
     *
     * @return o número total de entradas da outbox.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Fila_Sincronizacao";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting OutboxEntries", e);
        }
    }

    /**
     * Limpa todas as entradas da outbox da base de dados.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "TRUNCATE TABLE Fila_Sincronizacao RESTART IDENTITY";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing OutboxEntries", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio OutboxEntry.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto OutboxEntry construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private OutboxEntry mapResultSet(ResultSet rs) throws SQLException {
        return new OutboxEntry(
            rs.getLong("id_fila"),
            rs.getString("nome_tabela"),
            rs.getString("id_entidade"),
            rs.getString("operacao"),
            rs.getTimestamp("data_registo").toLocalDateTime()
        );
    }
}
