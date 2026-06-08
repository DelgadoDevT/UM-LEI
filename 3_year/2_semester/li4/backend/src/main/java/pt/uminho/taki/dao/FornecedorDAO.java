package pt.uminho.taki.dao;

import pt.uminho.taki.ln.fornecimentos.Fornecedor;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para entidades de Fornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FornecedorDAO extends AbstractDAO<String, Fornecedor> {
    
    /**
     * Guarda um Fornecedor na base de dados.
     *
     * @param key a chave que identifica o fornecedor
     * @param fornecedor o fornecedor a guardar
     * @return o fornecedor guardado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Fornecedor save(String key, Fornecedor fornecedor) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Fornecedor (id_fornecedor, nome, nif, telefone, email, estado) VALUES (?, ?, ?, ?, ?, ?)"
            : "UPDATE Fornecedor SET nome=?, nif=?, telefone=?, email=?, estado=? WHERE id_fornecedor=?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (isNew) {
                ps.setObject(1, UUID.fromString(key));
                ps.setString(2, fornecedor.getNome());
                ps.setString(3, fornecedor.getNif());
                ps.setString(4, fornecedor.getTelefone());
                ps.setString(5, fornecedor.getEmail());
                ps.setString(6, fornecedor.getEstado());
            } else {
                ps.setString(1, fornecedor.getNome());
                ps.setString(2, fornecedor.getNif());
                ps.setString(3, fornecedor.getTelefone());
                ps.setString(4, fornecedor.getEmail());
                ps.setString(5, fornecedor.getEstado());
                ps.setObject(6, UUID.fromString(key));
            }
            ps.executeUpdate();
            return fornecedor;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Fornecedor", e);
        }
    }

    /**
     * Procura um Fornecedor pelo seu identificador.
     *
     * @param key o identificador do fornecedor
     * @return um Optional que contém o fornecedor encontrado, ou vazio caso não seja encontrado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Optional<Fornecedor> findById(String key) {
        String sql = "SELECT * FROM Fornecedor WHERE id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Fornecedor", e);
        }
    }

    /**
     * Procura um Fornecedor pelo seu NIF.
     *
     * @param nif o NIF do fornecedor
     * @return um Optional que contém o fornecedor encontrado, ou vazio caso não seja encontrado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    public Optional<Fornecedor> findByNif(String nif) {
        String sql = "SELECT * FROM Fornecedor WHERE nif = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nif);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Fornecedor by NIF", e);
        }
    }

    /**
     * Verifica se um Fornecedor tem encomendas pendentes.
     *
     * @param idFornecedor o identificador do fornecedor
     * @return true se o fornecedor tiver encomendas pendentes, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    public boolean hasEncomendasPendentes(String idFornecedor) {
        String sql = "SELECT 1 FROM Encomenda WHERE id_fornecedor = ? AND estado IN ('Rascunho', 'Pendente', 'Enviada', 'Em Trânsito') LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idFornecedor));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking pending encomendas for fornecedor", e);
        }
    }

    /**
     * Procura todos os Fornecedores.
     *
     * @return uma coleção de todos os fornecedores
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public Collection<Fornecedor> findAll() {
        String sql = "SELECT * FROM Fornecedor";
        List<Fornecedor> fornecedores = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                fornecedores.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Fornecedores", e);
        }
        return fornecedores;
    }

    /**
     * Verifica se um Fornecedor existe pelo seu identificador.
     *
     * @param key o identificador do fornecedor
     * @return true se o fornecedor existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Fornecedor WHERE id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Fornecedor existence", e);
        }
    }

    /**
     * Elimina um Fornecedor através da definição do seu estado como inativo.
     *
     * @param key o identificador do fornecedor a eliminar
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void delete(String key) {
        String sql = "UPDATE Fornecedor SET estado = 'Inativo' WHERE id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Fornecedor", e);
        }
    }

    /**
     * Conta o número total de Fornecedores.
     *
     * @return o número de fornecedores
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Fornecedor";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Fornecedores", e);
        }
    }

    /**
     * Elimina todos os Fornecedores da base de dados.
     *
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Fornecedor";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Fornecedores", e);
        }
    }

    /**
     * Mapeia uma linha de ResultSet para um objeto Fornecedor.
     *
     * @param rs o ResultSet a mapear
     * @return o Fornecedor mapeado
     * @throws SQLException se ocorrer um erro de acesso à base de dados
     */
    private Fornecedor mapResultSet(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(rs.getObject("id_fornecedor", UUID.class).toString());
        f.setNome(rs.getString("nome"));
        f.setNif(rs.getString("nif"));
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));
        f.setEstado(rs.getString("estado"));
        return f;
    }
}
