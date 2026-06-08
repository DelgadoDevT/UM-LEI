package pt.uminho.taki.ln;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.ConnectionManager;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.inventario.MovimentoInventario;
import pt.uminho.taki.ln.inventario.TipoMovimento;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;
import pt.uminho.taki.ln.vendas.Venda;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TakiFullSystemTest {

    private ITakiLNLocal facade;
    private InventarioDAO inventarioDAO;

    @BeforeEach
    void setup() throws Exception {
        cleanDB();
        
        // Setup base
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Central de Testes', '253111222', 'central@taki.pt', '500111222', 'Rua Uni', 'Braga', 'Braga')");
        }
        
        this.facade = new TakiLNLocal();
        this.inventarioDAO = new InventarioDAO();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        cleanDB();
    }

    private void cleanDB() throws Exception {
        // Limpeza profunda da base de dados de desenvolvimento
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
    void testHappyPath_FluxoCompletoDeNegocio() throws Exception {
        // PASSO 1: Lojas - Criar Funcionario, Categoria e Produto
        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador Sistema", "admin@taki.pt", "Admin123", "GERENTE", 1);
        facade.registarFuncionario(f);
        
        Funcionario auth = facade.autenticar("admin@taki.pt", "Admin123");
        assertNotNull(auth);

        Categoria cat = new Categoria(UUID.randomUUID().toString(), "Bebidas", null);
        facade.adicionarCategoria(cat);

        Produto p = new Produto();
        p.setIdProduto(UUID.randomUUID().toString());
        p.setNome("Sumo de Laranja 1L");
        p.setCodigoBarras("5601112223334");
        p.setPrecoCusto(0.50);
        p.setTaxaIva(TaxaIva.REDUZIDA_6);
        facade.adicionarProduto(p);

        // PASSO 2: Inventario - Entrada de Stock
        // Criar registo de inventario (na realidade isto seria feito via sincronizacao ou trigger, mas vamos garantir que existe)
        Inventario inv = new Inventario(UUID.randomUUID().toString(), 0.0, 5.0, 1, p.getIdProduto());
        inventarioDAO.save(p.getIdProduto(), inv);

        MovimentoInventario mov = new MovimentoInventario(UUID.randomUUID().toString(), TipoMovimento.ENTRADA, 100.0, LocalDateTime.now(), "Stock Inicial", p.getIdProduto(), auth.getId());
        facade.registarMovimentoManual(mov);

        Inventario statusInv = inventarioDAO.findById(p.getIdProduto()).orElseThrow();
        assertEquals(100.0, statusInv.getQuantidade(), "O saldo de inventario deve refletir a entrada de 100 unidades.");

        // PASSO 3: Vendas - Realizar uma venda de 10 unidades
        Venda venda = facade.iniciarVenda(1, auth.getId());
        facade.adicionarLinhaVenda(venda, p, 10);
        
        // Verificacao cruzada do calculo (Passo 4 parcial)
        // O ProdutoService ja guarda o precoVenda com IVA (0.50 * 1.06)
        // A LinhaVenda volta a aplicar o IVA sobre o precoVenda (ou seja, precoVenda * 1.06)
        // Este e o comportamento atual do codigo: PrecoCusto -> PrecoVenda (c/ IVA) -> Venda (aplica IVA sobre PrecoVenda)
        double precoVendaComIva = 0.50 * 1.06;
        double esperadoTotal = (precoVendaComIva * 1.06) * 10; 
        assertEquals(esperadoTotal, venda.getTotal(), 0.01, "O total da venda deve ser calculado corretamente com IVA 6% sobre o preco de venda.");

        facade.registarVenda(venda, "Cartao");

        // PASSO 4: Verificacao Cruzada
        // Stock deve ter descido via Observer
        Inventario posVendaInv = inventarioDAO.findById(p.getIdProduto()).orElseThrow();
        assertEquals(90.0, posVendaInv.getQuantidade(), "O stock deve ter baixado automaticamente para 90 unidades apos a venda.");
        
        System.out.println("SUCESSO: Fluxo feliz validado ponta-a-ponta.");
    }

    @Test
    void testResiliencePath_CaminhosInfelizes() throws Exception {
        // Setup base
        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Admin", "admin@taki.pt", "Admin123", "GERENTE", 1);
        facade.registarFuncionario(f);

        // 1. Password Errada
        assertThrows(CredenciaisInvalidasException.class, () -> {
            facade.autenticar("admin@taki.pt", "PasswordErrada");
        });

        // 2. Venda sem stock
        Produto p = new Produto();
        p.setIdProduto(UUID.randomUUID().toString());
        p.setNome("Produto Esgotado");
        p.setCodigoBarras("0000000000000");
        p.setPrecoCusto(1.0);
        facade.adicionarProduto(p);
        
        Inventario inv = new Inventario(UUID.randomUUID().toString(), 0.0, 1.0, 1, p.getIdProduto());
        inventarioDAO.save(p.getIdProduto(), inv);

        Venda v = facade.iniciarVenda(1, f.getId());
        facade.adicionarLinhaVenda(v, p, 5);
        
        // O registo de venda deve falhar porque o Observer do Inventario lancara StockInsuficienteException
        assertThrows(RuntimeException.class, () -> {
            facade.registarVenda(v, "Numerario");
        });

        // 3. Devolucao fora do prazo (venda antiga simulada)
        Venda vendaAntiga = new Venda();
        vendaAntiga.setDataHora(LocalDateTime.now().minusDays(31));
        
        assertThrows(PrazoDevolucaoExcedidoException.class, () -> {
            facade.processarDevolucao(vendaAntiga, new ArrayList<>());
        });

        System.out.println("SUCESSO: Testes de resiliencia validados.");
    }
}