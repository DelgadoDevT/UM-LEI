package pt.uminho.taki.dao;

import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import java.sql.*;
import java.util.*;

import pt.uminho.taki.ln.fatura.Fatura;

/**
 * Objeto de Acesso a Dados (DAO) para Venda.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class VendaDAO extends AbstractDAO<String, Venda> {

    /** DAO utilizado para persistir faturas associadas às vendas. */
    private final FaturaDAO faturaDAO;

    /**
     * Constrói uma nova instância de VendaDAO.
     */
    public VendaDAO() {
        this.faturaDAO = new FaturaDAO();
    }

    /**
     * Constrói uma nova instância de VendaDAO com um FaturaDAO especificado.
     * 
     * @param faturaDAO o DAO de fatura
     */
    public VendaDAO(FaturaDAO faturaDAO) {
        this.faturaDAO = Objects.requireNonNull(faturaDAO, "faturaDAO");
    }
    
    /**
     * Guarda uma Venda.
     * 
     * @param key a chave da venda
     * @param venda a venda a guardar
     * @return a venda guardada
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Venda save(String key, Venda venda) {
        return saveComPagamento(key, venda, "Numerário", venda.getTotal(), 0.0);
    }

    /**
     * Guarda uma Venda com um método de pagamento específico.
     * 
     * @param key a chave da venda
     * @param venda a venda a guardar
     * @param metodoPagamento o método de pagamento
     * @param valorPago o valor pago
     * @param troco o troco fornecido
     * @return a venda guardada
     * @throws IllegalStateException lançada se não for possível concluir a venda por falta de stock ou inventário
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Venda saveComPagamento(String key, Venda venda, String metodoPagamento, double valorPago, double troco) {
        boolean isNew = !exists(key);
        String sql = isNew
            ? "INSERT INTO Venda (id_venda, data_hora, subtotal, imposto, total, estado, id_loja, id_funcionario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE Venda SET data_hora=?, subtotal=?, imposto=?, total=?, estado=?, id_loja=?, id_funcionario=? WHERE id_venda=?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (isNew) {
                        ps.setObject(1, UUID.fromString(key));
                        ps.setTimestamp(2, Timestamp.valueOf(venda.getDataHora()));
                        ps.setDouble(3, venda.getSubtotal());
                        ps.setDouble(4, venda.getImposto());
                        ps.setDouble(5, venda.getTotal());
                        ps.setString(6, venda.getEstado());
                        ps.setInt(7, venda.getIdLoja());
                        ps.setObject(8, UUID.fromString(venda.getIdFuncionario()));
                    } else {
                        ps.setTimestamp(1, Timestamp.valueOf(venda.getDataHora()));
                        ps.setDouble(2, venda.getSubtotal());
                        ps.setDouble(3, venda.getImposto());
                        ps.setDouble(4, venda.getTotal());
                        ps.setString(5, venda.getEstado());
                        ps.setInt(6, venda.getIdLoja());
                        ps.setObject(7, UUID.fromString(venda.getIdFuncionario()));
                        ps.setObject(8, UUID.fromString(key));
                    }
                    ps.executeUpdate();
                }
                
                // Guarda as linhas
                if (venda.getLinhas() != null) {
                    saveLinhas(conn, key, venda.getLinhas());
                }

                // Guarda o pagamento (se houver total)
                if (venda.getTotal() > 0) {
                    savePagamento(conn, key, metodoPagamento, valorPago, troco);
                    if (venda.getFatura() == null) {
                        venda.setFatura(new Fatura());
                        venda.getFatura().setIdVenda(key);
                    }
                    generateInvoiceNumberIfNeeded(conn, venda.getFatura());
                    faturaDAO.saveWithConnection(conn, venda.getFatura());
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return venda;
        } catch (SQLException e) {
            String detalhe = e.getMessage() != null ? e.getMessage() : "";
            if (detalhe.contains("Stock não pode ser negativo")
                    || detalhe.contains("trg_prevencao_stock_negativo")
                    || detalhe.contains("column \"id_inventario\"")
                    || detalhe.contains("fk_mov_inventario")) {
                throw new IllegalStateException("Não foi possível concluir a venda por falta de stock/inventário.", e);
            }
            throw new RuntimeException("Error saving Venda", e);
        }
    }

    /**
     * Gera um número de fatura único se este ainda não estiver definido.
     * 
     * @param conn a conexão ativa à base de dados
     * @param fatura o objeto de fatura a processar
     * @throws SQLException se ocorrer um erro durante a consulta à base de dados
     */
    private void generateInvoiceNumberIfNeeded(Connection conn, Fatura fatura) throws SQLException {
        // Verifica se já existe fatura para esta venda
        Optional<Fatura> existing = faturaDAO.findByVendaId(fatura.getIdVenda());
        if (existing.isPresent()) {
            fatura.setNumFatura(existing.get().getNumFatura());
            return;
        }

        int year = fatura.getDataEmissao().getYear();
        int sequence = 1;
        String seqSql = "SELECT COUNT(*) FROM Fatura WHERE EXTRACT(YEAR FROM data_emissao) = ?";
        try (PreparedStatement ps = conn.prepareStatement(seqSql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sequence = rs.getInt(1) + 1;
            }
        }

        if (fatura.getNumFatura() == null || fatura.getNumFatura().isBlank()) {
            fatura.setNumFatura(String.format("FT %d/%d", year, sequence));
        }
    }

    /**
     * Persiste as linhas associadas a uma venda.
     * 
     * @param conn a conexão ativa à base de dados
     * @param idVenda o identificador da venda pai
     * @param linhas a lista de linhas de venda a persistir
     * @throws SQLException se ocorrer um erro durante a inserção em lote
     */
    private void saveLinhas(Connection conn, String idVenda, List<LinhaVenda> linhas) throws SQLException {
        String deleteSql = "DELETE FROM Linha_Venda WHERE id_venda = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setObject(1, UUID.fromString(idVenda));
            ps.executeUpdate();
        }

        String insertSql = "INSERT INTO Linha_Venda (id_linha_venda, quantidade, preco, imposto, subtotal, id_venda, id_produto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (LinhaVenda lv : linhas) {
                ps.setObject(1, UUID.fromString(lv.getIdLinhaVenda()));
                ps.setDouble(2, lv.getQuantidade());
                ps.setDouble(3, lv.getProduto().getPrecoVenda());
                ps.setDouble(4, lv.getProduto().getTaxaIva().getValor());
                ps.setDouble(5, lv.getTotalFinal());
                ps.setObject(6, UUID.fromString(idVenda));
                ps.setObject(7, UUID.fromString(lv.getProduto().getIdProduto()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Persiste ou atualiza a informação de pagamento de uma venda.
     * 
     * @param conn a conexão ativa à base de dados
     * @param idVenda o identificador da venda
     * @param metodoPagamento a designação do método de pagamento
     * @param valorPago o montante entregue pelo cliente
     * @param troco o montante a devolver ao cliente
     * @throws SQLException se ocorrer um erro no acesso à base de dados
     */
    private void savePagamento(Connection conn, String idVenda, String metodoPagamento, double valorPago, double troco) throws SQLException {
        // Verifica se já existe pagamento para esta venda
        String checkSql = "SELECT 1 FROM Pagamento WHERE id_venda = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setObject(1, UUID.fromString(idVenda));
            if (ps.executeQuery().next()) {
                try (PreparedStatement ups = conn.prepareStatement("UPDATE Pagamento SET metodo=?, valor=?, troco=? WHERE id_venda=?")) {
                    ups.setString(1, metodoPagamento);
                    ups.setDouble(2, valorPago);
                    ups.setDouble(3, troco);
                    ups.setObject(4, UUID.fromString(idVenda));
                    ups.executeUpdate();
                }
                return;
            }
        }

        String insertSql = "INSERT INTO Pagamento (id_pagamento, metodo, valor, troco, id_venda) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, metodoPagamento);
            ps.setDouble(3, valorPago);
            ps.setDouble(4, troco);
            ps.setObject(5, UUID.fromString(idVenda));
            ps.executeUpdate();
        }
    }

    /**
     * Procura uma Venda pela sua chave.
     * 
     * @param key a chave da venda
     * @return um Optional que contém a Venda, caso seja encontrada.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Optional<Venda> findById(String key) {
        String sql = "SELECT v.*, f.id_fatura, f.num_fatura, f.data_emissao as fat_data, f.nif_cliente, f.hash, f.hash_control " +
                     "FROM Venda v LEFT JOIN Fatura f ON f.id_venda = v.id_venda WHERE v.id_venda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Venda v = mapResultSet(rs);
                v.setLinhas(getLinhas(key));
                return Optional.of(v);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Venda", e);
        }
    }

    /**
     * Procura todas as Vendas.
     * 
     * @return uma coleção de todas as Vendas
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public Collection<Venda> findAll() {
        String sql = "SELECT v.id_venda, v.data_hora, v.subtotal, v.imposto, v.total, v.estado, v.id_loja, v.id_funcionario, " +
                     "f.id_fatura, f.num_fatura, f.data_emissao as fat_data, f.nif_cliente, f.hash, f.hash_control, " +
                     "lv.id_linha_venda, lv.quantidade, " +
                     "p.id_produto, p.codigo_barras, p.nome, p.descricao, p.preco_custo, p.preco_venda, p.unidade_medida, p.taxa_iva, p.estado as produto_estado " +
                     "FROM Venda v " +
                     "LEFT JOIN Fatura f ON f.id_venda = v.id_venda " +
                     "LEFT JOIN Linha_Venda lv ON lv.id_venda = v.id_venda " +
                     "LEFT JOIN Produto p ON p.id_produto = lv.id_produto " +
                     "ORDER BY v.data_hora DESC";
    
        Map<String, Venda> vendaMap = new LinkedHashMap<>();
    
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
    
            while (rs.next()) {
                String idVenda = rs.getObject("id_venda", UUID.class).toString();
                Venda v = vendaMap.computeIfAbsent(idVenda, k -> {
                    try { return mapResultSet(rs); } catch (SQLException e) { throw new RuntimeException(e); }
                });
    
                String idLinha = null;
                try {
                    UUID idLinhaUUID = rs.getObject("id_linha_venda", UUID.class);
                    if (idLinhaUUID != null) idLinha = idLinhaUUID.toString();
                } catch (SQLException ignored) {}
    
                if (idLinha != null) {
                    Produto produto = new Produto();
                    produto.setIdProduto(rs.getObject("id_produto", UUID.class).toString());
                    produto.setCodigoBarras(rs.getString("codigo_barras"));
                    produto.setNome(rs.getString("nome"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setPrecoCusto(rs.getDouble("preco_custo"));
                    produto.setPrecoVenda(rs.getDouble("preco_venda"));
                    produto.setUnidadeMedida(rs.getString("unidade_medida"));
                    produto.setTaxaIva(TaxaIva.fromValor(rs.getDouble("taxa_iva")));
                    produto.setEstado(rs.getString("produto_estado"));
    
                    LinhaVenda linha = new LinhaVenda(produto, rs.getInt("quantidade"), 0.0);
                    linha.setIdLinhaVenda(idLinha);
    
                    if (v.getLinhas() == null) v.setLinhas(new ArrayList<>());
                    v.getLinhas().add(linha);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Vendas", e);
        }
    
        return vendaMap.values();
    }

    /**
     * Verifica se uma Venda existe pela sua chave.
     * 
     * @param key a chave da venda
     * @return verdadeiro se existir, falso caso contrário.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM Venda WHERE id_venda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Venda existence", e);
        }
    }

    /**
     * Elimina uma Venda pela sua chave.
     * 
     * @param key a chave da venda
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void delete(String key) {
        String sql = "DELETE FROM Venda WHERE id_venda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, UUID.fromString(key));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Venda", e);
        }
    }

    /**
     * Conta o número de Vendas.
     * 
     * @return o número de Vendas
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Venda";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting Vendas", e);
        }
    }

    /**
     * Limpa todas as Vendas.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM Venda";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing Vendas", e);
        }
    }

    /**
     * Efetua o mapeamento de uma linha de um ResultSet para um objeto de domínio Venda.
     * 
     * @param rs o conjunto de resultados da consulta SQL
     * @return o objeto Venda construído com os dados da linha atual
     * @throws SQLException se ocorrer um erro durante o acesso aos dados do ResultSet
     */
    private Venda mapResultSet(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setIdVenda(rs.getObject("id_venda", UUID.class).toString());
        v.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        v.setSubtotal(rs.getDouble("subtotal"));
        v.setImposto(rs.getDouble("imposto"));
        v.setTotal(rs.getDouble("total"));
        v.setEstado(rs.getString("estado"));
        v.setIdLoja(rs.getInt("id_loja"));
        v.setIdFuncionario(rs.getObject("id_funcionario", UUID.class).toString());
        
        try {
            String idFatura = rs.getString("id_fatura");
            if (idFatura != null) {
                Fatura f = new Fatura();
                f.setIdFatura(idFatura);
                f.setNumFatura(rs.getString("num_fatura"));
                f.setDataEmissao(rs.getTimestamp("fat_data").toLocalDateTime());
                f.setNifCliente(rs.getString("nif_cliente"));
                f.setHash(rs.getString("hash"));
                f.setHashControl(rs.getString("hash_control"));
                f.setIdVenda(v.getIdVenda());
                v.setFatura(f);
            }
        } catch (SQLException ignored) {}
        
        return v;
    }

    /**
     * Obtém as linhas de uma Venda.
     * 
     * @param idVenda o identificador da venda
     * @return uma lista de linhas de venda
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<LinhaVenda> getLinhas(String idVenda) {
        String sql = "SELECT lv.id_linha_venda, lv.quantidade, p.id_produto, p.codigo_barras, p.nome, p.descricao, p.preco_custo, p.preco_venda, p.unidade_medida, p.taxa_iva, p.estado " +
                     "FROM Linha_Venda lv JOIN Produto p ON p.id_produto = lv.id_produto WHERE lv.id_venda = ?";
        List<LinhaVenda> linhas = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Produto produto = new Produto();
                produto.setIdProduto(rs.getObject("id_produto", UUID.class).toString());
                produto.setCodigoBarras(rs.getString("codigo_barras"));
                produto.setNome(rs.getString("nome"));
                produto.setDescricao(rs.getString("descricao"));
                produto.setPrecoCusto(rs.getDouble("preco_custo"));
                produto.setPrecoVenda(rs.getDouble("preco_venda"));
                produto.setUnidadeMedida(rs.getString("unidade_medida"));
                produto.setTaxaIva(TaxaIva.fromValor(rs.getDouble("taxa_iva")));
                produto.setEstado(rs.getString("estado"));

                LinhaVenda linha = new LinhaVenda(produto, rs.getInt("quantidade"), 0.0);
                linha.setIdLinhaVenda(rs.getObject("id_linha_venda", UUID.class).toString());
                linhas.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting linhas de venda", e);
        }
        return linhas;
    }

    /**
     * Procura uma Venda pelo seu número de fatura.
     * 
     * @param numeroFatura o número da fatura
     * @return um Optional que contém a Venda, caso seja encontrada.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<Venda> findByNumeroFatura(String numeroFatura) {
        return faturaDAO.findByNumeroFatura(numeroFatura)
                .flatMap(f -> findById(f.getIdVenda()));
    }

    /**
     * Obtém o método de pagamento de uma Venda.
     * 
     * @param idVenda o identificador da venda
     * @return um Optional que contém o método de pagamento, caso seja encontrado.
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Optional<String> getMetodoPagamento(String idVenda) {
        String sql = "SELECT metodo FROM Pagamento WHERE id_venda = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.ofNullable(rs.getString("metodo"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting método de pagamento", e);
        }
    }
}
