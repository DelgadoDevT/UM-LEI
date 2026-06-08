package pt.uminho.taki.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.uminho.taki.api.global.GlobalApiApp;
import pt.uminho.taki.api.local.LocalApiApp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NodeApiLauncherTest {

    @Mock
    private LocalApiApp localApiApp;

    @Mock
    private GlobalApiApp globalApiApp;

    private NodeApiLauncher nodeApiLauncher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.nodeApiLauncher = new NodeApiLauncher(localApiApp, globalApiApp);
    }

    @Test
    @DisplayName("Deve arrancar API local e global em role HUB")
    public void testIniciarComRoleHub() {
        // Act
        nodeApiLauncher.iniciar(NodeRole.CENTRAL);

        // Assert
        verify(localApiApp).iniciar();
        verify(globalApiApp).iniciar();
    }

    @Test
    @DisplayName("Deve arrancar apenas API local em role BRANCH")
    public void testIniciarComRoleBranch() {
        // Act
        nodeApiLauncher.iniciar(NodeRole.LOCAL);

        // Assert
        verify(localApiApp).iniciar();
        verify(globalApiApp, never()).iniciar();
    }

    @Test
    @DisplayName("Deve parar API local se arranque global falhar no role HUB")
    public void testIniciarComRoleHubQuandoGlobalFalha() {
        // Arrange
        RuntimeException falha = new RuntimeException("falha ao arrancar global");
        doThrow(falha).when(globalApiApp).iniciar();

        // Act & Assert
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> nodeApiLauncher.iniciar(NodeRole.CENTRAL));
        verify(localApiApp).iniciar();
        verify(globalApiApp).iniciar();
        verify(localApiApp).parar();
        org.junit.jupiter.api.Assertions.assertSame(falha, excecao);
    }
}
