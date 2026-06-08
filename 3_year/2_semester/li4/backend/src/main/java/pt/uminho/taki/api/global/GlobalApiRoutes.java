package pt.uminho.taki.api.global;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi;

import java.util.Objects;

/**
 * Registo das rotas da API Global.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public final class GlobalApiRoutes {

    /**
     * Lista de perfis com permissão para operações de gestão global.
     */
    private static final String[] ROLES_GESTAO_GLOBAL = {
            "ADMIN",
            "ADMINISTRADOR",
            "ADMINISTRADOR DO SISTEMA",
            "PROPRIETARIO DA CADEIA",
            "PROPRIETÁRIO DA CADEIA",
            "GESTOR_CENTRAL"
    };

    /**
     * Lista de perfis com permissão para operações de leitura global.
     */
    private static final String[] ROLES_LEITURA_GLOBAL = {
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

    /**
     * Construtor privado para evitar a instanciação de uma classe utilitária.
     */
    private GlobalApiRoutes() {
    }

    /**
     * Regista as rotas da API Global.
     *
     * @param app a aplicação Javalin
     * @param controller o controlador da API Global
     */
    public static void registar(Javalin app, GlobalApiController controller) {
        Objects.requireNonNull(app, "app");
        Objects.requireNonNull(controller, "controller");

        // Auth
        app.post("/api/global/auth/login", controller::autenticar);

        Handler autenticado   = MiddlewareSegurancaApi.exigirAutenticacao();

        app.post("/api/global/auth/logout", proteger(controller::logout, autenticado));
        app.get("/api/global/auth/me", proteger(controller::obterSessaoAtual, autenticado));
        app.post("/api/global/auth/verify-password", proteger(controller::verificarPassword, autenticado));
        Handler leituraGlobal = MiddlewareSegurancaApi.exigirQualquerRole(ROLES_LEITURA_GLOBAL);
        Handler gestaoGlobal  = MiddlewareSegurancaApi.exigirQualquerRole(ROLES_GESTAO_GLOBAL);

        // RF01/RF02 + RNF01: CRUD de lojas — gestão centralizada da cadeia de lojas.
        app.get("/api/global/lojas",              proteger(controller::listarLojas,   autenticado, leituraGlobal));
        app.get("/api/global/lojas/{idLoja}",     proteger(controller::buscarLoja,    autenticado, leituraGlobal));
        app.post("/api/global/lojas",             proteger(controller::registarLoja,  autenticado, gestaoGlobal));
        app.patch("/api/global/lojas/{idLoja}",   proteger(controller::atualizarLoja, autenticado, gestaoGlobal));
        app.delete("/api/global/lojas/{idLoja}",  proteger(controller::removerLoja,   autenticado, gestaoGlobal));

        // RF11-RF15 + RNF01: gestão de catálogo e operações de administração de produtos/categorias.
        app.get("/api/global/catalogo/categorias",                       proteger(controller::listarCategorias,             autenticado, leituraGlobal));
        app.post("/api/global/catalogo/categorias",                      proteger(controller::adicionarCategoria,           autenticado, gestaoGlobal));
        app.post("/api/global/catalogo/produtos",                        proteger(controller::adicionarProduto,             autenticado, gestaoGlobal));
        app.get("/api/global/catalogo/produtos/{codigoBarras}",          proteger(controller::pesquisarProdutoPorCodigoBarras, autenticado, leituraGlobal));
        app.patch("/api/global/catalogo/produtos/{idProduto}/inativar",  proteger(controller::inativarProduto,              autenticado, gestaoGlobal));

        // RF19-RF20 + RNF01/RNF08: gestão de contas de funcionários com operações administrativas privilegiadas.
        app.post("/api/global/admin/funcionarios",                              proteger(controller::registarFuncionario,     autenticado, gestaoGlobal));
        app.post("/api/global/admin/funcionarios/{idFuncionario}/bloquear",     proteger(controller::bloquearContaFuncionario,    autenticado, gestaoGlobal));
        app.get("/api/global/admin/funcionarios",                               proteger(controller::listarFuncionarios, autenticado, gestaoGlobal));

        // Rotas compatíveis para endpoints /funcionarios e /perfis sem /admin (mapeamento correto aos métodos compat no controller)
        app.get("/api/global/funcionarios",                                     proteger(controller::listarFuncionarios, autenticado, leituraGlobal));
        app.post("/api/global/funcionarios",                                    proteger(controller::registarFuncionarioCompat, autenticado, gestaoGlobal));
        app.patch("/api/global/funcionarios/{idFuncionario}",                   proteger(controller::atualizarFuncionarioCompat, autenticado, gestaoGlobal));
        app.post("/api/global/funcionarios/{idFuncionario}/bloquear",           proteger(controller::bloquearFuncionarioCompat, autenticado, gestaoGlobal));
        app.post("/api/global/funcionarios/{idFuncionario}/desbloquear",        proteger(controller::desbloquearFuncionarioCompat, autenticado, gestaoGlobal));
        app.delete("/api/global/funcionarios/{idFuncionario}",                  proteger(controller::removerFuncionarioCompat, autenticado, gestaoGlobal));
        app.patch("/api/global/funcionarios/{idFuncionario}/perfil",            proteger(controller::atribuirPerfilFuncionarioCompat, autenticado, gestaoGlobal));

        app.get("/api/global/perfis",                                           proteger(controller::listarPerfis, autenticado, leituraGlobal));
        app.post("/api/global/perfis",                                          proteger(controller::registarPerfil, autenticado, gestaoGlobal));
        app.patch("/api/global/perfis/{nomePerfil}",                            proteger(controller::editarPerfil, autenticado, gestaoGlobal));

        // RF11-RF15 + RNF01: gestão de fornecedores centralizada.
        app.post("/api/global/fornecedores",                          proteger(controller::adicionarFornecedor,          autenticado, gestaoGlobal));
        app.patch("/api/global/fornecedores/{idFornecedor}/inativar", proteger(controller::inativarFornecedor,           autenticado, gestaoGlobal));
        app.get("/api/global/fornecedores",                           proteger(controller::listarFornecedores, autenticado, leituraGlobal));
        app.get("/api/global/produtos",                               proteger(controller::listarProdutos, autenticado, leituraGlobal));
        app.get("/api/global/vendas",                                 proteger(controller::listarVendas, autenticado, leituraGlobal));
        app.get("/api/global/devolucoes",                             proteger(controller::listarDevolucoes, autenticado, leituraGlobal));
        app.get("/api/global/encomendas",                             proteger(controller::listarEncomendas, autenticado, leituraGlobal));
        app.get("/api/global/promocoes/ativas",                       proteger(controller::listarPromocoesAtivasCompat, autenticado, leituraGlobal));
        app.post("/api/global/promocoes",                              proteger(controller::adicionarPromocaoCompat, autenticado, gestaoGlobal));
        app.post("/api/global/promocoes/{idPromocao}/cancelar",        proteger(controller::cancelarPromocaoCompat, autenticado, gestaoGlobal));

        // RF09 + RNF01/RNF03: emissão de segunda via de fatura, SAF-T (PT) e relatórios PDF agregados.
        app.get("/api/global/faturas/saft",                proteger(controller::gerarSaft, autenticado, leituraGlobal));
        app.get("/api/global/faturas/{idFatura}/pdf",      proteger(controller::gerarSegundaViaPDF, autenticado, leituraGlobal));
        app.get("/api/global/relatorios/vendas/pdf",       proteger(controller::exportarRelatorioVendasPDF, autenticado, leituraGlobal));
        app.get("/api/global/relatorios/inventario/pdf",   proteger(controller::exportarRelatorioInventarioPDF, autenticado, leituraGlobal));

        // RF16-RF18 + RNF01/RNF06: reporting e estatísticas globais.
        app.get("/api/global/relatorios/vendas",       proteger(controller::gerarRelatorioVendas,  autenticado, leituraGlobal));
        app.get("/api/global/relatorios/vendas-mensais", proteger(controller::obterVendasMensais, autenticado, leituraGlobal));
        app.get("/api/global/relatorios/inventario",   proteger(controller::gerarRelatorioInventario, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/volume-vendas", proteger(controller::calcularVolumeVendas, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/ticket-medio",  proteger(controller::calcularTicketMedio,  autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/dashboard-kpis", proteger(controller::gerarDashboardKpis, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/kpi-gerais", proteger(controller::obterKpiGerais, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/vendas-mensais", proteger(controller::obterVendasMensais, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/vendas-horarias", proteger(controller::obterVendasPorHora, autenticado, leituraGlobal));
        app.get("/api/global/estatisticas/vendas-categoria", proteger(controller::obterVendasPorCategoria, autenticado, leituraGlobal));

        // RF18 + RNF02/RNF07: atualização de vista global para consolidação operacional.
        app.post("/api/global/view/atualizar", proteger(controller::atualizarView, autenticado, gestaoGlobal));

        // RNF02/RNF04: endpoints planeados de sincronização global ainda não expostos no boundary ITakiLNGlobal.
        app.post("/api/global/sincronizacao/exportar", proteger(controller::sincronizacaoExportacaoNaoSuportada, autenticado, gestaoGlobal));
        app.post("/api/global/sincronizacao/importar", proteger(controller::sincronizacaoImportacaoNaoSuportada, autenticado, gestaoGlobal));
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
