package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.*;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.inventario.Inventario;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticsDatabaseIntegrationTest {

    private TakiLNLocal facade;
    private StatisticsDAO statisticsDAO;
    private ProdutoDAO produtoDAO;
    private CategoriaDAO categoriaDAO;
    private FuncionarioDAO funcionarioDAO;
    private InventarioDAO inventarioDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.statisticsDAO = new StatisticsDAO();
        this.produtoDAO = new ProdutoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.funcionarioDAO = new FuncionarioDAO();
        this.inventarioDAO = new InventarioDAO();

        // 1. Setup Loja
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Analytics', '253000000', 'stats@taki.pt', '500123456', 'Rua D', 'Braga', 'Braga')");
        }

        // 2. Setup Funcionario
        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Analista", "stats@taki.pt", "Hash123", "GERENTE", 1);
        funcionarioDAO.save(f.getId(), f);

        // 3. Setup Categorias
        Categoria catBebidas = new Categoria(UUID.randomUUID().toString(), "Bebidas", null);
        categoriaDAO.save(catBebidas.getIdCategoria(), catBebidas);

        // 4. Setup Produtos e Inventario
        String idProd1 = UUID.randomUUID().toString();
        Produto p1 = new Produto(idProd1, "STAT001", "Agua 0.5L", "Agua Mineral", 0.10, 0.50, TaxaIva.REDUZIDA_6, "un", "Ativo");
        produtoDAO.save(idProd1, p1);
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Produto_Categoria (id_produto, id_categoria) VALUES ('" + idProd1 + "', '" + catBebidas.getIdCategoria() + "')");
        }
        inventarioDAO.save(idProd1, new Inventario(UUID.randomUUID().toString(), 100.0, 10.0, 1, idProd1));

        String idProd2 = UUID.randomUUID().toString();
        Produto p2 = new Produto(idProd2, "STAT002", "Sumo 1L", "Sumo Natural", 0.30, 1.50, TaxaIva.REDUZIDA_6, "un", "Ativo");
        produtoDAO.save(idProd2, p2);
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Produto_Categoria (id_produto, id_categoria) VALUES ('" + idProd2 + "', '" + catBebidas.getIdCategoria() + "')");
        }
        inventarioDAO.save(idProd2, new Inventario(UUID.randomUUID().toString(), 50.0, 5.0, 1, idProd2));
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanDB();
    }

    private void cleanDB() throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Fila_Sincronizacao RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE Pagamento CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Movimento_Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Produto_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    @Test
    void testKPIsDiariosEValorizacaoEstoque() throws Exception {
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Produto p1 = produtoDAO.findByCodigoBarras("STAT001").orElseThrow();
        Produto p2 = produtoDAO.findByCodigoBarras("STAT002").orElseThrow();

        // 1. Realizar Vendas
        // Venda 1: 10 Aguas (10 * 0.50 = 5.00 + IVA)
        Venda v1 = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v1, p1, 10);
        facade.registarVenda(v1, "Numerário");

        // Venda 2: 2 Sumos (2 * 1.50 = 3.00 + IVA)
        Venda v2 = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v2, p2, 2);
        facade.registarVenda(v2, "Cartão");

        // 2. Validar Dashboard KPI Diário
        Map<String, Object> kpis = statisticsDAO.getKpiDiario(1);
        assertFalse(kpis.isEmpty());
        assertEquals(2, kpis.get("contagemVendasDia"));
        
        double totalEsperado = v1.getTotal() + v2.getTotal();
        assertEquals(totalEsperado, (double) kpis.get("totalFaturadoDia"), 0.01);

        // 3. Validar Desempenho por Categoria
        List<Map<String, Object>> desempenho = statisticsDAO.getDesempenhoCategorias(1);
        assertFalse(desempenho.isEmpty());
        assertEquals("Bebidas", desempenho.get(0).get("categoria"));
        assertEquals(totalEsperado, (double) desempenho.get(0).get("totalFaturado"), 0.01);

        // 4. Validar Valorização de Estoque (func_valorizacao_estoque)
        // Stock P1: 100 - 10 = 90. Custo = 0.10. Valor = 9.00
        // Stock P2: 50 - 2 = 48. Custo = 0.30. Valor = 14.40
        // Total = 9.00 + 14.40 = 23.40
        double valorizacao = statisticsDAO.getValorizacaoEstoque(1);
        assertEquals(23.40, valorizacao, 0.01);
    }
}
