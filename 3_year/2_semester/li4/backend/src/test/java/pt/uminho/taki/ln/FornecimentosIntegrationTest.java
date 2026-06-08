package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.ConnectionManager;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.ln.fornecimentos.Encomenda;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.fornecimentos.LinhaEncomenda;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FornecimentosIntegrationTest {

    private EncomendaDAO encomendaDAO;
    private FornecedorDAO fornecedorDAO;
    private ProdutoDAO produtoDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.encomendaDAO = new EncomendaDAO();
        this.fornecedorDAO = new FornecedorDAO();
        this.produtoDAO = new ProdutoDAO();

        // Setup inicial: Loja e Fornecedor
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Teste', '253000000', 'loja@taki.pt', '500123456', 'Rua A', 'Braga', 'Braga')");
        }

        Fornecedor f = new Fornecedor(UUID.randomUUID().toString(), "Fornecedor Teste", "500999888", "210000000", "forn@teste.pt", "Ativo");
        fornecedorDAO.save(f.getIdFornecedor(), f);
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
    void testCicloVidaEncomenda() throws Exception {
        // 1. Preparar Produto e Fornecedor
        Fornecedor f = fornecedorDAO.findAll().iterator().next();
        Produto p = new Produto(UUID.randomUUID().toString(), "111", "Prod Teste", "Desc", 1.0, 1.23, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(p.getIdProduto(), p);

        // 2. Criar Encomenda e avancar estados em memoria
        String idEnc = UUID.randomUUID().toString();
        Encomenda enc = new Encomenda(idEnc, f.getIdFornecedor(), "1");
        enc.adicionarLinha(new LinhaEncomenda(UUID.randomUUID().toString(), idEnc, p.getIdProduto(), 50.0, 1.0));

        assertEquals("Rascunho", enc.getEstadoAtual().getDesignacao());
        enc.avancarEstado(); // Pendente
        enc.avancarEstado(); // Em Transito
        enc.avancarEstado(); // Concluída
        
        // 3. Persistir apenas o estado final para validar o DAO
        encomendaDAO.save(idEnc, enc);

        // 4. Verificacao na BD
        Encomenda guardada = encomendaDAO.findById(idEnc).orElseThrow();
        assertEquals("Concluída", guardada.getEstadoAtual().getDesignacao());
        assertFalse(guardada.getLinhas().isEmpty(), "A encomenda deve ter linhas persistidas.");
    }
}
