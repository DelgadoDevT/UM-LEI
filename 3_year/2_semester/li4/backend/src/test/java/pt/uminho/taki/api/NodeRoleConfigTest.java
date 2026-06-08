package pt.uminho.taki.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeRoleConfigTest {

    @Test
    @DisplayName("Deve usar BRANCH como omissao quando variavel de ambiente nao existe")
    public void testResolverRoleRuntimeComOmissaoSegura() {
        // Act
        NodeRole role = NodeRoleConfig.resolverRoleRuntime(null);

        // Assert
        assertEquals(NodeRole.LOCAL, role);
    }

    @Test
    @DisplayName("Deve aceitar HUB e BRANCH de forma case-insensitive")
    public void testResolverRoleRuntimeComRolesValidos() {
        // Act
        NodeRole roleHub = NodeRoleConfig.resolverRoleRuntime(" central ");
        NodeRole roleBranch = NodeRoleConfig.resolverRoleRuntime("LOCAL");

        // Assert
        assertEquals(NodeRole.CENTRAL, roleHub);
        assertEquals(NodeRole.LOCAL, roleBranch);
    }

    @Test
    @DisplayName("Deve falhar com mensagem explicita para role invalido")
    public void testResolverRoleRuntimeComRoleInvalido() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> NodeRoleConfig.resolverRoleRuntime("invalid")
        );

        assertTrue(exception.getMessage().contains("APP_MODE"));
        assertTrue(exception.getMessage().contains("CENTRAL"));
        assertTrue(exception.getMessage().contains("LOCAL"));
    }
}
