package pt.uminho.taki.api;

import pt.uminho.taki.api.global.GlobalApiApp;
import pt.uminho.taki.api.local.LocalApiApp;

import java.util.Objects;

/**
 * Lançador para a API do nó, responsável pelo início da API local ou de ambas as APIs local e global, consoante a função.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class NodeApiLauncher {

    private final LocalApiApp localApiApp;
    private final GlobalApiApp globalApiApp;

    /**
     * Construtor por omissão para NodeApiLauncher.
     */
    public NodeApiLauncher() {
        this(new LocalApiApp(), new GlobalApiApp());
    }

    /**
     * Construtor utilizado em contexto de testes de integração para injeção de dependências.
     *
     * @param localApiApp a aplicação da API local
     * @param globalApiApp a aplicação da API global
     */
    NodeApiLauncher(LocalApiApp localApiApp, GlobalApiApp globalApiApp) {
        this.localApiApp = Objects.requireNonNull(localApiApp, "localApiApp");
        this.globalApiApp = Objects.requireNonNull(globalApiApp, "globalApiApp");
    }

    /**
     * Inicia as aplicações de API com base na função do nó fornecida.
     *
     * @param role a função do nó para a qual iniciar as APIs.
     */
    public void iniciar(NodeRole role) {
        Objects.requireNonNull(role, "role");

        switch (role) {
            case LOCAL -> this.localApiApp.iniciar();
            case CENTRAL -> {
                // CENTRAL disponibiliza API local e global em simultâneo.
                this.localApiApp.iniciar();
                try {
                    this.globalApiApp.iniciar();
                } catch (Exception e) {
                    this.localApiApp.parar();
                    throw e;
                }
            }
        }
    }

    /**
     * Método principal para lançar a API.
     *
     * @param args os argumentos da linha de comandos.
     */
    public static void main(String[] args) {
        NodeRole role = NodeRoleConfig.resolverRoleRuntime();
        new NodeApiLauncher().iniciar(role);
    }
}
