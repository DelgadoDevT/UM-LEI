package pt.uminho.taki.dao;

import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para Produto.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoDAO extends AbstractDAO<String, Produto> {

    /**
     * Guarda um Produto.
     *
     * @param key a chave do Produto
     * @param produto o Produto a guardar
     * @return o Produto guardado
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Produto save(String key, Produto produto) {
        boolean isNew = produto.getIdProduto() == null || !exists(key);
        String sql = isNew
            ? "INSERT INTO Produto (id_produto, codigo_barras, nome, descricao, preco_custo, preco_venda, unidade_medida, taxa_iva, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Produto SET codigo_barras=?, nome=?, descricao=?, preco_custo=?, preco_venda=?, unidade_medida=?, taxa_iva=?, estado=? WHERE id_produto=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (isNew) {                ps.setObject(1, UUID.fromString(key));
                ps.setString(2, produto.getCodigoBarras());
                ps.setString(3, produto.getNome());
                ps.setString(4, produto.getDescricao());
                ps.setDouble(5, produto.getPrecoCusto());
                ps.setDouble(6, produto.getPrecoVenda());
                ps.setString(7, produto.getUnidadeMedida());
                ps.setDouble(8, produto.getTaxaIva().getValor());
                ps.setString(9, produto.getEstado());
            } else {
                ps.setString(1, produto.getCodigoBarras());
                ps.setString(2, produto.getNome());
                ps.setString(3, produto.getDescricao());
                ps.setDouble(4, produto.getPrecoCusto());
                ps.setDouble(5, produto.getPrecoVenda());
                ps.setString(6, produto.getUnidadeMedida());
                ps.setDouble(7, produto.getTaxaIva().getValor());
                ps.setString(8, produto.getEstado());
                ps.setObject(9, UUID.fromString(key));
            }
            ps.executeUpdate();
            return produto;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Produto", e);
        }
    }

    /**
     * Procura um Produto pelo seu identificador.
     *
     * @param key o identificador do Produto
     * @return um Optional que contém o Produto, caso seja encontrado, ou vazio em caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Produto> findById(String key) {
        String sql = "SELECT * FROM Produto WHERE id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Produto", e);
        }
    }

    /**
     * Procura um Produto pelo seu código de barras.
     *
     * @param codigoBarras o código de barras do Produto
     * @return um Optional que contém o Produto, caso seja encontrado, ou vazio em caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<Produto> findByCodigoBarras(String codigoBarras) {
        String sql = "SELECT * FROM Produto WHERE codigo_barras = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, codigoBarras);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Produto by codigo_barras", e);
        }
    }

    /**
     * Procura todos os Produtos.
     *
     * @return uma coleção de todos os Produtos
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Produto> findAll() {
        String sql = "SELECT * FROM Produto";
        List<Produto> produtos = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produtos.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Produtos", e);
        }
        return produtos;
    }

    /**
     * Lista produtos com inventário e agregação (se loja=0) ou filtrado por loja.
     *
     * @param idLoja o ID da loja (0 para todas)
     * @return uma lista de mapas contendo os dados dos produtos e inventário
     */
    public List<Map<String, Object>> listarProdutosComInventario(int idLoja) {
        String sqlAll = "SELECT " +
                        "  p.*, " +
                        "  c.designacao as categoria_designacao, " +
                        "  COALESCE(SUM(i.quantidade), 0) as stock_atual, " +
                        "  COALESCE(MIN(i.quantidade_minima), 0) as stock_minimo, " +
                        "  JSON_AGG( " +
                        "    JSON_BUILD_OBJECT( " +
                        "      'idLoja', i.id_loja, " +
                        "      'stockAtual', i.quantidade, " +
                        "      'stockMinimo', i.quantidade_minima " +
                        "    ) " +
                        "  ) FILTER (WHERE i.quantidade <= i.quantidade_minima) as low_stock_lojas " +
                        "FROM Produto p " +
                        "LEFT JOIN Produto_Categoria pc ON pc.id_produto = p.id_produto " +
                        "LEFT JOIN Categoria c ON c.id_categoria = pc.id_categoria " +
                        "LEFT JOIN Inventario i ON i.id_produto = p.id_produto " +
                        "WHERE UPPER(p.estado) = 'ATIVO' " +
                        "GROUP BY p.id_produto, c.designacao";

        String sqlLoja = "SELECT " +
                        "  p.*, " +
                        "  c.designacao as categoria_designacao, " +
                        "  COALESCE(i.quantidade, 0) as stock_atual, " +
                        "  COALESCE(i.quantidade_minima, 0) as stock_minimo, " +
                        "  CASE " +
                        "    WHEN i.quantidade <= i.quantidade_minima " +
                        "    THEN JSON_BUILD_ARRAY(JSON_BUILD_OBJECT( " +
                        "      'idLoja', i.id_loja, " +
                        "      'stockAtual', i.quantidade, " +
                        "      'stockMinimo', i.quantidade_minima " +
                        "    )) " +
                        "    ELSE JSON_BUILD_ARRAY() " +
                        "  END as low_stock_lojas " +
                        "FROM Produto p " +
                        "LEFT JOIN Produto_Categoria pc ON pc.id_produto = p.id_produto " +
                        "LEFT JOIN Categoria c ON c.id_categoria = pc.id_categoria " +
                        "LEFT JOIN Inventario i ON i.id_produto = p.id_produto AND i.id_loja = ? " +
                        "WHERE UPPER(p.estado) = 'ATIVO'";

        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(idLoja == 0 ? sqlAll : sqlLoja)) {
            
            if (idLoja != 0) ps.setInt(1, idLoja);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("idProduto", rs.getObject("id_produto", UUID.class).toString());
                map.put("codigoBarras", rs.getString("codigo_barras"));
                map.put("nome", rs.getString("nome"));
                map.put("descricao", rs.getString("descricao"));
                map.put("precoCusto", rs.getDouble("preco_custo"));
                map.put("precoVenda", rs.getDouble("preco_venda"));
                map.put("unidadeMedida", rs.getString("unidade_medida"));
                map.put("taxaIva", rs.getString("taxa_iva"));
                map.put("estado", rs.getString("estado"));
                map.put("categoriaDesignacao", rs.getString("categoria_designacao"));
                map.put("stockAtual", rs.getDouble("stock_atual"));
                map.put("stockMinimo", rs.getDouble("stock_minimo"));
                String lowStockJson = rs.getString("low_stock_lojas");
                if (lowStockJson != null && !lowStockJson.isBlank()) {
                    try {
                        map.put("lowStockLojas", new com.fasterxml.jackson.databind.ObjectMapper().readValue(lowStockJson, java.util.List.class));
                    } catch (Exception e) {
                        map.put("lowStockLojas", new ArrayList<>());
                    }
                } else {
                    map.put("lowStockLojas", new ArrayList<>());
                }
                lista.add(map);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing products with inventory", e);
        }
        return lista;
    }

    /**
     * Verifica se um Produto existe pelo seu identificador.
     *
     * @param key o identificador do Produto
     * @return verdadeiro se o Produto existir, falso caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Produto WHERE id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Produto existence", e);
        }
    }

    /**
     * Elimina um Produto pelo seu identificador.
     *
     * @param key o identificador do Produto
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "UPDATE Produto SET estado = 'Descontinuado' WHERE id_produto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Produto", e);
        }
    }

    /**
     * Conta o número de Produtos.
     *
     * @return o número total de Produtos
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Produto";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Produtos", e);
        }
    }

    /**
     * Limpa todos os Produtos.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Produto";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Produtos", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Produto.
     *
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Produto construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Produto mapResultSet(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setIdProduto(rs.getObject("id_produto", UUID.class).toString());
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPrecoCusto(rs.getDouble("preco_custo"));
        p.setPrecoVenda(rs.getDouble("preco_venda"));
        p.setUnidadeMedida(rs.getString("unidade_medida"));
        p.setTaxaIva(TaxaIva.fromValor(rs.getDouble("taxa_iva")));
        p.setEstado(rs.getString("estado"));
        return p;
    }

    /**
     * Adiciona uma categoria a um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idCategoria o identificador da categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void addCategoria(String idProduto, String idCategoria) {
        String sql = "INSERT INTO Produto_Categoria (id_produto, id_categoria) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding categoria", e);
        }
    }

    /**
     * Remove uma categoria de um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idCategoria o identificador da categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void removeCategoria(String idProduto, String idCategoria) {
        String sql = "DELETE FROM Produto_Categoria WHERE id_produto = ? AND id_categoria = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idCategoria));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing categoria", e);
        }
    }

    /**
     * Obtém todas as categorias de um Produto.
     *
     * @param idProduto o identificador do Produto
     * @return um conjunto de identificadores de categoria
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Set<String> getCategorias(String idProduto) {
        String sql = "SELECT id_categoria FROM Produto_Categoria WHERE id_produto = ?";
        Set<String> categorias = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categorias.add(rs.getObject("id_categoria", UUID.class).toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting categorias", e);
        }
        return categorias;
    }

    /**
     * Adiciona um fornecedor a um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idFornecedor o identificador do fornecedor
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void addFornecedor(String idProduto, String idFornecedor) {
        String sql = "INSERT INTO Produto_Fornecedor (id_produto, id_fornecedor) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idFornecedor));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding fornecedor", e);
        }
    }

    /**
     * Remove um fornecedor de um Produto.
     *
     * @param idProduto o identificador do Produto
     * @param idFornecedor o identificador do fornecedor
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public void removeFornecedor(String idProduto, String idFornecedor) {
        String sql = "DELETE FROM Produto_Fornecedor WHERE id_produto = ? AND id_fornecedor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ps.setObject(2, UUID.fromString(idFornecedor));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing fornecedor", e);
        }
    }

    /**
     * Obtém todos os fornecedores de um Produto.
     *
     * @param idProduto o identificador do Produto
     * @return um conjunto de identificadores de fornecedor
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Set<String> getFornecedores(String idProduto) {
        String sql = "SELECT id_fornecedor FROM Produto_Fornecedor WHERE id_produto = ?";
        Set<String> fornecedores = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idProduto));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fornecedores.add(rs.getObject("id_fornecedor", UUID.class).toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting fornecedores", e);
        }
        return fornecedores;
    }
}
