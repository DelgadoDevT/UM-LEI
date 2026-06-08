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
import pt.uminho.taki.ln.vendas.Promocao;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PromocaoHierarquiaIntegrationTest {

    private TakiLNLocal facade;
    private ProdutoDAO produtoDAO;
    private PromocaoDAO promocaoDAO;
    private CategoriaDAO categoriaDAO;
    private FuncionarioDAO funcionarioDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.produtoDAO = new ProdutoDAO();
        this.promocaoDAO = new PromocaoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.funcionarioDAO = new FuncionarioDAO();

        // Setup Loja
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Hierarquia', '253000000', 'hie@taki.pt', '500123456', 'Rua G', 'Braga', 'Braga')");
        }

        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador Hie", "hie@taki.pt", "Hash123", "OPERADOR", 1);
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
            stmt.execute("TRUNCATE TABLE Produto_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Inventario CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    @Test
    void testVendaComPromocaoHerdadaDaCategoriaPai() throws Exception {
        // 1. Criar Hierarquia de Categorias: Bebidas -> Alcoolicas -> Vinhos
        String idBebidas = UUID.randomUUID().toString();
        categoriaDAO.save(idBebidas, new Categoria(idBebidas, "Bebidas", null));

        String idAlcoolicas = UUID.randomUUID().toString();
        categoriaDAO.save(idAlcoolicas, new Categoria(idAlcoolicas, "Alcoólicas", idBebidas));

        String idVinhos = UUID.randomUUID().toString();
        categoriaDAO.save(idVinhos, new Categoria(idVinhos, "Vinhos", idAlcoolicas));

        // 2. Criar Produto na categoria "Vinhos"
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "HIE001", "Vinho Tinto Reserva", "Desc", 10.0, 20.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);
        produtoDAO.addCategoria(idProd, idVinhos);
        
        // Criar Inventário
        new InventarioDAO().save(idProd, new pt.uminho.taki.ln.inventario.Inventario(UUID.randomUUID().toString(), 50.0, 5.0, 1, idProd));

        // 3. Criar Promoção de 25% na categoria de topo "Bebidas"
        String idPromo = UUID.randomUUID().toString();
        Promocao promo = new Promocao(idPromo, "Promo Bebidas Verão", 25.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Ativa", 1);
        promocaoDAO.save(idPromo, promo);
        promocaoDAO.addCategoria(idPromo, idBebidas);

        // 4. Realizar Venda e validar aplicação do desconto (mesmo sendo categoria avó)
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 1);

        // Validação: Preço 20.0. Desconto 25% = 5.0. Subtotal = 15.0.
        // IVA 23% sobre 15.0 = 3.45. Total = 18.45.
        assertEquals(15.0, v.getSubtotal(), 0.01, "O desconto da categoria pai deve ser aplicado ao produto da subcategoria.");
        assertEquals(18.45, v.getTotal(), 0.01);
    }
}
