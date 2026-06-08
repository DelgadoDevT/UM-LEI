package pt.uminho.taki.api;

import java.util.Locale;

/**
 * Classe de configuração para determinar a função do nó em tempo de execução.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public final class NodeRoleConfig {

    /**
     * O nome da variável de ambiente para configurar a função do nó.
     */
    public static final String VARIAVEL_AMBIENTE_NODE_ROLE = "APP_MODE";
    
    private static final NodeRole ROLE_OMISSAO = NodeRole.LOCAL;

    private NodeRoleConfig() {
    }

    /**
     * Resolve a função do nó em tempo de execução com base na variável de ambiente.
     *
     * @return a NodeRole resolvida.
     */
    public static NodeRole resolverRoleRuntime() {
        return resolverRoleRuntime(System.getenv(VARIAVEL_AMBIENTE_NODE_ROLE));
    }

    static NodeRole resolverRoleRuntime(String roleEnv) {
        // Omissao segura: quando o role nao vem no ambiente, arranca apenas a API local.
        if (roleEnv == null || roleEnv.isBlank()) {
            return ROLE_OMISSAO;
        }

        try {
            return NodeRole.valueOf(roleEnv.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Valor invalido para " + VARIAVEL_AMBIENTE_NODE_ROLE
                            + ": '" + roleEnv + "'. Valores suportados: CENTRAL, LOCAL.",
                    e
            );
        }
    }
}
