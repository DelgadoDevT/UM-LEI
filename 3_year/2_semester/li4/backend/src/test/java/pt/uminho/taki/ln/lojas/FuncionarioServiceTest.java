package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.ln.lojas.seguranca.IPasswordHasher;
import pt.uminho.taki.ln.lojas.exceptions.ContaBloqueadaException;
import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o FuncionarioService sincronizados com a base de dados.
 */
@ExtendWith(MockitoExtension.class)
public class FuncionarioServiceTest {

    @Mock
    private FuncionarioDAO funcionarioDAO;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private IPerfilAcessoService perfilAcessoService;

    @InjectMocks
    private FuncionarioService funcionarioService;

    private Funcionario funcionarioValido;

    @BeforeEach
    public void setUp() {
        // Inicializa funcionario com idLoja=1 e idPerfilAcesso
        this.funcionarioValido = new Funcionario("1", "Colaborador Teste", "teste@empresa.com", "Password123", "PERFIL_VENDEDOR", 1);
        lenient().when(perfilAcessoService.existePerfil(anyString())).thenReturn(true);
        lenient().when(perfilAcessoService.temPermissao(anyString(), eq(Permissao.ADMINISTRAR_SISTEMA)))
                .thenAnswer(invocation -> "ADMIN".equalsIgnoreCase(invocation.getArgument(0)));
    }

    @Test
    @DisplayName("Deve registar um funcionario com password encriptada e idLoja")
    public void testRegistoComHashingSucesso() {
        // Arrange
        when(funcionarioDAO.findAll()).thenReturn(Collections.emptyList());
        when(passwordHasher.hash("Password123")).thenReturn("hashed_password");

        // Act
        funcionarioService.registarFuncionario(funcionarioValido);

        // Assert
        verify(passwordHasher, times(1)).hash("Password123");
        verify(funcionarioDAO, times(1)).save(eq(funcionarioValido.getId()), argThat(f -> 
            f.getPassword().equals("hashed_password") && 
            f.getIdLoja() == 1 &&
            f.getEstadoConta() == EstadoConta.ATIVO
        ));
    }

    @Test
    @DisplayName("Deve rejeitar password curta")
    public void testRegistoPasswordCurta() {
        funcionarioValido.setPassword("Pass1");
        assertThrows(PasswordFracaException.class, () -> funcionarioService.registarFuncionario(funcionarioValido));
    }

    @Test
    @DisplayName("Deve rejeitar password nula")
    public void testRegistoPasswordNula() {
        funcionarioValido.setPassword(null);
        assertThrows(PasswordFracaException.class, () -> funcionarioService.registarFuncionario(funcionarioValido));
    }

    @Test
    @DisplayName("Deve rejeitar password sem maiúsculas")
    public void testRegistoPasswordSemMaiuscula() {
        funcionarioValido.setPassword("password123");
        assertThrows(PasswordFracaException.class, () -> funcionarioService.registarFuncionario(funcionarioValido));
    }

    @Test
    @DisplayName("Deve rejeitar password sem números")
    public void testRegistoPasswordSemNumero() {
        funcionarioValido.setPassword("Password");
        assertThrows(PasswordFracaException.class, () -> funcionarioService.registarFuncionario(funcionarioValido));
    }

    @Test
    @DisplayName("Deve falhar o registo quando o email já existe")
    public void testRegistoComEmailExistente() {
        // Arrange
        when(funcionarioDAO.findAll()).thenReturn(Collections.singletonList(funcionarioValido));

        // Act & Assert
        assertThrows(EmailJaExisteException.class, () -> {
            funcionarioService.registarFuncionario(funcionarioValido);
        });
        verify(funcionarioDAO, never()).save(anyString(), any(Funcionario.class));
    }

    @Test
    @DisplayName("Deve bloquear uma conta ativa com sucesso")
    public void testBloqueioContaComSucesso() {
        // Arrange
        when(funcionarioDAO.findById("1")).thenReturn(Optional.of(funcionarioValido));

        // Act
        funcionarioService.bloquearConta("1", "Violacao de politica");

        // Assert
        verify(funcionarioDAO, times(1)).save(eq("1"), argThat(f -> f.getEstadoConta() == EstadoConta.BLOQUEADO));
    }

    @Test
    @DisplayName("Deve bloquear conta com confirmação de password do administrador")
    public void testBloqueioContaComConfirmacaoPasswordAdmin() throws Exception {
        Funcionario alvo = new Funcionario(funcionarioValido);
        Funcionario admin = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "ADMIN", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("1")).thenReturn(Optional.of(alvo));
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(admin));
        when(passwordHasher.matches("AdminPass123", "hash_admin")).thenReturn(true);

        funcionarioService.bloquearConta("1", "Motivo", "ADMIN1", "AdminPass123");

        verify(funcionarioDAO).save(eq("1"), argThat(f -> f.getEstadoConta() == EstadoConta.BLOQUEADO));
    }

    @Test
    @DisplayName("Deve falhar bloqueio com confirmação quando password admin é inválida")
    public void testBloqueioContaComPasswordAdminInvalida() {
        Funcionario admin = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "ADMIN", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(admin));
        when(passwordHasher.matches("Errada", "hash_admin")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> funcionarioService.bloquearConta("1", "Motivo", "ADMIN1", "Errada"));
        verify(funcionarioDAO, never()).save(anyString(), any(Funcionario.class));
    }

    @Test
    @DisplayName("Deve bloquear operação administrativa sem perfil autorizado e registar auditoria")
    public void testBloqueioContaSemPermissaoAdministrativaRegistaAuditoria() {
        Funcionario adminSemPermissao = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "OPERADOR", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(adminSemPermissao));

        assertThrows(SecurityException.class,
                () -> funcionarioService.bloquearConta("1", "Motivo", "ADMIN1", null));

        verify(funcionarioDAO, never()).save(anyString(), any(Funcionario.class));
    }

    @Test
    @DisplayName("Deve falhar o bloqueio se o funcionário não existir")
    public void testBloqueioFuncionarioInexistente() {
        when(funcionarioDAO.findById("X")).thenReturn(Optional.empty());
        assertThrows(FuncionarioNaoEncontradoException.class, () -> funcionarioService.bloquearConta("X", "Motivo"));
    }

    @Test
    @DisplayName("Deve impedir auto-bloqueio do administrador autenticado")
    public void testBloqueioPropriaContaNaoPermitido() {
        assertThrows(IllegalStateException.class,
                () -> funcionarioService.bloquearConta("1", "Motivo", "1"));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso validando o hash")
    public void testAutenticacaoComHashSucesso() {
        // Arrange
        Funcionario fNoSistema = new Funcionario(funcionarioValido);
        fNoSistema.setPassword("hashed_password");

        when(funcionarioDAO.findAll()).thenReturn(Collections.singletonList(fNoSistema));
        when(passwordHasher.matches("Password123", "hashed_password")).thenReturn(true);

        // Act
        Funcionario autenticado = funcionarioService.autenticar(funcionarioValido.getEmail(), "Password123");

        // Assert
        assertNotNull(autenticado);
        assertEquals(1, autenticado.getIdLoja());
        verify(passwordHasher, times(1)).matches("Password123", "hashed_password");
    }

    @Test
    @DisplayName("Deve falhar a autenticacao devido a conta bloqueada")
    public void testAutenticacaoFalhaContaBloqueada() {
        // Arrange
        Funcionario fBloqueado = new Funcionario(funcionarioValido);
        fBloqueado.setEstadoConta(EstadoConta.BLOQUEADO);
        when(funcionarioDAO.findAll()).thenReturn(Collections.singletonList(fBloqueado));

        // Act & Assert
        assertThrows(ContaBloqueadaException.class, () -> {
            funcionarioService.autenticar(fBloqueado.getEmail(), "Password123");
        });
        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve falhar a autenticacao quando o hash nao coincide")
    public void testAutenticacaoFalhaCredenciaisInvalidas() {
        // Arrange
        Funcionario fNoSistema = new Funcionario(funcionarioValido);
        fNoSistema.setPassword("hashed_password");

        when(funcionarioDAO.findAll()).thenReturn(Collections.singletonList(fNoSistema));
        when(passwordHasher.matches("SenhaIncorreta", "hashed_password")).thenReturn(false);

        // Act & Assert
        assertThrows(CredenciaisInvalidasException.class, () -> {
            funcionarioService.autenticar(funcionarioValido.getEmail(), "SenhaIncorreta");
        });
    }

    @Test
    @DisplayName("Deve falhar a autenticacao se o email não existir")
    public void testAutenticacaoEmailInexistente() {
        when(funcionarioDAO.findAll()).thenReturn(Collections.emptyList());
        assertThrows(CredenciaisInvalidasException.class, () -> funcionarioService.autenticar("x@x.pt", "pw"));
    }

    @Test
    @DisplayName("Deve atualizar funcionário e manter hash quando password não é alterada")
    public void testAtualizacaoFuncionarioSemAlterarPassword() {
        // Arrange
        Funcionario atual = new Funcionario(funcionarioValido);
        atual.setPassword("hash_existente");
        when(funcionarioDAO.findById("1")).thenReturn(Optional.of(atual));
        when(funcionarioDAO.findAll()).thenReturn(Collections.singletonList(atual));

        Funcionario atualizado = new Funcionario(atual);
        atualizado.setNome("Novo Nome");
        atualizado.setPassword("");

        // Act
        assertDoesNotThrow(() -> funcionarioService.atualizarFuncionario(atualizado));

        // Assert
        verify(funcionarioDAO).save(eq("1"), argThat(f -> "hash_existente".equals(f.getPassword())));
    }

    @Test
    @DisplayName("Deve remover conta logicamente com confirmação de password admin")
    public void testRemoverContaLogicamenteComConfirmacaoAdmin() throws Exception {
        Funcionario alvo = new Funcionario(funcionarioValido);
        Funcionario admin = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "ADMIN", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("1")).thenReturn(Optional.of(alvo));
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(admin));
        when(passwordHasher.matches("AdminPass123", "hash_admin")).thenReturn(true);

        funcionarioService.removerContaLogicamente("1", "ADMIN1", "AdminPass123");

        verify(funcionarioDAO).save(eq("1"), argThat(f -> f.getEstadoConta() == EstadoConta.INATIVO));
    }

    @Test
    @DisplayName("Deve atribuir perfil válido a utilizador com sucesso")
    public void testAtribuirPerfilComSucesso() throws Exception {
        Funcionario alvo = new Funcionario(funcionarioValido);
        Funcionario admin = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "ADMIN", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("1")).thenReturn(Optional.of(alvo));
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(admin));
        when(perfilAcessoService.existePerfil("GERENTE")).thenReturn(true);

        funcionarioService.atribuirPerfil("1", "GERENTE", "ADMIN1");

        verify(funcionarioDAO).save(eq("1"), argThat(f -> "GERENTE".equals(f.getIdPerfilAcesso())));
    }

    @Test
    @DisplayName("Deve falhar atribuição de perfil inválido")
    public void testAtribuirPerfilInvalidoFalha() {
        Funcionario admin = new Funcionario("ADMIN1", "Admin", "admin@empresa.com", "hash_admin", "ADMIN", 1, EstadoConta.ATIVO);
        when(funcionarioDAO.findById("ADMIN1")).thenReturn(Optional.of(admin));
        when(perfilAcessoService.existePerfil("INVALIDO")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> funcionarioService.atribuirPerfil("1", "INVALIDO", "ADMIN1"));
        verify(funcionarioDAO, never()).save(anyString(), any(Funcionario.class));
    }
}
