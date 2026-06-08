package pt.uminho.taki.dao;

import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.EstadoConta;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para a gestão de entidades de Funcionário.
 * Implementa a persistência de dados utilizando PostgreSQL.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FuncionarioDAO extends AbstractDAO<String, Funcionario> {
    
    /**
     * Guarda ou atualiza uma entidade de Funcionário na base de dados.
     *
     * @param key o identificador único do funcionário (UUID em formato String)
     * @param func a entidade de funcionário a guardar
     * @return a entidade de funcionário após a persistência
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Funcionario save(String key, Funcionario func) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Funcionario (id_funcionario, nome, email, cargo, password_hash, estado, id_loja) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Funcionario SET nome=?, email=?, cargo=?, password_hash=?, estado=?, id_loja=? WHERE id_funcionario=?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String estadoBD;
                if (func.getEstadoConta() == EstadoConta.BLOQUEADO) estadoBD = "Bloqueado";
                else if (func.getEstadoConta() == EstadoConta.INATIVO) estadoBD = "Inativo";
                else estadoBD = "Ativo";

                if (isNew) {
                    ps.setObject(1, UUID.fromString(key));
                    ps.setString(2, func.getNome());
                    ps.setString(3, func.getEmail());
                    ps.setString(4, func.getIdPerfilAcesso());
                    ps.setString(5, func.getPassword());
                    ps.setString(6, estadoBD);
                    ps.setInt(7, func.getIdLoja());
                } else {
                    ps.setString(1, func.getNome());
                    ps.setString(2, func.getEmail());
                    ps.setString(3, func.getIdPerfilAcesso());
                    ps.setString(4, func.getPassword());
                    ps.setString(5, estadoBD);
                    ps.setInt(6, func.getIdLoja());
                    ps.setObject(7, UUID.fromString(key));
                }
                ps.executeUpdate();
                conn.commit();
                return func;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Funcionario", e);
        }
    }

    /**
     * Procura uma entidade de Funcionário através do seu identificador.
     *
     * @param key o identificador único do funcionário
     * @return um Optional que contém o Funcionário, se encontrado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Funcionario> findById(String key) {
        String sql = "SELECT * FROM Funcionario WHERE id_funcionario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Funcionario", e);
        }
    }

    /**
     * Lista todos os funcionários registados na base de dados.
     *
     * @return uma coleção com todos os funcionários
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Funcionario> findAll() {
        String sql = "SELECT * FROM Funcionario";
        List<Funcionario> funcionarios = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                funcionarios.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Funcionarios", e);
        }
        return funcionarios;
    }

    /**
     * Verifica se existe um funcionário com o identificador fornecido na base de dados.
     *
     * @param key o identificador único do funcionário
     * @return true se o funcionário existir, false caso contrário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Funcionario WHERE id_funcionario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Funcionario existence", e);
        }
    }

    /**
     * Elimina logicamente um funcionário da base de dados, alterando o seu estado para Inativo.
     *
     * @param key o identificador único do funcionário a eliminar
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "UPDATE Funcionario SET estado = 'Inativo' WHERE id_funcionario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Funcionario", e);
        }
    }

    /**
     * Conta o número total de funcionários registados na base de dados.
     *
     * @return o número total de funcionários
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Funcionario";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Funcionarios", e);
        }
    }

    /**
     * Remove fisicamente todos os registos de funcionários da base de dados.
     * Operação destrutiva utilizada principalmente em contexto de limpeza de testes.
     *
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Funcionario";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Funcionarios", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Funcionario.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Funcionario construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Funcionario mapResultSet(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setId(rs.getObject("id_funcionario", UUID.class).toString());
        f.setNome(rs.getString("nome"));
        f.setEmail(rs.getString("email"));
        f.setIdPerfilAcesso(rs.getString("cargo"));
        f.setPassword(rs.getString("password_hash"));
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            if ("Ativo".equalsIgnoreCase(estadoStr)) f.setEstadoConta(EstadoConta.ATIVO);
            else if ("Inativo".equalsIgnoreCase(estadoStr)) f.setEstadoConta(EstadoConta.INATIVO);
            else f.setEstadoConta(EstadoConta.BLOQUEADO);
        }
        f.setIdLoja(rs.getInt("id_loja"));
        return f;
    }
}
