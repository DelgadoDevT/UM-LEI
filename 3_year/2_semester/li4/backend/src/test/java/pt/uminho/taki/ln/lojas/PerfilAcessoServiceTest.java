package pt.uminho.taki.ln.lojas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para a classe PerfilAcessoService com gestao em memoria.
 */
public class PerfilAcessoServiceTest {

    private PerfilAcessoService perfilAcessoService;

    @BeforeEach
    public void setUp() {
        this.perfilAcessoService = new PerfilAcessoService();
    }

    @Test
    @DisplayName("Deve inicializar os perfis base com as permissoes corretas")
    public void testInicializacaoPerfisBase() {
        // ADMIN deve ter todas as permissoes
        assertTrue(perfilAcessoService.temPermissao("ADMIN", Permissao.ADMINISTRAR_SISTEMA));
        assertTrue(perfilAcessoService.temPermissao("ADMIN", Permissao.GERIR_PRODUTOS));
        assertTrue(perfilAcessoService.temPermissao("ADMIN", Permissao.REGISTAR_VENDA));

        // GERENTE deve ter gestao e venda, mas nao admin
        assertFalse(perfilAcessoService.temPermissao("GERENTE", Permissao.ADMINISTRAR_SISTEMA));
        assertTrue(perfilAcessoService.temPermissao("GERENTE", Permissao.GERIR_PRODUTOS));
        assertTrue(perfilAcessoService.temPermissao("GERENTE", Permissao.REGISTAR_VENDA));

        // OPERADOR deve ter apenas venda
        assertFalse(perfilAcessoService.temPermissao("OPERADOR", Permissao.ADMINISTRAR_SISTEMA));
        assertFalse(perfilAcessoService.temPermissao("OPERADOR", Permissao.GERIR_PRODUTOS));
        assertTrue(perfilAcessoService.temPermissao("OPERADOR", Permissao.REGISTAR_VENDA));
    }

    @Test
    @DisplayName("Deve registar um perfil com sucesso")
    public void testRegistoPerfilComSucesso() throws PerfilDuplicadoException {
        // Arrange
        PerfilAcesso novo = new PerfilAcesso("P1", "AUDITOR", List.of(Permissao.REGISTAR_VENDA));

        // Act
        perfilAcessoService.registarPerfil(novo);

        // Assert
        assertTrue(perfilAcessoService.temPermissao("AUDITOR", Permissao.REGISTAR_VENDA));
    }

    @Test
    @DisplayName("Deve falhar o registo quando o nome do perfil ja existe")
    public void testRegistoPerfilComNomeDuplicado() {
        // Arrange
        PerfilAcesso dup = new PerfilAcesso("P2", "ADMIN", List.of());

        // Act & Assert
        assertThrows(PerfilDuplicadoException.class, () -> {
            perfilAcessoService.registarPerfil(dup);
        });
    }

    @Test
    @DisplayName("Deve devolver false se o perfil nao existir")
    public void testVerificacaoPermissaoPerfilInexistente() {
        // Act
        boolean result = perfilAcessoService.temPermissao("Inexistente", Permissao.REGISTAR_VENDA);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve validar existência de perfil base")
    public void testExistePerfilBase() {
        assertTrue(perfilAcessoService.existePerfil("ADMIN"));
        assertFalse(perfilAcessoService.existePerfil("NAO_EXISTE"));
    }

    @Test
    @DisplayName("Deve editar perfil existente e aplicar novas permissões")
    public void testEditarPerfilComSucesso() {
        // Act
        perfilAcessoService.editarPerfil("OPERADOR", List.of(Permissao.REGISTAR_VENDA, Permissao.GERIR_PRODUTOS));

        // Assert
        assertTrue(perfilAcessoService.temPermissao("OPERADOR", Permissao.GERIR_PRODUTOS));
    }

    @Test
    @DisplayName("Deve falhar edição de perfil inexistente")
    public void testEditarPerfilInexistenteFalha() {
        assertThrows(IllegalArgumentException.class,
                () -> perfilAcessoService.editarPerfil("INEXISTENTE", List.of(Permissao.REGISTAR_VENDA)));
    }
}
