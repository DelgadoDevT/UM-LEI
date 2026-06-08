package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.*;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.sincronizacao.dto.OutboxEntry;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SincronizacaoDatabaseIntegrationTest {

    private TakiLNLocal facade;
    private ProdutoDAO produtoDAO;
    private FuncionarioDAO funcionarioDAO;
    private OutboxDAO outboxDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.produtoDAO = new ProdutoDAO();
        this.funcionarioDAO = new FuncionarioDAO();
        this.outboxDAO = new OutboxDAO();

        // Setup Loja
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Sinc', '253000000', 'sinc@taki.pt', '500123456', 'Rua C', 'Braga', 'Braga')");
        }

        Funcionario f = new Funcionario(UUID.randomUUID().toString(), "Operador Sinc", "sinc@taki.pt", "Hash123", "OPERADOR", 1);
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
    void testTriggerSincronizacaoProduto() throws Exception {
        // 1. Inserir produto via fachada
        Produto p = new Produto(UUID.randomUUID().toString(), "SYNC001", "Produto Sinc", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        facade.adicionarProduto(p);

        // 2. Verificar se o trigger inseriu na Fila_Sincronizacao
        Collection<OutboxEntry> entries = outboxDAO.findAll();
        
        boolean found = entries.stream()
                .anyMatch(e -> e.getNomeTabela().equalsIgnoreCase("Produto") && e.getIdEntidade().equals(p.getIdProduto()));
        
        assertTrue(found, "O trigger deve registar a inserção do produto na fila de sincronização.");
    }

    @Test
    void testTriggerSincronizacaoVendaEMovimento() throws Exception {
        // 1. Preparar Produto
        String idProd = UUID.randomUUID().toString();
        Produto p = new Produto(idProd, "SYNC002", "Produto Venda Sinc", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProd, p);

        // Criar inventario para permitir venda (caso contrário o trigger de stock poderia falhar se houvesse check de negativo, 
        // mas aqui focamos na sincronização)
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Inventario (id_inventario, quantidade, quantidade_minima, id_loja, id_produto) " +
                         "VALUES ('" + UUID.randomUUID() + "', 100, 10, 1, '" + idProd + "')");
        }

        // 2. Realizar Venda
        Funcionario op = funcionarioDAO.findAll().iterator().next();
        Venda v = facade.iniciarVenda(1, op.getId());
        facade.adicionarLinhaVenda(v, p, 1);
        facade.registarVenda(v, "Numerário");

        // 3. Verificar fila de sincronização
        Collection<OutboxEntry> entries = outboxDAO.findAll();
        
        // Deve haver registo para a Venda e para o Movimento_Inventario (gerado pelo trigger de stock)
        boolean vendaFound = entries.stream().anyMatch(e -> e.getNomeTabela().equalsIgnoreCase("Venda"));
        boolean movFound = entries.stream().anyMatch(e -> e.getNomeTabela().equalsIgnoreCase("Movimento_Inventario"));

        assertTrue(vendaFound, "A venda deve estar na fila de sincronização.");
        assertTrue(movFound, "O movimento de inventário automático deve estar na fila de sincronização.");
    }
}
