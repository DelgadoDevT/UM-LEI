package pt.uminho.taki.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.vendas.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para verificar o comportamento transacional do VendaDAO.
 * Garante que falhas (como FK inválida) resultam em rollback completo.
 * 
 * @author TakiLN Team
 */
class VendaDAOTransactionTest {

    private VendaDAO vendaDAO;
    private ProdutoDAO produtoDAO;

    @BeforeEach
    void setUp() throws Exception {
        vendaDAO = new VendaDAO();
        produtoDAO = new ProdutoDAO();
        TestConnectionManager.initializeDatabase();
        cleanDB();

        try (Connection conn = TestConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Loja (id_loja, nome, telefone, email, nif, rua, cidade, distrito) " +
                         "VALUES (1, 'Loja Teste Vendas', '253000000', 'vendas@taki.pt', '500123456', 'Rua B', 'Braga', 'Braga')");
            stmt.execute("INSERT INTO Funcionario (id_funcionario, nome, email, cargo, password_hash, estado, id_loja) " +
                         "VALUES ('" + UUID.randomUUID() + "', 'Op', 'op@taki.pt', 'OPERADOR', 'hash', 'Ativo', 1)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanDB();
    }

    private void cleanDB() throws Exception {
        try (Connection conn = TestConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Fila_Sincronizacao RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE Pagamento CASCADE");
            stmt.execute("TRUNCATE TABLE Fatura CASCADE");
            stmt.execute("TRUNCATE TABLE Linha_Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Venda CASCADE");
            stmt.execute("TRUNCATE TABLE Produto_Categoria CASCADE");
            stmt.execute("TRUNCATE TABLE Produto_Fornecedor CASCADE");
            stmt.execute("TRUNCATE TABLE Produto CASCADE");
            stmt.execute("TRUNCATE TABLE Funcionario CASCADE");
            stmt.execute("TRUNCATE TABLE Loja CASCADE");
        }
    }

    private String getFuncionarioId() throws Exception {
        try (Connection conn = TestConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_funcionario FROM Funcionario LIMIT 1")) {
            if (rs.next()) return rs.getString(1);
            throw new RuntimeException("Funcionario nao encontrado");
        }
    }

    @Test
    void save_comLinhaInvalida_fazRollbackCompleto() throws Exception {
        
        String idFuncionario = getFuncionarioId();

        // 1. Arrange: Venda válida + uma linha válida + uma linha inválida
        String idVenda = UUID.randomUUID().toString();
        Venda venda = new Venda();
        venda.setIdVenda(idVenda);
        venda.setIdLoja(1);
        venda.setIdFuncionario(idFuncionario);
        venda.setEstado("Concluída");
        venda.setDataHora(LocalDateTime.now());
        
        // Produto válido
        String idProdutoValido = UUID.randomUUID().toString();
        Produto pValido = new Produto(idProdutoValido, "111", "Prod Val", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProdutoValido, pValido);

        try (Connection conn = TestConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Inventario (id_inventario, quantidade, quantidade_minima, id_loja, id_produto) VALUES (?, ?, ?, ?, ?)")) {
            ps.setObject(1, UUID.randomUUID());
            ps.setDouble(2, 10.0);
            ps.setDouble(3, 1.0);
            ps.setInt(4, 1);
            ps.setObject(5, UUID.fromString(idProdutoValido));
            ps.executeUpdate();
        }

        List<LinhaVenda> linhas = new ArrayList<>();
        LinhaVenda lvValida = new LinhaVenda(pValido, 1, 0.0);
        linhas.add(lvValida);

        // Produto inválido (não existe na BD)
        String idProdutoInvalido = UUID.randomUUID().toString();
        Produto pInvalido = new Produto(idProdutoInvalido, "222", "Prod Inval", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        LinhaVenda lvInvalida = new LinhaVenda(pInvalido, 1, 0.0);
        linhas.add(lvInvalida);

        venda.setLinhas(linhas);
        venda.setTotal(4.0);

        // 2. Act: Tentar guardar a venda
        assertThrows(RuntimeException.class, () -> {
            vendaDAO.save(idVenda, venda);
        }, "Deve lançar exceção devido a violação de chave estrangeira no id_produto da Linha_Venda");

        // 3. Assert: Verificar que a Venda NÃO foi inserida (rollback funcionou)
        try (Connection conn = TestConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Venda WHERE id_venda = ?")) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt(1), "A Venda não deve existir na base de dados (esperado rollback)");
        }
    }

    @Test
    void save_apenasComLinhasValidas_fazCommitComSucesso() throws Exception {
        
        String idFuncionario = getFuncionarioId();

        // 1. Arrange: Venda válida + apenas linhas válidas
        String idVenda = UUID.randomUUID().toString();
        Venda venda = new Venda();
        venda.setIdVenda(idVenda);
        venda.setIdLoja(1);
        venda.setIdFuncionario(idFuncionario);
        venda.setEstado("Concluída");
        venda.setDataHora(LocalDateTime.now());
        
        String idProdutoValido = UUID.randomUUID().toString();
        Produto pValido = new Produto(idProdutoValido, "111", "Prod Val", "Desc", 1.0, 2.0, TaxaIva.NORMAL_23, "un", "Ativo");
        produtoDAO.save(idProdutoValido, pValido);

        try (Connection conn = TestConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Inventario (id_inventario, quantidade, quantidade_minima, id_loja, id_produto) VALUES (?, ?, ?, ?, ?)")) {
            ps.setObject(1, UUID.randomUUID());
            ps.setDouble(2, 10.0);
            ps.setDouble(3, 1.0);
            ps.setInt(4, 1);
            ps.setObject(5, UUID.fromString(idProdutoValido));
            ps.executeUpdate();
        }

        List<LinhaVenda> linhas = new ArrayList<>();
        LinhaVenda lvValida = new LinhaVenda(pValido, 1, 0.0);
        linhas.add(lvValida);

        venda.setLinhas(linhas);
        venda.setTotal(2.0);

        // 2. Act: Guardar a venda
        assertDoesNotThrow(() -> {
            vendaDAO.save(idVenda, venda);
        });

        // 3. Assert: Verificar que a Venda e as Linhas existem na base de dados
        try (Connection conn = TestConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Venda WHERE id_venda = ?")) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(1, rs.getInt(1), "A Venda deve existir na base de dados");
        }

        try (Connection conn = TestConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Linha_Venda WHERE id_venda = ?")) {
            ps.setObject(1, UUID.fromString(idVenda));
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(1, rs.getInt(1), "A Linha_Venda deve existir na base de dados");
        }
    }
}
