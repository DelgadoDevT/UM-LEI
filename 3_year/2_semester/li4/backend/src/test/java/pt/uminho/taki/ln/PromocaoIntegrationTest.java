package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.*;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.Promocao;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PromocaoIntegrationTest {

    private TakiLNLocal facade;
    private ProdutoDAO produtoDAO;
    private PromocaoDAO promocaoDAO;
    private FuncionarioDAO funcionarioDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.produtoDAO = new ProdutoDAO();
        this.promocaoDAO = new PromocaoDAO();
        this.funcionarioDAO = new FuncionarioDAO();

        // Setup Loja
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Promo', '253000000', 'promo@taki.pt', '500123456', 'Rua F', 'Braga', 'Braga')");
        }

        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador Promo", "promo@taki.pt", "Hash123", "OPERADOR", 1);
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
            stmt.execute("TRUNCATE TABLE Promocao_Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Promocao_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Promocao CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    @Test
    void testVendaComPromocaoProdutoEspecifico() throws Exception {
        // 1. Criar Produto
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "PROM001", "Vinho Promo", "Desc", 5.0, 10.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);
        
        // Criar Inventário para evitar erro no trigger de stock na venda
        new pt.uminho.taki.dao.InventarioDAO().save(idProd, new pt.uminho.taki.ln.inventario.Inventario(UUID.randomUUID().toString(), 100.0, 10.0, 1, idProd));

        // 2. Criar Promoção de 20% para este produto
        String idPromo = UUID.randomUUID().toString();
        Promocao promo = new Promocao(idPromo, "Promo Vinho", 20.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Ativa", 1);
        promocaoDAO.save(idPromo, promo);
        promocaoDAO.addProduto(idPromo, idProd);

        // 3. Realizar Venda
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 1);
        
        // Validação: Preço base 10.0. Desconto 20% = 2.0. Preço c/ desconto = 8.0.
        // Com IVA 23% sobre 8.0 = 1.84. Total = 9.84.
        assertEquals(8.0, v.getLinhas().get(0).getSubtotal(), 0.01);
        assertEquals(9.84, v.getTotal(), 0.01);

        facade.registarVenda(v, "Numerário");
    }

    @Test
    void testVendaComPromocaoGlobal() throws Exception {
        // 1. Criar Produto
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "PROM002", "Pão", "Desc", 0.10, 0.20, TaxaIva.REDUZIDA_6, "un", "Ativo");
        produtoDAO.save(idProd, p);

        // Criar Inventário
        new pt.uminho.taki.dao.InventarioDAO().save(idProd, new pt.uminho.taki.ln.inventario.Inventario(UUID.randomUUID().toString(), 100.0, 10.0, 1, idProd));

        // 2. Criar Promoção Global de 10% (sem filtros de produto/categoria)
        String idPromo = UUID.randomUUID().toString();
        Promocao promo = new Promocao(idPromo, "Promo Tudo", 10.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Ativa", 1);
        promocaoDAO.save(idPromo, promo);

        // 3. Realizar Venda
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 10); // 10 pães @ 0.20 = 2.00
        
        // Desconto 10% de 2.00 = 0.20. Subtotal = 1.80.
        // IVA 6% sobre 1.80 = 0.108. Total = 1.908.
        assertEquals(1.80, v.getSubtotal(), 0.01);
        // O valor total pode variar conforme arredondamento do sistema (1.91 ou 1.908)
        assertTrue(v.getTotal() >= 1.90 && v.getTotal() <= 1.92);
    }

    @Test
    void testVendaComPromocaoPorCategoria() throws Exception {
        // 1. Criar Categoria e Produto
        String idCat = UUID.randomUUID().toString();
        pt.uminho.taki.ln.lojas.Categoria cat = new pt.uminho.taki.ln.lojas.Categoria(idCat, "Limpeza", null);
        new CategoriaDAO().save(idCat, cat);

        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "PROM003", "Detergente", "Desc", 1.0, 3.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);
        produtoDAO.addCategoria(idProd, idCat);
        
        // Criar Inventário
        new pt.uminho.taki.dao.InventarioDAO().save(idProd, new pt.uminho.taki.ln.inventario.Inventario(UUID.randomUUID().toString(), 10.0, 1.0, 1, idProd));

        // 2. Criar Promoção de 50% para a Categoria "Limpeza"
        String idPromo = UUID.randomUUID().toString();
        Promocao promo = new Promocao(idPromo, "Promo Limpeza", 50.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Ativa", 1);
        promocaoDAO.save(idPromo, promo);
        promocaoDAO.addCategoria(idPromo, idCat);

        // 3. Realizar Venda
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 1);

        // Validação: Preço 3.0. Desconto 50% = 1.5. Subtotal = 1.5.
        // IVA 23% sobre 1.5 = 0.345. Total = 1.845.
        assertEquals(1.5, v.getSubtotal(), 0.01);
        assertTrue(v.getTotal() >= 1.84 && v.getTotal() <= 1.85);
    }
}
