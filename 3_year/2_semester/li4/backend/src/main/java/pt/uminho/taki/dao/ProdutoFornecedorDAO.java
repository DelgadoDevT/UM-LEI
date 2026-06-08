package pt.uminho.taki.dao;

import pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Objeto de Acesso a Dados (DAO) para ProdutoFornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoFornecedorDAO extends AbstractDAO<String, ProdutoFornecedor> {

    /**
     * Guarda uma associação ProdutoFornecedor.
     *
     * @param key a chave (ignorada)
     * @param pf o ProdutoFornecedor a guardar
     * @return o ProdutoFornecedor guardado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public ProdutoFornecedor save(String key, ProdutoFornecedor pf) {
        // Para este DAO, a chave é uma composta de idProduto e idFornecedor,
        // mas o AbstractDAO apenas suporta uma chave única. Vamos ignorar o parâmetro key
        // e usar a chave composta do próprio objeto.
        String sql;
        boolean isNew = !exists(pf.getIdProduto(), pf.getIdFornecedor());

        if (isNew) {
            sql = "INSERT INTO Produto_Fornecedor (id_produto, id_fornecedor, preco_custo, preferencial) VALUES (?, ?, ?, ?)";
        } else {
            sql = "UPDATE Produto_Fornecedor SET preco_custo=?, preferencial=? WHERE id_produto=? AND id_fornecedor=?";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (isNew) {
                ps.setObject(1, UUID.fromString(pf.getIdProduto()));
                ps.setObject(2, UUID.fromString(pf.getIdFornecedor()));
                ps.setDouble(3, pf.getPrecoCusto());
                ps.setBoolean(4, pf.isPreferencial());
            } else {
                ps.setDouble(1, pf.getPrecoCusto());
                ps.setBoolean(2, pf.isPreferencial());
                ps.setObject(3, UUID.fromString(pf.getIdProduto()));
                ps.setObject(4, UUID.fromString(pf.getIdFornecedor()));
            }
            ps.executeUpdate();
            return pf;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving ProdutoFornecedor", e);
        }
    }


    /**
     * Verifica se existe uma associação ProdutoFornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @return verdadeiro se a associação existir, falso caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public boolean exists(String idProduto, String idFornecedor) {
        String sql = "SELECT 1 FROM Produto_Fornecedor WHERE id_produto = ? AND id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idFornecedor));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if ProdutoFornecedor exists", e);
        }
    }


    /**
     * Procura associações pelo identificador do produto.
     *
     * @param idProduto o identificador do produto
     * @return uma lista de associações
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<ProdutoFornecedor> findByIdProduto(String idProduto) {
        List<ProdutoFornecedor> result = new ArrayList<>();
        String sql = "SELECT * FROM Produto_Fornecedor WHERE id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ProdutoFornecedor by idProduto", e);
        }
        return result;
    }

    /**
     * Procura uma associação pelo identificador do produto e identificador do fornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @return um Optional que contém a associação, caso seja encontrada, ou vazio em caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<ProdutoFornecedor> findByIdProdutoAndIdFornecedor(String idProduto, String idFornecedor) {
        String sql = "SELECT * FROM Produto_Fornecedor WHERE id_produto = ? AND id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idFornecedor));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ProdutoFornecedor by idProduto and idFornecedor", e);
        }
        return Optional.empty();
    }

    /**
     * Procura associações pelo identificador do fornecedor.
     *
     * @param idFornecedor o identificador do fornecedor
     * @return uma lista de associações
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<ProdutoFornecedor> findByIdFornecedor(String idFornecedor) {
        List<ProdutoFornecedor> result = new ArrayList<>();
        String sql = "SELECT * FROM Produto_Fornecedor WHERE id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idFornecedor));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ProdutoFornecedor by idFornecedor", e);
        }
        return result;
    }

    /**
     * Elimina uma associação pelo identificador do produto e identificador do fornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void deleteAssociation(String idProduto, String idFornecedor) {
        String sql = "DELETE FROM Produto_Fornecedor WHERE id_produto = ? AND id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idFornecedor));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting ProdutoFornecedor association", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio ProdutoFornecedor.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto ProdutoFornecedor construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private ProdutoFornecedor mapResultSet(ResultSet rs) throws SQLException {
        return new ProdutoFornecedor(
                rs.getObject("id_produto", UUID.class).toString(),
                rs.getObject("id_fornecedor", UUID.class).toString(),
                rs.getDouble("preco_custo"),
                rs.getBoolean("preferencial")
        );
    }

    // Os seguintes métodos do AbstractDAO não estão implementados uma vez que não fazem sentido
    // para um DAO de chave composta da mesma forma.

    /**
     * Não suportado.
     *
     * @param key a chave
     * @return lança UnsupportedOperationException
     */
    @Override
    public Optional<ProdutoFornecedor> findById(String key) {
        throw new UnsupportedOperationException("findById is not supported, use findByIdProdutoAndIdFornecedor instead.");
    }

    /**
     * Não suportado.
     *
     * @return lança UnsupportedOperationException
     */
    @Override
    public Collection<ProdutoFornecedor> findAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Não suportado.
     *
     * @param key a chave
     * @return lança UnsupportedOperationException
     */
    @Override
    public boolean exists(String key) {
        throw new UnsupportedOperationException("exists is not supported, use exists(idProduto, idFornecedor) instead.");
    }

    /**
     * Não suportado.
     *
     * @param key a chave
     */
    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("Use deleteAssociation(idProduto, idFornecedor).");
    }

    /**
     * Não suportado.
     *
     * @return lança UnsupportedOperationException
     */
    @Override
    public int count() {
        throw new UnsupportedOperationException();
    }

    /**
     * Não suportado.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
