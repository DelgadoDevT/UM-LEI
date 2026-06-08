package pt.uminho.taki.api.local;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi;

import java.util.Objects;

/**
 * Configura as rotas para a API Local.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public final class LocalApiRoutes {


    private static final String[] ROLES_GERENTE = {
            "GERENTE",
            "GERENTE DE LOJA",
            "ADMIN",
            "ADMINISTRADOR",
            "ADMINISTRADOR DO SISTEMA",
            "PROPRIETARIO DA CADEIA",
            "PROPRIETÁRIO DA CADEIA",
            "GESTOR_CENTRAL",
            "GESTOR_LOJA"
    };

    private LocalApiRoutes() {
    }

    /**
     * Regista as rotas para a API Local na aplicação Javalin fornecida.
     *
     * @param app a aplicação Javalin
     * @param controller o controlador para gerir os pedidos
     */
    public static void registar(Javalin app, LocalApiController controller) {
        Objects.requireNonNull(app, "app");
        Objects.requireNonNull(controller, "controller");        // Auth
        app.post("/api/local/auth/login", controller::autenticar);

        Handler autenticado = MiddlewareSegurancaApi.exigirAutenticacao();

        app.post("/api/local/auth/logout", proteger(controller::logout, autenticado));
        app.get("/api/local/auth/me", proteger(controller::obterSessaoAtual, autenticado));
        app.post("/api/local/auth/verify-password", proteger(controller::verificarPassword, autenticado));
        Handler gerente = MiddlewareSegurancaApi.exigirQualquerRole(ROLES_GERENTE);

        // Produtos
        app.get("/api/local/produtos", proteger(controller::listarProdutos, autenticado));
        app.post("/api/local/produtos", proteger(controller::adicionarProduto, autenticado, gerente));
        app.patch("/api/local/produtos/{idProduto}", proteger(controller::editarProduto, autenticado, gerente));
        app.patch("/api/local/produtos/{idProduto}/inativar", proteger(controller::inativarProduto, autenticado, gerente));

        // Categorias
        app.get("/api/local/categorias", proteger(controller::listarCategorias, autenticado));
        app.post("/api/local/categorias", proteger(controller::adicionarCategoria, autenticado, gerente));
        app.patch("/api/local/categorias/{idCategoria}", proteger(controller::editarCategoria, autenticado, gerente));
        app.patch("/api/local/categorias/{idCategoria}/inativar", proteger(controller::inativarCategoria, autenticado, gerente));

        // Funcionários
        app.get("/api/local/funcionarios", proteger(controller::listarFuncionarios, autenticado));
        app.post("/api/local/funcionarios", proteger(controller::registarFuncionario, autenticado, gerente));
        app.patch("/api/local/funcionarios/{idFuncionario}", proteger(controller::atualizarFuncionario, autenticado, gerente));
        app.post("/api/local/funcionarios/{idFuncionario}/bloquear", proteger(controller::bloquearFuncionario, autenticado, gerente));
        app.post("/api/local/funcionarios/{idFuncionario}/desbloquear", proteger(controller::desbloquearFuncionario, autenticado, gerente));
        app.delete("/api/local/funcionarios/{idFuncionario}", proteger(controller::removerFuncionario, autenticado, gerente));
        app.patch("/api/local/funcionarios/{idFuncionario}/perfil", proteger(controller::atribuirPerfilFuncionario, autenticado, gerente));

        // Inventário
        app.post("/api/local/inventario/movimentos", proteger(controller::registarMovimentoInventario, autenticado, gerente));
        app.get("/api/local/inventario", proteger(controller::listarInventario, autenticado));

        // Fornecedores e Encomendas
        app.get("/api/local/fornecedores", proteger(controller::listarFornecedores, autenticado));
        app.post("/api/local/fornecedores", proteger(controller::adicionarFornecedor, autenticado, gerente));
        app.patch("/api/local/fornecedores/{idFornecedor}", proteger(controller::editarFornecedor, autenticado, gerente));
        app.patch("/api/local/fornecedores/{idFornecedor}/inativar", proteger(controller::inativarFornecedor, autenticado, gerente));
        app.post("/api/local/fornecedores/associar", proteger(controller::associarProdutoAFornecedor, autenticado, gerente));
        app.get("/api/local/fornecedores/{idFornecedor}/produtos", proteger(controller::consultarProdutosDoFornecedor, autenticado));
        app.get("/api/local/encomendas", proteger(controller::listarEncomendas, autenticado));
        app.post("/api/local/encomendas", proteger(controller::criarEncomenda, autenticado, gerente));
        app.patch("/api/local/encomendas/{idEncomenda}/estado", proteger(controller::processarTransicaoEstadoEncomenda, autenticado, gerente));

        // Vendas
        app.post("/api/local/vendas/iniciar", proteger(controller::iniciarVenda, autenticado));
        app.post("/api/local/vendas/{idVenda}/linhas", proteger(controller::adicionarLinhaVenda, autenticado));
        app.post("/api/local/vendas/{idVenda}/registar", proteger(controller::registarVenda, autenticado));
        app.get("/api/local/vendas", proteger(controller::listarVendas, autenticado));
        app.post("/api/local/vendas/{idVenda}/devolucoes", proteger(controller::processarDevolucao, autenticado));
        app.get("/api/local/devolucoes", proteger(controller::listarDevolucoes, autenticado));
        app.get("/api/local/devolucoes/linhas", proteger(controller::obterLinhasDevolvidas, autenticado));

        // Promoções
        app.get("/api/local/promocoes/ativas", proteger(controller::listarPromocoesAtivas, autenticado));
        app.post("/api/local/promocoes", proteger(controller::adicionarPromocao, autenticado, gerente));
        app.post("/api/local/promocoes/{idPromocao}/cancelar", proteger(controller::cancelarPromocao, autenticado, gerente));

        // Perfis de acesso
        app.get("/api/local/perfis", proteger(controller::listarPerfis, autenticado));
        app.post("/api/local/perfis", proteger(controller::registarPerfil, autenticado, gerente));
        app.patch("/api/local/perfis/{nomePerfil}", proteger(controller::editarPerfil, autenticado, gerente));

        // Sincronização
        app.post("/api/local/sincronizacao/executar", proteger(controller::sincronizarDados, autenticado, gerente));
        app.get("/api/local/sincronizacao/disponibilidade", proteger(controller::consultarEstadoSincronizacao, autenticado));

        // Estatísticas
        app.get("/api/local/estatisticas/kpi", proteger(controller::obterKpiGerais, autenticado));
        app.get("/api/local/estatisticas/vendas-mensais", proteger(controller::obterVendasMensais, autenticado));
        app.get("/api/local/estatisticas/vendas-hora", proteger(controller::obterVendasPorHora, autenticado));
        app.get("/api/local/estatisticas/vendas-categoria", proteger(controller::obterVendasPorCategoria, autenticado));

        // Informação do nó
        app.get("/api/local/node/role", proteger(controller::getNodeRole, autenticado));


        // Faturas e SAF-T
        app.get("/api/local/faturas/saft", proteger(controller::gerarSaft, autenticado));
        app.get("/api/local/faturas/{idFatura}/pdf", proteger(controller::gerarSegundaViaPDF, autenticado));

        // Relatórios PDF
        app.get("/api/local/relatorios/vendas/pdf", proteger(controller::exportarRelatorioVendasPDF, autenticado));
        app.get("/api/local/relatorios/inventario/pdf", proteger(controller::exportarRelatorioInventarioPDF, autenticado));
    }

    /**
     * Aplica uma sequência de guardas (handlers de segurança) a um endpoint.
     *
     * @param endpoint o handler final da operação
     * @param guards os handlers de segurança a executar previamente
     * @return um novo handler protegido
     */
    private static Handler proteger(Handler endpoint, Handler... guards) {
        return ctx -> {
            for (Handler guard : guards) {
                guard.handle(ctx);
            }
            endpoint.handle(ctx);
        };
    }
}
