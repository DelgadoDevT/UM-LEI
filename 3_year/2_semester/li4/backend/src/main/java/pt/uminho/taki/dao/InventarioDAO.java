package pt.uminho.taki.dao;

import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.inventario.MovimentoInventario;
import pt.uminho.taki.ln.inventario.TipoMovimento;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para a gestão de entidades de Inventário.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class InventarioDAO extends AbstractDAO<String, Inventario> {

    /**
     * Guarda uma entidade de Inventário na base de dados.
     *
     * @param key o identificador do Inventário
     * @param inventario a entidade de Inventário a guardar
     * @return a entidade de Inventário guardada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Inventario save(String key, Inventario inventario) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Inventario (id_inventario, quantidade, quantidade_minima, id_loja, id_produto) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE Inventario SET quantidade=?, quantidade_minima=? WHERE id_inventario=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (isNew) {                ps.setObject(1, UUID.fromString(key));
                ps.setDouble(2, inventario.getQuantidade());
                ps.setDouble(3, inventario.getQuantidadeMinima());
                ps.setInt(4, inventario.getIdLoja());
                ps.setObject(5, UUID.fromString(inventario.getIdProduto()));
            } else {
                ps.setDouble(1, inventario.getQuantidade());
                ps.setDouble(2, inventario.getQuantidadeMinima());
                ps.setObject(3, UUID.fromString(key));
            }
            ps.executeUpdate();
            return inventario;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Inventario", e);
        }
    }

    /**
     * Procura uma entidade de Inventário através do seu identificador.
     *
     * @param key o identificador do Inventário
     * @return um Optional que contém o Inventário, se encontrado, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Inventario> findById(String key) {
        String sql = "SELECT * FROM Inventario WHERE id_inventario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Inventario by id_inventario", e);
        }
    }

    /**
     * Procura uma entidade de Inventário através do identificador do produto.
     *
     * @param idProduto o identificador do produto
     * @return um Optional que contém o Inventário, se encontrado, ou vazio caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<Inventario> findByProdutoId(String idProduto) {
        String sql = "SELECT * FROM Inventario WHERE id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(idProduto));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Inventario by id_produto", e);
        }
    }

    /**
     * Verifica se uma entidade de Inventário existe.
     *
     * @param key o identificador do Inventário
     * @return true se o Inventário existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Inventario WHERE id_inventario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Inventario existence", e);
        }
    }

    /**
     * Recupera todas as entidades de Inventário da base de dados.
     *
     * @return uma coleção de todas as entidades de Inventário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Inventario> findAll() {
        String sql = "SELECT * FROM Inventario";
        List<Inventario> inventarios = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                inventarios.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Inventarios", e);
        }
        return inventarios;
    }

    /**
     * Mapeia uma linha do ResultSet para um objeto Inventario.
     *
     * @param rs o ResultSet posicionado na linha a mapear
     * @return o objeto Inventario mapeado
     * @throws SQLException se ocorrer um erro ao aceder aos dados do ResultSet
     */
    private Inventario mapResultSet(ResultSet rs) throws SQLException {
        String idInventario = rs.getObject("id_inventario", UUID.class).toString();
        double quantidade = rs.getDouble("quantidade");
        double quantidadeMinima = rs.getDouble("quantidade_minima");
        int idLoja = rs.getInt("id_loja");
        String idProduto = rs.getObject("id_produto", UUID.class).toString();
        return new Inventario(idInventario, quantidade, quantidadeMinima, idLoja, idProduto);
    }

    /**
     * Elimina uma entidade de Inventário através do seu identificador.
     *
     * @param key o identificador do Inventário a eliminar
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Inventario WHERE id_inventario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Inventario", e);
        }
    }

    /**
     * Conta o número de entidades de Inventário na base de dados.
     *
     * @return o número total de entidades de Inventário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Inventario";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Inventarios", e);
        }
    }

    /**
     * Elimina todas as entidades de Inventário da base de dados.
     *
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Inventario";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Inventarios", e);
        }
    }

    /**
     * Adiciona um movimento de inventário para um produto específico.
     *
     * @param idProduto o identificador do produto
     * @param movimento o movimento a adicionar
     * @throws RuntimeException lançada se o inventário não existir para o produto ou em caso de erro de acesso à base de dados.
     */
    public void addMovimento(String idProduto, MovimentoInventario movimento) {
        String sqlInventario = "SELECT id_inventario FROM Inventario WHERE id_produto = ?";
        String sqlInsert = "INSERT INTO Movimento_Inventario (id_movimento, tipo, quantidade, data_registo, motivo, id_inventario, id_funcionario) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement psInv = conn.prepareStatement(sqlInventario)) {
            psInv.setObject(1, UUID.fromString(idProduto));
            ResultSet rs = psInv.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Inventário inexistente para o produto " + idProduto);
            }
            UUID idInventario = rs.getObject("id_inventario", UUID.class);
            try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                psIns.setObject(1, movimento.getId() != null && !movimento.getId().isBlank() ? UUID.fromString(movimento.getId()) : UUID.randomUUID());
                psIns.setString(2, movimento.getTipo().name());
                psIns.setDouble(3, movimento.getQuantidade());
                psIns.setTimestamp(4, Timestamp.valueOf(movimento.getDataRegisto() != null ? movimento.getDataRegisto() : java.time.LocalDateTime.now()));
                psIns.setString(5, movimento.getMotivo());
                psIns.setObject(6, idInventario);
                if (movimento.getIdFuncionario() != null && !movimento.getIdFuncionario().isBlank()) {
                    psIns.setObject(7, UUID.fromString(movimento.getIdFuncionario()));
                } else {
                    psIns.setNull(7, Types.OTHER);
                }
                psIns.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding movimento de inventário", e);
        }
    }

    /**
     * Recupera todos os movimentos de inventário para um produto específico.
     *
     * @param idProduto o identificador do produto
     * @return uma lista de movimentos de inventário para o produto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<MovimentoInventario> getMovimentos(String idProduto) {
        String sql = "SELECT * FROM Movimento_Inventario mi " +
                    "JOIN Inventario i ON mi.id_inventario = i.id_inventario " +
                    "WHERE i.id_produto = ? ORDER BY mi.data_registo DESC";
        List<MovimentoInventario> movimentos = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(idProduto));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                MovimentoInventario m = new MovimentoInventario();
                m.setId(rs.getObject("id_movimento", UUID.class).toString());
                m.setTipo(TipoMovimento.valueOf(rs.getString("tipo")));
                m.setQuantidade(rs.getDouble("quantidade"));
                m.setDataRegisto(rs.getTimestamp("data_registo").toLocalDateTime());
                m.setMotivo(rs.getString("motivo"));
                m.setIdInventario(rs.getObject("id_inventario", UUID.class).toString());
                UUID idFuncionario = rs.getObject("id_funcionario", UUID.class);
                m.setIdFuncionario(idFuncionario != null ? idFuncionario.toString() : null);
                movimentos.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting movimentos", e);
        }
        return movimentos;
    }
}
