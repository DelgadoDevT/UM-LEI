package pt.uminho.taki.ln;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.ConnectionManager;
import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TakiLNLocalIntegrationTest {

    private TakiLNLocal takiLNLocal;
    private OutboxDAO outboxDAO;

    @BeforeEach
    void setupDB() throws Exception {
        cleanDB();
        
        // Inserir uma loja base (necessaria para a FK de Funcionario)
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Teste', '253000000', 'loja@taki.pt', '500123456', 'Rua A', 'Braga', 'Braga')");
        }
        
        this.takiLNLocal = new TakiLNLocal();
        this.outboxDAO = new OutboxDAO();
    }

    @org.junit.jupiter.api.AfterEach
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
    void testFluxoCompleto_FuncionarioEProduto() throws Exception {
        // 1. Registar um Funcionario Real
        Funcionario f = new Funcionario();
        String idF = UUID.randomUUID().toString();
        f.setId(idF);
        f.setNome("Soberano Teste");
        f.setEmail("soberano@taki.pt");
        f.setPassword("Password123"); 
        f.setIdPerfilAcesso("GERENTE");
        f.setIdLoja(1);
        
        takiLNLocal.registarFuncionario(f);
        System.out.println("DEBUG: Funcionario registado com sucesso.");

        // 2. Autenticar o Funcionario (Valida o Hash da Password na DB)
        Funcionario autenticado = takiLNLocal.autenticar("soberano@taki.pt", "Password123");
        assertNotNull(autenticado);
        assertEquals("Soberano Teste", autenticado.getNome());
        System.out.println("DEBUG: Autenticacao real com Hash validada.");

        // 3. Adicionar um Produto (Valida Persistencia e Trigger de Sync)
        Produto p = new Produto();
        p.setIdProduto(UUID.randomUUID().toString());
        p.setNome("Vinho Verde Premium");
        p.setCodigoBarras("5609998887771");
        p.setPrecoCusto(5.50);
        p.setTaxaIva(TaxaIva.INTERMEDIA_13);
        p.setEstado("Ativo");
        
        takiLNLocal.adicionarProduto(p);
        
        // 4. Verificar se o Produto existe
        List<Produto> produtos = takiLNLocal.listarProdutos();
        assertTrue(produtos.stream().anyMatch(prod -> prod.getCodigoBarras().equals("5609998887771")));

        // 5. Verificar se o TRIGGER de sincronizacao funcionou automaticamente na BD
        // Ao inserir um produto, a tabela Fila_Sincronizacao DEVE ter uma entrada
        int countSync = outboxDAO.count();
        assertTrue(countSync > 0, "O Trigger de base de dados deve ter inserido um registo na fila de sincronizacao.");
        
        System.out.println("SUCESSO: Fluxo completo validado com dados reais e Triggers de BD.");
    }
}