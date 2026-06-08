package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.dao.LojaDAO;
import pt.uminho.taki.ln.lojas.seguranca.BCryptPasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integracao para o servico de funcionarios.
 * Valida a comunicacao real com a base de dados PostgreSQL.
 */
public class FuncionarioIntegrationTest {

    private FuncionarioDAO funcionarioDAO;
    private LojaDAO lojaDAO;
    private FuncionarioService funcionarioService;
    
    private Integer idLoja;
    private String idFuncionario;

    @BeforeEach
    void setUp() {
        this.funcionarioDAO = new FuncionarioDAO();
        this.lojaDAO = new LojaDAO();
        this.funcionarioService = new FuncionarioService(this.funcionarioDAO, new BCryptPasswordHasher());
        
        // Criar loja necessaria para a chave estrangeira
        Loja loja = new Loja();
        loja.setNome("Loja Teste");
        loja.setEmail("loja@teste.com");
        loja.setTelefone("253123456");
        loja.setNif("500123456");
        loja.setRua("Rua Teste");
        loja.setCidade("Braga");
        loja.setDistrito("Braga");
        
        Loja guardada = this.lojaDAO.save(null, loja);
        this.idLoja = guardada.getIdLoja();
        this.idFuncionario = UUID.randomUUID().toString();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = pt.uminho.taki.dao.TestConnectionManager.getConnection()) {
            if (this.idLoja != null) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Funcionario WHERE id_loja = ?")) {
                    ps.setInt(1, this.idLoja);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Loja WHERE id_loja = ?")) {
                    ps.setInt(1, this.idLoja);
                    ps.executeUpdate();
                }
            }
        }
    }

    @Test
    void testRegistarFuncionarioEVerificarHash() throws Exception {
        Funcionario f = new Funcionario();
        f.setId(this.idFuncionario);
        f.setNome("Funcionario Teste");
        f.setEmail("func." + this.idFuncionario.substring(0, 8) + "@taki.com");
        f.setPassword("Password123");
        f.setIdLoja(this.idLoja);
        f.setIdPerfilAcesso("Administrador");

        // Act
        this.funcionarioService.registarFuncionario(f);

        // Assert
        Optional<Funcionario> guardadoOpt = this.funcionarioDAO.findById(this.idFuncionario);
        assertTrue(guardadoOpt.isPresent(), "O funcionario deve estar na base de dados");
        
        Funcionario guardado = guardadoOpt.get();
        assertNotEquals("Password123", guardado.getPassword(), "A password nao deve ser guardada em plain text");
        assertTrue(guardado.getPassword().startsWith("$2"), "A password deve estar guardada em formato BCrypt.");
    }

    @Test
    void testAutenticarFuncionarioComPasswordHashed() throws Exception {
        // Arrange
        Funcionario f = new Funcionario();
        f.setId(this.idFuncionario);
        f.setNome("Diogo Teste Auth");
        f.setEmail("auth." + this.idFuncionario.substring(0, 8) + "@taki.com");
        f.setPassword("MinhaSenhaSegura123");
        f.setIdLoja(this.idLoja);
        f.setIdPerfilAcesso("Gerente");

        this.funcionarioService.registarFuncionario(f);

        // Act: Autenticar
        Funcionario autenticado = this.funcionarioService.autenticar(f.getEmail(), "MinhaSenhaSegura123");

        // Assert
        assertNotNull(autenticado, "A autenticacao deve ser bem sucedida");
        assertEquals(f.getEmail(), autenticado.getEmail());
        
        // Act & Assert: Password errada
        assertThrows(pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException.class, () -> {
            this.funcionarioService.autenticar(f.getEmail(), "SenhaErrada123");
        });
    }

    @Test
    void testErroRegistarFuncionarioComEmailJaExistente() throws Exception {
        // Arrange
        String emailDuplicado = "dup." + UUID.randomUUID() + "@taki.com";
        Funcionario f1 = new Funcionario();
        f1.setId(this.idFuncionario);
        f1.setNome("Primeiro Diogo");
        f1.setEmail(emailDuplicado);
        f1.setPassword("Pass12345");
        f1.setIdLoja(this.idLoja);
        f1.setIdPerfilAcesso("Caixa");
        this.funcionarioService.registarFuncionario(f1);

        String id2 = UUID.randomUUID().toString();
        Funcionario f2 = new Funcionario();
        f2.setId(id2);
        f2.setNome("Segundo Diogo");
        f2.setEmail(emailDuplicado); // Email duplicado
        f2.setPassword("Pass12345");
        f2.setIdLoja(this.idLoja);
        f2.setIdPerfilAcesso("Caixa");

        // Act & Assert
        try {
            assertThrows(pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException.class, () -> {
                this.funcionarioService.registarFuncionario(f2);
            });
        } finally {
            if (this.funcionarioDAO.exists(id2)) {
                this.funcionarioDAO.delete(id2);
            }
        }
    }
}
