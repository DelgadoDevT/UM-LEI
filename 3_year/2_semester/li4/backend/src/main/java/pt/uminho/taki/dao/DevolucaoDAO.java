package pt.uminho.taki.dao;

import pt.uminho.taki.ln.vendas.Devolucao;
import pt.uminho.taki.ln.vendas.LinhaVenda;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para Devolucao.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class DevolucaoDAO extends AbstractDAO<String, Devolucao> {
    
    /**
     * Guarda uma devolução e as suas respetivas linhas.
     * 
     * @param devolucao a devolução
     * @param linhas as linhas
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    public void save(Devolucao devolucao, List<LinhaVenda> linhas) {
        String key = devolucao.getIdDevolucao();
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Devolucao (id_devolucao, data_hora, valor, metodo_reembolso, num_nota_credito, id_venda, id_funcionario) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Devolucao SET data_hora=?, valor=?, metodo_reembolso=?, num_nota_credito=?, id_venda=?, id_funcionario=? WHERE id_devolucao=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (devolucao.getNumNotaCredito() == null || devolucao.getNumNotaCredito().isBlank()) {
                    devolucao.setNumNotaCredito(gerarNumeroNotaCredito(conn, devolucao.getDataHora()));
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (isNew) {
                        ps.setObject(1, UUID.fromString(key));
                        ps.setTimestamp(2, Timestamp.valueOf(devolucao.getDataHora()));
                        ps.setDouble(3, devolucao.getValor());
                        ps.setString(4, devolucao.getMetodoReembolso());
                        ps.setString(5, devolucao.getNumNotaCredito());
                        ps.setObject(6, UUID.fromString(devolucao.getIdVenda()));
                        ps.setObject(7, UUID.fromString(devolucao.getIdFuncionario()));
                    } else {
                        ps.setTimestamp(1, Timestamp.valueOf(devolucao.getDataHora()));
                        ps.setDouble(2, devolucao.getValor());
                        ps.setString(3, devolucao.getMetodoReembolso());
                        ps.setString(4, devolucao.getNumNotaCredito());
                        ps.setObject(5, UUID.fromString(devolucao.getIdVenda()));
                        ps.setObject(6, UUID.fromString(devolucao.getIdFuncionario()));
                        ps.setObject(7, UUID.fromString(key));
                    }
                    ps.executeUpdate();
                }
                
                // Salvar Linhas de Devolução
                saveLinhas(conn, key, linhas);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Devolucao and its lines", e);
        }
    }

    /**
     * Gera um número único para uma nota de crédito com base no ano e na sequência atual.
     * 
     * @param conn a conexão ativa à base de dados
     * @param dataHora o instante da devolução para extração do ano
     * @return o identificador formatado da nota de crédito
     * @throws SQLException se ocorrer um erro na consulta à base de dados
     */
    private String gerarNumeroNotaCredito(Connection conn, java.time.LocalDateTime dataHora) throws SQLException {
        int year = dataHora.getYear();
        int sequence = 1;
        String seqSql = "SELECT COUNT(*) FROM Devolucao WHERE EXTRACT(YEAR FROM data_hora) = ?";
        try (PreparedStatement ps = conn.prepareStatement(seqSql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sequence = rs.getInt(1) + 1;
            }
        }
        return String.format("NC %d/%d", year, sequence);
    }

    /**
     * Persiste as linhas associadas a uma devolução.
     * 
     * @param conn a conexão ativa à base de dados
     * @param idDevolucao o identificador da devolução pai
     * @param linhas a lista de linhas de venda a devolver
     * @throws SQLException se ocorrer um erro durante a inserção em lote
     */
    private void saveLinhas(Connection conn, String idDevolucao, List<LinhaVenda> linhas) throws SQLException {
        String deleteSql = "DELETE FROM Linha_Devolucao WHERE id_devolucao = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setObject(1, UUID.fromString(idDevolucao));
            ps.executeUpdate();
        }

        String insertSql = "INSERT INTO Linha_Devolucao (id_linha_devolucao, quantidade, valor, id_devolucao, id_linha_venda) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (LinhaVenda lv : linhas) {
                ps.setObject(1, UUID.randomUUID());
                ps.setDouble(2, lv.getQuantidade());
                ps.setDouble(3, lv.getTotalFinal());
                ps.setObject(4, UUID.fromString(idDevolucao));
                ps.setObject(5, UUID.fromString(lv.getIdLinhaVenda()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Obtém todos os identificadores de linhas de venda que já foram devolvidos.
     * 
     * @return um mapa onde a chave é o id_linha_venda e o valor é a quantidade total já devolvida.
     */
    public Map<String, Double> getQuantidadesDevolvidas() {
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT id_linha_venda, SUM(quantidade) as total_devolvido FROM Linha_Devolucao GROUP BY id_linha_venda";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("id_linha_venda"), rs.getDouble("total_devolvido"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting returned quantities", e);
        }
        return result;
    }

    /**
     * Guarda uma devolução.
     * 
     * @param key a chave
     * @param devolucao a devolução
     * @return a devolução guardada
     */
    @Override
    public Devolucao save(String key, Devolucao devolucao) {
        // Fallback para cumprir interface se necessário, mas preferimos a versão com linhas
        save(devolucao, new ArrayList<>());
        return devolucao;
    }

    /**
     * Procura uma devolução por identificador.
     * 
     * @param key a chave
     * @return um Optional que contém a devolução, se encontrada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Optional<Devolucao> findById(String key) {
        String sql = "SELECT * FROM Devolucao WHERE id_devolucao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Devolucao", e);
        }
    }

    /**
     * Procura todas as devoluções.
     * 
     * @return uma coleção de todas as devoluções
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Collection<Devolucao> findAll() {
        String sql = "SELECT * FROM Devolucao";
        List<Devolucao> devolucoes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                devolucoes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Devolucoes", e);
        }
        return devolucoes;
    }

    /**
     * Verifica se uma devolução existe.
     * 
     * @param key a chave
     * @return true se existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Devolucao WHERE id_devolucao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Devolucao existence", e);
        }
    }

    /**
     * Elimina uma devolução por identificador.
     * 
     * @param key a chave
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Devolucao WHERE id_devolucao = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Devolucao", e);
        }
    }

    /**
     * Conta as devoluções.
     * 
     * @return o número de devoluções
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Devolucao";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Devolucoes", e);
        }
    }

    /**
     * Elimina todas as devoluções.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Devolucao";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Devolucoes", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Devolucao.
     * 
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Devolucao construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Devolucao mapResultSet(ResultSet rs) throws SQLException {
        Devolucao d = new Devolucao();
        d.setIdDevolucao(rs.getObject("id_devolucao", UUID.class).toString());
        d.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        d.setValor(rs.getDouble("valor"));
        d.setMetodoReembolso(rs.getString("metodo_reembolso"));
        d.setNumNotaCredito(rs.getString("num_nota_credito"));
        d.setIdVenda(rs.getObject("id_venda", UUID.class).toString());
        d.setIdFuncionario(rs.getObject("id_funcionario", UUID.class).toString());
        return d;
    }
}
