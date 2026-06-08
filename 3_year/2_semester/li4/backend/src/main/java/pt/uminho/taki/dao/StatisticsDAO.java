package pt.uminho.taki.dao;

import java.sql.*;
import java.util.*;

/**
 * Objeto de Acesso a Dados (DAO) para Estatísticas.
 * @author TakiLN Team
 * @since 1.0
 */
public class StatisticsDAO {

    /**
     * Devolve uma ligação ativa à base de dados através do gestor de ligações.
     * @return a ligação ativa à base de dados
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     * @throws SQLException em caso de erro na obtenção da ligação
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionManager.getConnection();
    }

    // Vista: View_Dashboard_Kpi_Diario
    /**
     * Obtém os indicadores de desempenho (KPI) diários de uma loja.
     * @param idLoja o identificador da loja
     * @return mapa com as chaves: {@code idLoja} (identificador da loja),
     *         {@code contagemVendasDia} (número de vendas do dia),
     *         {@code totalFaturadoDia} (valor total faturado no dia)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public Map<String, Object> getKpiDiario(int idLoja) {
        String sql = "SELECT * FROM View_Dashboard_Kpi_Diario WHERE id_loja = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idLoja);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> kpi = new HashMap<>();
                kpi.put("idLoja", rs.getInt("id_loja"));
                kpi.put("contagemVendasDia", rs.getInt("contagem_vendas_dia"));
                kpi.put("totalFaturadoDia", rs.getDouble("total_faturado_dia"));
                return kpi;
            }
            return Collections.emptyMap();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting KPI diario", e);
        }
    }

    /**
     * Obtém KPIs comparativos entre hoje e ontem.
     * @param idLoja o identificador da loja (0 para todas)
     * @return mapa com dados de hoje e ontem
     */
    public Map<String, Object> getKpiComparativo(int idLoja) {
        String filter = idLoja == 0 ? "" : " AND id_loja = " + idLoja;
        String sql = "SELECT " +
                     "  SUM(CASE WHEN DATE(data_hora) = CURRENT_DATE THEN total ELSE 0 END) as receita_hoje, " +
                     "  COUNT(CASE WHEN DATE(data_hora) = CURRENT_DATE THEN 1 END) as vendas_hoje, " +
                     "  SUM(CASE WHEN DATE(data_hora) = CURRENT_DATE - INTERVAL '1 day' THEN total ELSE 0 END) as receita_ontem, " +
                     "  COUNT(CASE WHEN DATE(data_hora) = CURRENT_DATE - INTERVAL '1 day' THEN 1 END) as vendas_ontem " +
                     "FROM Venda " +
                     "WHERE estado = 'Concluída' AND (DATE(data_hora) = CURRENT_DATE OR DATE(data_hora) = CURRENT_DATE - INTERVAL '1 day')" +
                     filter;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> res = new HashMap<>();
                double rHoje = rs.getDouble("receita_hoje");
                double rOntem = rs.getDouble("receita_ontem");
                
                // Subtrai devoluções
                rHoje -= getTotalDevolucoesHoje(idLoja);
                rOntem -= getTotalDevolucoesOntem(idLoja);
                
                res.put("receitaHoje", Math.max(0.0, rHoje));
                res.put("vendasHoje", rs.getInt("vendas_hoje"));
                res.put("receitaOntem", Math.max(0.0, rOntem));
                res.put("vendasOntem", rs.getInt("vendas_ontem"));
                return res;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting KPI comparativo", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Obtém o valor total das devoluções efetuadas hoje.
     * @param idLoja o identificador da loja (0 para todas)
     * @return o valor total das devoluções
     */
    public double getTotalDevolucoesHoje(int idLoja) {
        return getTotalDevolucoesPorData(idLoja, "CURRENT_DATE");
    }

    /**
     * Obtém o valor total das devoluções efetuadas ontem.
     * @param idLoja o identificador da loja (0 para todas)
     * @return o valor total das devoluções
     */
    public double getTotalDevolucoesOntem(int idLoja) {
        return getTotalDevolucoesPorData(idLoja, "CURRENT_DATE - INTERVAL '1 day'");
    }

    private double getTotalDevolucoesPorData(int idLoja, String dataSql) {
        String filter = idLoja == 0 ? "" : " AND v.id_loja = " + idLoja;
        String sql = "SELECT COALESCE(SUM(d.valor), 0) FROM Devolucao d " +
                     "JOIN Venda v ON d.id_venda = v.id_venda " +
                     "WHERE DATE(d.data_hora) = " + dataSql + filter;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total devolucoes", e);
        }
        return 0.0;
    }

    /**
     * Obtém os indicadores de desempenho (KPI) diários de todas as lojas.
     * @return uma lista de mapas contendo os KPIs diários de todas as lojas da rede
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getKpiDiarioTodasLojas() {
        String sql = "SELECT * FROM View_Dashboard_Kpi_Diario";
        List<Map<String, Object>> kpis = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> kpi = new HashMap<>();
                kpi.put("idLoja", rs.getInt("id_loja"));
                kpi.put("contagemVendasDia", rs.getInt("contagem_vendas_dia"));
                kpi.put("totalFaturadoDia", rs.getDouble("total_faturado_dia"));
                kpis.add(kpi);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting KPI diario todas lojas", e);
        }
        return kpis;
    }

    // Vista: View_Alertas_StockCritico
    /**
     * Obtém os alertas de stock crítico de uma loja.
     * @param idLoja o identificador da loja
     * @return uma lista de mapas com as chaves: {@code idLoja} (identificador da loja),
     *         {@code codigoBarras} (código de barras do produto),
     *         {@code produto} (nome do produto),
     *         {@code stockAtual} (quantidade atual em stock),
     *         {@code limiteMinimo} (limite mínimo configurado)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getAlertasStockCritico(int idLoja) {
        String sql = "SELECT * FROM View_Alertas_StockCritico WHERE id_loja = ?";
        List<Map<String, Object>> alertas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idLoja);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("idLoja", rs.getInt("id_loja"));
                alerta.put("codigoBarras", rs.getString("codigo_barras"));
                alerta.put("produto", rs.getString("produto"));
                alerta.put("stockAtual", rs.getInt("stock_atual"));
                alerta.put("limiteMinimo", rs.getInt("limite_minimo"));
                alertas.add(alerta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting alertas stock critico", e);
        }
        return alertas;
    }

    /**
     * Obtém os alertas de stock crítico de todas as lojas.
     * @return uma lista de mapas com as chaves: {@code idLoja} (identificador da loja),
     *         {@code codigoBarras} (código de barras do produto),
     *         {@code produto} (nome do produto),
     *         {@code stockAtual} (quantidade atual em stock),
     *         {@code limiteMinimo} (limite mínimo configurado)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getAlertasStockCriticoTodasLojas() {
        String sql = "SELECT * FROM View_Alertas_StockCritico";
        List<Map<String, Object>> alertas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("idLoja", rs.getInt("id_loja"));
                alerta.put("codigoBarras", rs.getString("codigo_barras"));
                alerta.put("produto", rs.getString("produto"));
                alerta.put("stockAtual", rs.getInt("stock_atual"));
                alerta.put("limiteMinimo", rs.getInt("limite_minimo"));
                alertas.add(alerta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting alertas stock critico todas lojas", e);
        }
        return alertas;
    }

    // Vista: View_Desempenho_Categorias
    /**
     * Obtém o desempenho das categorias.
     * @param idLoja o identificador da loja
     * @return uma lista de mapas com as chaves: {@code idLoja} (identificador da loja, opcional),
     *         {@code categoria} (nome da categoria),
     *         {@code totalFaturado} (total faturado pela categoria)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getDesempenhoCategorias(int idLoja) {
        String sql = idLoja == 0 ? "SELECT categoria, SUM(total_faturado) as total_faturado FROM View_Desempenho_Categorias GROUP BY categoria ORDER BY total_faturado DESC"
                                 : "SELECT * FROM View_Desempenho_Categorias WHERE id_loja = ? ORDER BY total_faturado DESC";
        List<Map<String, Object>> desempenho = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (idLoja != 0) ps.setInt(1, idLoja);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                if (idLoja != 0) cat.put("idLoja", rs.getInt("id_loja"));
                cat.put("categoria", rs.getString("categoria"));
                cat.put("totalFaturado", rs.getDouble("total_faturado"));
                desempenho.add(cat);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting desempenho categorias", e);
        }
        return desempenho;
    }

    /**
     * Obtém as vendas mensais.
     * @param idLoja o identificador da loja
     * @param meses o número de meses
     * @return uma lista de mapas com as chaves: {@code mes} (abreviatura do mês),
     *         {@code totalVendas} (valor total faturado),
     *         {@code totalTransacoes} (número de vendas realizadas)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getVendasMensais(int idLoja, int meses) {
        String filter = idLoja == 0 ? "" : " AND id_loja = " + idLoja;
        String sql = "SELECT TO_CHAR(data_hora, 'Mon') as mes, " +
                     "SUM(total) as total_vendas, COUNT(*) as total_transacoes " +
                     "FROM Venda " +
                     "WHERE data_hora >= CURRENT_DATE - INTERVAL '" + meses + " months' " +
                     filter + " " +
                     "GROUP BY TO_CHAR(data_hora, 'Mon'), DATE_TRUNC('month', data_hora) " +
                     "ORDER BY DATE_TRUNC('month', data_hora)";
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("mes", rs.getString("mes"));
                r.put("totalVendas", rs.getDouble("total_vendas"));
                r.put("totalTransacoes", rs.getInt("total_transacoes"));
                resultados.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting monthly sales", e);
        }
        return resultados;
    }

    /**
     * Obtém as vendas por hora.
     * @param idLoja o identificador da loja
     * @return uma lista de mapas com as chaves: {@code hora} (hora do dia no formato "Hh"),
     *         {@code totalClientes} (número de vendas/clientes atendidos)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public List<Map<String, Object>> getVendasPorHora(int idLoja) {
        String filter = idLoja == 0 ? "" : " WHERE id_loja = " + idLoja;
        String sql = "SELECT EXTRACT(HOUR FROM data_hora) as hora, COUNT(*) as total_clientes " +
                     "FROM Venda " +
                     filter + " " +
                     "GROUP BY EXTRACT(HOUR FROM data_hora) " +
                     "ORDER BY hora";
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("hora", rs.getInt("hora") + "h");
                r.put("totalClientes", rs.getInt("total_clientes"));
                resultados.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting hourly traffic", e);
        }
        return resultados;
    }

    // Função: func_valorizacao_estoque
    /**
     * Obtém a valorização do stock de uma loja.
     * @param idLoja o identificador da loja
     * @return o valor total financeiro do stock em armazém (preço de custo x quantidade)
     * @throws RuntimeException se ocorrer um erro de acesso à base de dados.
     */
    public double getValorizacaoEstoque(int idLoja) {
        String sql = idLoja == 0
            ? "SELECT COALESCE(SUM(i.quantidade * p.preco_custo), 0) FROM Inventario i JOIN Produto p ON i.id_produto = p.id_produto"
            : "SELECT func_valorizacao_estoque(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (idLoja != 0) ps.setInt(1, idLoja);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting valorizacao estoque", e);
        }
    }
}
