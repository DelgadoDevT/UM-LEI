package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.*;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.LinhaVenda;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DevolucaoIntegrationTest {

    private TakiLNLocal facade;
    private ProdutoDAO produtoDAO;
    private InventarioDAO inventarioDAO;
    private FuncionarioDAO funcionarioDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.produtoDAO = new ProdutoDAO();
        this.inventarioDAO = new InventarioDAO();
        this.funcionarioDAO = new FuncionarioDAO();

        // 1. Setup Loja
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Devolucoes', '253000000', 'dev@taki.pt', '500123456', 'Rua E', 'Braga', 'Braga')");
        }

        // 2. Setup Funcionario
        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador Dev", "dev@taki.pt", "Hash123", "OPERADOR", 1);
        funcionarioDAO.save(f.getId(), f);
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanDB();
    }

    private void cleanDB() throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Fila_Sincronizacao RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Devolucao CASCADE");
            stmt.execute("TRUNCATE TABLE Devolucao CASCADE");
            stmt.execute("TRUNCATE TABLE Pagamento CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Movimento_Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    @Test
    void testDevolucaoRepoeStockAutomaticamente() throws Exception {
        // 1. Setup Produto e Inventario
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "DEV001", "Produto Dev", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);
        inventarioDAO.save(idProd, new Inventario(UUID.randomUUID().toString(), 10.0, 2.0, 1, idProd));

        // 2. Realizar Venda de 5 unidades
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 5);
        facade.registarVenda(v, "Numerário");

        // Validar abate (10 - 5 = 5)
        assertEquals(5.0, inventarioDAO.findById(idProd).get().getQuantidade());

        // 3. Realizar Devolução de 2 unidades
        List<LinhaVenda> linhasADevolver = new ArrayList<>();
        // Vamos buscar a linha da venda persistida (na memória do objeto 'v' as linhas estão lá)
        LinhaVenda lv = v.getLinhas().get(0);
        LinhaVenda lvDev = new LinhaVenda(lv);
        lvDev.setQuantidade(2); // Devolver apenas 2 das 5
        linhasADevolver.add(lvDev);

        facade.processarDevolucao(v, linhasADevolver);

        // 4. Validar reposição de stock (5 + 2 = 7)
        Inventario posDev = inventarioDAO.findById(idProd).orElseThrow();
        assertEquals(7.0, posDev.getQuantidade(), "O stock deve ter sido reposto para 7 após a devolução.");

        // 5. Validar movimento de ENTRADA na BD
        var movimentos = inventarioDAO.getMovimentos(idProd);
        // O último movimento deve ser a Entrada por devolução
        boolean entradaFound = movimentos.stream().anyMatch(m -> m.getTipo().name().equals("ENTRADA") && m.getQuantidade() == 2.0);
        assertTrue(entradaFound, "Deve existir um movimento de ENTRADA correspondente à devolução.");
    }
}
