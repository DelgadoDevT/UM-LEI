package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.ProdutoFornecedorService;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integracao para o servico de produtos e fornecedores.
 * Valida a comunicacao real com a base de dados PostgreSQL.
 */
public class ProdutoIntegrationTest {

    private ProdutoDAO produtoDAO;
    private FornecedorDAO fornecedorDAO;
    private ProdutoFornecedorDAO pfDAO;
    private ProdutoService produtoService;
    private ProdutoFornecedorService pfService;

    private String idProduto;
    private String idFornecedor;

    @BeforeEach
    void setUp() {
        this.produtoDAO = new ProdutoDAO() {
            @Override
            protected java.sql.Connection getConnection() throws java.sql.SQLException {
                return pt.uminho.taki.dao.TestConnectionManager.getConnection();
            }
        };
        this.fornecedorDAO = new FornecedorDAO() {
            @Override
            protected java.sql.Connection getConnection() throws java.sql.SQLException {
                return pt.uminho.taki.dao.TestConnectionManager.getConnection();
            }
        };
        this.pfDAO = new ProdutoFornecedorDAO() {
            @Override
            protected java.sql.Connection getConnection() throws java.sql.SQLException {
                return pt.uminho.taki.dao.TestConnectionManager.getConnection();
            }
        };
        this.produtoService = new ProdutoService(this.produtoDAO);
        this.pfService = new ProdutoFornecedorService(this.fornecedorDAO, this.pfDAO);
        
        this.idProduto = UUID.randomUUID().toString();
        this.idFornecedor = UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() {
        // Limpeza dos dados de teste: Primeiro associações para evitar erros de FK
        if (this.idProduto != null && this.idFornecedor != null) {
            try (java.sql.Connection conn = pt.uminho.taki.dao.TestConnectionManager.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM Produto_Fornecedor WHERE id_produto = ? OR id_fornecedor = ?")) {
                ps.setObject(1, java.util.UUID.fromString(this.idProduto));
                ps.setObject(2, java.util.UUID.fromString(this.idFornecedor));
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                // Silencioso se falhar
            }
        }
        
        if (this.idProduto != null && this.produtoDAO.exists(this.idProduto)) {
            this.produtoDAO.delete(this.idProduto);
        }
        if (this.idFornecedor != null && this.fornecedorDAO.exists(this.idFornecedor)) {
            this.fornecedorDAO.delete(this.idFornecedor);
        }
    }

    @Test
    void testCriarProdutoECalcularPrecos() throws Exception {
        Produto p = new Produto();
        p.setIdProduto(this.idProduto);
        p.setCodigoBarras("BC-" + this.idProduto.substring(0, 8));
        p.setNome("Produto Teste Integracao");
        p.setPrecoCusto(100.0);
        p.setTaxaIva(TaxaIva.NORMAL_23);
        p.setUnidadeMedida("un");

        // Act
        this.produtoService.adicionarProduto(p);

        // Assert
        Optional<Produto> guardadoOpt = this.produtoDAO.findById(this.idProduto);
        assertTrue(guardadoOpt.isPresent(), "O produto deve estar na base de dados");
        
        Produto guardado = guardadoOpt.get();
        assertEquals(123.0, guardado.getPrecoVenda(), 0.01, "O preco de venda deve incluir 23% de IVA");
        assertEquals("Ativo", guardado.getEstado(), "O estado deve ser Ativo por defeito");
    }

    @Test
    void testImunidadeSqlInjection() throws Exception {
        Produto p = new Produto();
        p.setIdProduto(this.idProduto);
        p.setCodigoBarras("BC-SQL-" + this.idProduto.substring(0, 5));
        
        // Simular tentativa de SQL Injection com aspas e comentários
        String nomeMalicioso = "Produto'; DROP TABLE Produto; --";
        p.setNome(nomeMalicioso);
        p.setPrecoCusto(100.0);
        p.setTaxaIva(TaxaIva.NORMAL_23);
        p.setUnidadeMedida("un");

        // Act
        assertDoesNotThrow(() -> this.produtoService.adicionarProduto(p), 
            "A inserção não deve quebrar sintaticamente devido a carateres maliciosos");

        // Assert
        Optional<Produto> guardadoOpt = this.produtoDAO.findById(this.idProduto);
        assertTrue(guardadoOpt.isPresent(), "O produto deve ter sido guardado em segurança");
        
        Produto guardado = guardadoOpt.get();
        assertEquals(nomeMalicioso, guardado.getNome(), 
            "O nome malicioso deve ter sido guardado literalmente como texto, provando o escape dos Prepared Statements");
    }

    @Test
    void testAssociarFornecedorEValidarPreferencial() throws Exception {
        // Arrange: Criar produto e fornecedor
        Produto p = new Produto();
        p.setIdProduto(this.idProduto);
        p.setCodigoBarras("BC-PF-" + this.idProduto.substring(0, 8));
        p.setNome("Produto para Fornecedor");
        p.setPrecoCusto(50.0);
        this.produtoDAO.save(this.idProduto, p);

        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(this.idFornecedor);
        f.setNome("Fornecedor Teste");
        f.setNif("123456789");
        f.setEmail("teste@forn.com");
        f.setTelefone("912345678");
        f.setEstado("Ativo");
        this.fornecedorDAO.save(this.idFornecedor, f);

        // Act: Associar
        this.pfService.associarProdutoAFornecedor(this.idProduto, this.idFornecedor, 45.0);

        // Assert
        Optional<ProdutoFornecedor> pfOpt = this.pfDAO.findByIdProdutoAndIdFornecedor(this.idProduto, this.idFornecedor);
        assertTrue(pfOpt.isPresent(), "A associacao deve existir na base de dados");
        assertTrue(pfOpt.get().isPreferencial(), "Deve ser o fornecedor preferencial por ser o unico");
    }

    @Test
    void testEditarProdutoEAtualizarPrecoVenda() throws Exception {
        // Arrange
        Produto p = new Produto();
        p.setIdProduto(this.idProduto);
        p.setCodigoBarras("EDIT-" + this.idProduto.substring(0, 8));
        p.setNome("Produto Original");
        p.setPrecoCusto(10.0);
        p.setTaxaIva(TaxaIva.NORMAL_23);
        this.produtoService.adicionarProduto(p);

        // Act: Alterar preco de custo
        p.setPrecoCusto(20.0);
        p.setNome("Produto Editado");
        this.produtoService.editarProduto(p);

        // Assert
        Optional<Produto> editadoOpt = this.produtoDAO.findById(this.idProduto);
        assertTrue(editadoOpt.isPresent());
        assertEquals(24.6, editadoOpt.get().getPrecoVenda(), 0.01, "O preco de venda deve ser recalculado no update");
        assertEquals("Produto Editado", editadoOpt.get().getNome());
    }

    @Test
    void testErroAoAdicionarProdutoComCodigoBarrasDuplicado() throws Exception {
        // Arrange
        Produto p1 = new Produto();
        p1.setIdProduto(this.idProduto);
        p1.setCodigoBarras("DUP-" + this.idProduto.substring(0, 8));
        p1.setNome("Produto 1");
        this.produtoService.adicionarProduto(p1);

        String id2 = UUID.randomUUID().toString();
        Produto p2 = new Produto();
        p2.setIdProduto(id2);
        p2.setCodigoBarras("DUP-" + this.idProduto.substring(0, 8));
        p2.setNome("Produto 2");

        // Act & Assert
        try {
            assertThrows(pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException.class, () -> {
                this.produtoService.adicionarProduto(p2);
            });
        } finally {
            if (this.produtoDAO.exists(id2)) {
                this.produtoDAO.delete(id2);
            }
        }
    }
}
