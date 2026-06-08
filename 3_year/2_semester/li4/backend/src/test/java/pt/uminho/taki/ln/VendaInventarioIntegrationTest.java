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

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class VendaInventarioIntegrationTest {

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

        // Setup base: Loja e Funcionario
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Teste Vendas', '253000000', 'vendas@taki.pt', '500123456', 'Rua B', 'Braga', 'Braga')");
        }

        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador", "op@taki.pt", "Hash123", "OPERADOR", 1);
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
            stmt.execute("TRUNCATE TABLE Fatura CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Movimento_Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Encomenda CASCADE");
            stmt.execute("TRUNCATE TABLE Encomenda CASCADE");
            stmt.execute("TRUNCATE TABLE Produto_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Produto_Fornecedor CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Fornecedor CASCADE");
            stmt.execute("TRUNCATE TABLE Promocao_Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Promocao_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Promocao CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    @Test
    void testVendaAbateStockAutomaticamenteViaTrigger() throws Exception {
        // 1. Criar Produto e Inventario inicial
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "999001", "Cerveja 33cl", "Fresca", 0.40, 1.20, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);

        Inventario inv = new Inventario(UUID.randomUUID().toString(), 100.0, 10.0, 1, idProd);
        inventarioDAO.save(idProd, inv);

        // 2. Realizar Venda de 10 unidades
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 10);
        facade.registarVenda(v, "Numerário");

        // 3. Validar abate no Inventario (100 - 10 = 90)
        Inventario atualizado = inventarioDAO.findById(idProd).orElseThrow();
        System.out.println("DEBUG: Stock atual: " + atualizado.getQuantidade());
        assertEquals(90.0, atualizado.getQuantidade(), "O stock deve ter sido abatido para 90.");
        
        // 4. Validar que foi registado um Movimento de Saída
        var movimentos = inventarioDAO.getMovimentos(idProd);
        assertFalse(movimentos.isEmpty());
        System.out.println("DEBUG: Tipo de movimento: " + movimentos.get(0).getTipo());
        assertEquals("SAIDA", movimentos.get(0).getTipo().name());
        assertEquals(10.0, movimentos.get(0).getQuantidade());
    }

    @Test
    void testVendaFalhaSeStockInsuficiente_TriggerException() throws Exception {
        // 1. Criar Produto e Inventario com pouco stock
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "999002", "Vinho Tinto", "Reserva", 2.00, 15.00, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);

        Inventario inv = new Inventario(UUID.randomUUID().toString(), 5.0, 1.0, 1, idProd);
        inventarioDAO.save(idProd, inv);

        // 2. Tentar vender 10 unidades (temos apenas 5)
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 10);

        // 3. Deve falhar ao registar (o trigger trg_prevencao_stock_negativo deve lancar erro)
        assertThrows(RuntimeException.class, () -> {
            facade.registarVenda(v, "Cartão");
        }, "Deve falhar por stock insuficiente (excecao do Postgres).");

        // 4. Validar que stock permanece 5
        Inventario finalInv = inventarioDAO.findById(idProd).orElseThrow();
        assertEquals(5.0, finalInv.getQuantidade());
    }
}
