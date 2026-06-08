package pt.uminho.taki.dao;

import pt.uminho.taki.ln.fornecimentos.Encomenda;
import pt.uminho.taki.ln.fornecimentos.LinhaEncomenda;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para Encomenda.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class EncomendaDAO extends AbstractDAO<String, Encomenda> {

    /**
     * Guarda uma encomenda.
     * 
     * @param key a chave
     * @param encomenda a encomenda
     * @return a encomenda guardada
     */
    @Override
    public Encomenda save(String key, Encomenda encomenda) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Encomenda (id_encomenda, data_criacao, data_entrega, estado, id_loja, id_fornecedor) VALUES (?, ?, ?, ?, ?, ?)"
            : "UPDATE Encomenda SET data_criacao=?, data_entrega=?, estado=?, id_loja=?, id_fornecedor=? WHERE id_encomenda=?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (isNew) {
                    ps.setObject(1, UUID.fromString(key));
                    ps.setTimestamp(2, Timestamp.valueOf(encomenda.getDataCriacao()));
                    if (encomenda.getDataEntrega() != null) {
                        ps.setTimestamp(3, Timestamp.valueOf(encomenda.getDataEntrega()));
                    } else {
                        ps.setNull(3, Types.TIMESTAMP);
                    }
                    ps.setString(4, toEstadoPersistencia(encomenda.getEstadoAtual().getDesignacao()));
                    ps.setInt(5, Integer.parseInt(encomenda.getIdLoja()));
                    ps.setObject(6, UUID.fromString(encomenda.getIdFornecedor()));
                } else {
                    ps.setTimestamp(1, Timestamp.valueOf(encomenda.getDataCriacao()));
                    if (encomenda.getDataEntrega() != null) {
                        ps.setTimestamp(2, Timestamp.valueOf(encomenda.getDataEntrega()));
                    } else {
                        ps.setNull(2, Types.TIMESTAMP);
                    }
                    ps.setString(3, toEstadoPersistencia(encomenda.getEstadoAtual().getDesignacao()));
                    ps.setInt(4, Integer.parseInt(encomenda.getIdLoja()));
                    ps.setObject(5, UUID.fromString(encomenda.getIdFornecedor()));
                    ps.setObject(6, UUID.fromString(key));
                }
                ps.executeUpdate();
                
                // Save linhas
                if (encomenda.getLinhas() != null && !encomenda.getLinhas().isEmpty()) {
                    saveLinhas(conn, key, encomenda.getLinhas());
                }
                
                conn.commit();
                return encomenda;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Encomenda", e);
        }
    }

    /**
     * Persiste as linhas associadas a uma encomenda.
     * 
     * @param conn a conexão ativa à base de dados
     * @param idEncomenda o identificador da encomenda pai
     * @param linhas a lista de linhas de encomenda a persistir
     * @throws SQLException se ocorrer um erro durante a inserção em lote
     */
    private void saveLinhas(Connection conn, String idEncomenda, List<LinhaEncomenda> linhas) throws SQLException {
        String deleteSql = "DELETE FROM Linha_Encomenda WHERE id_encomenda = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setObject(1, UUID.fromString(idEncomenda));
            ps.executeUpdate();
        }
        
        String insertSql = "INSERT INTO Linha_Encomenda (id_linha_encomenda, quantidade, preco, id_encomenda, id_produto) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (LinhaEncomenda linha : linhas) {
                ps.setObject(1, UUID.randomUUID());
                ps.setDouble(2, linha.getQuantidade());
                ps.setDouble(3, linha.getPrecoCustoAplicado());
                ps.setObject(4, UUID.fromString(idEncomenda));
                ps.setObject(5, UUID.fromString(linha.getIdProduto()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Procura uma encomenda por identificador.
     * 
     * @param key a chave
     * @return um Optional que contém a encomenda, se encontrada
     */
    @Override
    public Optional<Encomenda> findById(String key) {
        String sql = "SELECT * FROM Encomenda WHERE id_encomenda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Encomenda e = mapResultSet(rs);
                e.setLinhas(getLinhas(key));
                return Optional.of(e);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Encomenda", e);
        }
    }

    /**
     * Procura todas as encomendas.
     * 
     * @return uma coleção de todas as encomendas
     */
    @Override
    public Collection<Encomenda> findAll() {
        String sql = "SELECT * FROM Encomenda";
        List<Encomenda> encomendas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Encomenda e = mapResultSet(rs);
                e.setLinhas(getLinhas(e.getIdEncomenda()));
                encomendas.add(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Encomendas", e);
        }
        return encomendas;
    }

    /**
     * Verifica se uma encomenda existe.
     * 
     * @param key a chave
     * @return true se existir, false caso contrário
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Encomenda WHERE id_encomenda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Encomenda existence", e);
        }
    }

    /**
     * Elimina uma encomenda por identificador.
     * 
     * @param key a chave
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Encomenda WHERE id_encomenda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Encomenda", e);
        }
    }

    /**
     * Conta as encomendas.
     * 
     * @return o número de encomendas
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Encomenda";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Encomendas", e);
        }
    }

    /**
     * Elimina todas as encomendas.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Encomenda";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Encomendas", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Encomenda.
     * 
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Encomenda construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Encomenda mapResultSet(ResultSet rs) throws SQLException {
        String idEncomenda = rs.getObject("id_encomenda", UUID.class).toString();
        String idFornecedor = rs.getObject("id_fornecedor", UUID.class).toString();
        String idLoja = String.valueOf(rs.getInt("id_loja"));
        String estadoStr = rs.getString("estado");
        
        Encomenda e = new Encomenda(idEncomenda, idFornecedor, idLoja);
        e.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        
        // Restaurar estado
        if ("Pendente".equals(estadoStr)) e.setEstadoAtual(new pt.uminho.taki.ln.fornecimentos.estados.EstadoPendente());
        else if ("Em Trânsito".equals(estadoStr) || "Enviada".equals(estadoStr)) e.setEstadoAtual(new pt.uminho.taki.ln.fornecimentos.estados.EstadoEnviada());
        else if ("Entregue".equals(estadoStr) || "Concluída".equals(estadoStr)) e.setEstadoAtual(new pt.uminho.taki.ln.fornecimentos.estados.EstadoConcluida());
        else if ("Cancelada".equals(estadoStr)) e.setEstadoAtual(new pt.uminho.taki.ln.fornecimentos.estados.EstadoCancelada());
        
        java.sql.Date dataEntregaDate = rs.getDate("data_entrega");
        if (dataEntregaDate != null) {
            e.setDataEntrega(dataEntregaDate.toLocalDate());
        }
        return e;
    }

    /**
     * Obtém as linhas de uma encomenda.
     * 
     * @param idEncomenda o identificador da encomenda
     * @return a lista de linhas
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<LinhaEncomenda> getLinhas(String idEncomenda) {
        String sql = "SELECT * FROM Linha_Encomenda WHERE id_encomenda = ?";
        List<LinhaEncomenda> linhas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(idEncomenda));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                LinhaEncomenda linha = new LinhaEncomenda(
                    idEncomenda,
                    rs.getObject("id_produto", UUID.class).toString(),
                    rs.getDouble("quantidade"),
                    rs.getDouble("preco")
                );
                linha.setIdLinhaEncomenda(rs.getObject("id_linha_encomenda", UUID.class).toString());
                linhas.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting linhas encomenda", e);
        }
        return linhas;
    }

    /**
     * Converte a designação do estado de domínio para o formato de persistência.
     * 
     * @param estadoDominio a designação do estado no domínio
     * @return a representação textual para a base de dados
     */
    private String toEstadoPersistencia(String estadoDominio) {
        return estadoDominio;
    }
}
