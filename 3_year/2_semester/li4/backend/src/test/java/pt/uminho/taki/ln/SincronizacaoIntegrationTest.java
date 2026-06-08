package pt.uminho.taki.ln;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.ConnectionManager;
import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.ln.sincronizacao.dto.OutboxEntry;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SincronizacaoIntegrationTest {

    private TakiLNLocal facade;
    private OutboxDAO outboxDAO;

    @BeforeEach
    void setUp() throws Exception {
        cleanDB();
        this.facade = new TakiLNLocal();
        this.outboxDAO = new OutboxDAO();
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
    void testProcessamentoSincronizacao() throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            String idEntidade = UUID.randomUUID().toString();
            stmt.execute("INSERT INTO Fila_Sincronizacao (nome_tabela, id_entidade, operacao) " +
                         "VALUES ('Produto', '" + idEntidade + "', 'INSERT')");
        }

        Collection<OutboxEntry> entries = outboxDAO.findAll();
        assertFalse(entries.isEmpty(), "A fila de sincronização deve ter um registo.");
        
        assertDoesNotThrow(() -> {
            facade.sincronizarDados();
        }, "A execução deve decorrer sem exceções uma vez que o processo está mockado na fachada local.");
    }
}