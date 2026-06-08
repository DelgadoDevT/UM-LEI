package pt.uminho.taki.ln.inventario;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.LojaDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.ln.lojas.Loja;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integracao para o servico de inventario.
 * Valida a comunicacao real com a base de dados PostgreSQL.
 */
public class InventarioIntegrationTest {

    private InventarioDAO inventarioDAO;
    private LojaDAO lojaDAO;
    private ProdutoDAO produtoDAO;
    private InventarioService inventarioService;

    private Integer idLoja;
    private String idProduto;
    private String idInventario;

    @BeforeEach
    void setUp() {
        this.inventarioDAO = new InventarioDAO();
        this.lojaDAO = new LojaDAO();
        this.produtoDAO = new ProdutoDAO();
        this.inventarioService = new InventarioService(this.inventarioDAO);

        // Criar loja necessaria
        Loja loja = new Loja();
        loja.setNome("Loja Stock Teste");
        loja.setEmail("stock@teste.com");
        loja.setTelefone("253111222");
        loja.setNif("500999888");
        loja.setRua("Rua Stock");
        loja.setCidade("Braga");
        loja.setDistrito("Braga");
        Loja guardada = this.lojaDAO.save(null, loja);
        this.idLoja = guardada.getIdLoja();

        // Criar produto necessario
        this.idProduto = UUID.randomUUID().toString();
        Produto p = new Produto();
        p.setIdProduto(this.idProduto);
        p.setCodigoBarras("BC-INV-" + this.idProduto.substring(0, 8));
        p.setNome("Produto Inventario");
        p.setPrecoCusto(10.0);
        p.setTaxaIva(TaxaIva.NORMAL_23);
        this.produtoDAO.save(this.idProduto, p);

        // Criar registo de inventario
        this.idInventario = UUID.randomUUID().toString();
        Inventario inv = new Inventario(this.idInventario, 100.0, 10.0, this.idLoja, this.idProduto);
        this.inventarioDAO.save(this.idProduto, inv);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = pt.uminho.taki.dao.TestConnectionManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Movimento_Inventario WHERE id_inventario IN (SELECT id_inventario FROM Inventario WHERE id_produto = ?)")) {
                ps.setObject(1, UUID.fromString(this.idProduto));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Inventario WHERE id_produto = ?")) {
                ps.setObject(1, UUID.fromString(this.idProduto));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Alerta_Stock WHERE id_loja = ? AND id_produto = ?")) {
                ps.setInt(1, this.idLoja);
                ps.setObject(2, UUID.fromString(this.idProduto));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE Produto SET estado = 'Descontinuado' WHERE id_produto = ?")) {
                ps.setObject(1, UUID.fromString(this.idProduto));
                ps.executeUpdate();
            }
            if (this.idLoja != null) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Loja WHERE id_loja = ?")) {
                    ps.setInt(1, this.idLoja);
                    ps.executeUpdate();
                }
            }
        }
    }

    @Test
    void testAtualizarLimiteSegurancaEVerificarPersistencia() throws Exception {
        // Arrange
        double novoLimite = 120.0;

        // Act
        // Nota: O servico usa o idProduto como chave no DAO de Inventario, conforme implementado
        this.inventarioService.definirLimiteSeguranca(this.idProduto, novoLimite);

        // Assert
        Optional<Inventario> guardadoOpt = this.inventarioDAO.findById(this.idProduto);
        assertTrue(guardadoOpt.isPresent(), "O inventario deve estar na base de dados");
        
        Inventario guardado = guardadoOpt.get();
        assertEquals(120.0, guardado.getQuantidadeMinima(), 0.001, "A quantidade minima deve ser 120.0");
    }

    @Test
    void testRegistarMovimentoEntradaESaida() throws Exception {
        // Arrange
        // Stock inicial no setUp e 100.0
        pt.uminho.taki.ln.inventario.MovimentoInventario mEntrada = new pt.uminho.taki.ln.inventario.MovimentoInventario();
        mEntrada.setIdInventario(this.idProduto); // O DAO usa idProduto como chave para Inventario
        mEntrada.setQuantidade(50.0);
        mEntrada.setTipo(TipoMovimento.ENTRADA);

        // Act: Entrada de 50
        this.inventarioService.registarMovimentoManual(mEntrada);

        // Assert
        Optional<Inventario> posEntrada = this.inventarioDAO.findById(this.idProduto);
        assertEquals(150.0, posEntrada.get().getQuantidade(), "Stock deve ser 100 + 50 = 150");

        // Act: Saida de 80
        pt.uminho.taki.ln.inventario.MovimentoInventario mSaida = new pt.uminho.taki.ln.inventario.MovimentoInventario();
        mSaida.setIdInventario(this.idProduto);
        mSaida.setQuantidade(80.0);
        mSaida.setTipo(TipoMovimento.SAIDA);
        this.inventarioService.registarMovimentoManual(mSaida);

        // Assert
        Optional<Inventario> posSaida = this.inventarioDAO.findById(this.idProduto);
        assertEquals(70.0, posSaida.get().getQuantidade(), "Stock deve ser 150 - 80 = 70");
    }

    @Test
    void testErroSaidaComStockInsuficiente() throws Exception {
        // Arrange
        // Stock inicial e 100.0
        pt.uminho.taki.ln.inventario.MovimentoInventario mQuebra = new pt.uminho.taki.ln.inventario.MovimentoInventario();
        mQuebra.setIdInventario(this.idProduto);
        mQuebra.setQuantidade(150.0); // Tentar remover 150 de 100 disponiveis
        mQuebra.setTipo(TipoMovimento.QUEBRA);

        // Act & Assert
        assertThrows(pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException.class, () -> {
            this.inventarioService.registarMovimentoManual(mQuebra);
        });
    }
}
