package pt.uminho.taki.api.global;

import io.javalin.Javalin;
import pt.uminho.taki.api.ApiBootstrapHelper;
import pt.uminho.taki.dao.CategoriaDAO;
import pt.uminho.taki.dao.DevolucaoDAO;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.LojaDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.dao.FaturaDAO;
import pt.uminho.taki.dao.PromocaoDAO;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.ln.ITakiLNGlobal;
import pt.uminho.taki.ln.fatura.FaturaService;
import pt.uminho.taki.ln.report.RelatorioService;
import pt.uminho.taki.ln.TakiLNGlobal;
import pt.uminho.taki.ln.estatisticas.ISubSistemaEstatisticas;
import pt.uminho.taki.ln.estatisticas.SubSistemaEstatisticas;
import pt.uminho.taki.ln.fornecimentos.ISubSistemaFornecimentos;
import pt.uminho.taki.ln.fornecimentos.SubSistemaFornecimentos;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.SubSistemaLojas;
import pt.uminho.taki.ln.view.ISubSistemaView;
import pt.uminho.taki.ln.view.SubSistemaView;

import java.util.Arrays;
import java.util.Objects;

/**
 * Ponto de entrada da aplicação para a API Global.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class GlobalApiApp {

    private static final String VAR_PORTA = "TAKI_GLOBAL_API_PORT";
    private static final int PORTA_OMISSAO = 8081;

    private final ITakiLNGlobal takiLNGlobal;
    private final Javalin app;

    /**
     * Constrói uma nova instância de GlobalApiApp com a fachada global por omissão.
     */
    public GlobalApiApp() {
        this(criarFachadaGlobal());
    }

    /**
     * Constrói uma nova instância de GlobalApiApp.
     *
     * @param takiLNGlobal a fachada global
     */
    public GlobalApiApp(ITakiLNGlobal takiLNGlobal) {
        this.takiLNGlobal = Objects.requireNonNull(takiLNGlobal, "takiLNGlobal");
        String[] corsOrigins = parseCorsOrigins(System.getenv().getOrDefault("TAKI_CORS_ORIGIN", "http://localhost:5173"));
        this.app = Javalin.create(config -> {
            config.jsonMapper(ApiBootstrapHelper.criarJsonMapper());
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    if (corsOrigins.length == 1) {
                        it.allowHost(corsOrigins[0]);
                    } else {
                        it.allowHost(corsOrigins[0], Arrays.copyOfRange(corsOrigins, 1, corsOrigins.length));
                    }
                    it.allowCredentials = true;
                });
            });
        });
        ApiBootstrapHelper.configurarCamadaPartilhada(this.app);
        configurarRotasBase();
    }

    private static ITakiLNGlobal criarFachadaGlobal() {
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        LojaDAO lojaDAO = new LojaDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        FornecedorDAO fornecedorDAO = new FornecedorDAO();
        EncomendaDAO encomendaDAO = new EncomendaDAO();
        ProdutoFornecedorDAO produtoFornecedorDAO = new ProdutoFornecedorDAO();
        VendaDAO vendaDAO = new VendaDAO();
        InventarioDAO inventarioDAO = new InventarioDAO();

        ISubSistemaLojas subSistemaLojas = new SubSistemaLojas(funcionarioDAO, lojaDAO, categoriaDAO, produtoDAO);
        ISubSistemaFornecimentos subSistemaFornecimentos = new SubSistemaFornecimentos(fornecedorDAO, encomendaDAO, produtoDAO, produtoFornecedorDAO);
        ISubSistemaEstatisticas subSistemaEstatisticas = new SubSistemaEstatisticas(vendaDAO, inventarioDAO, produtoDAO);
        ISubSistemaView subSistemaView = new SubSistemaView();

        return new TakiLNGlobal(subSistemaLojas, subSistemaFornecimentos, subSistemaEstatisticas, subSistemaView);
    }

    private void configurarRotasBase() {
        ApiBootstrapHelper.registarEndpointHealth(this.app);
        ProdutoDAO produtoDAO = new ProdutoDAO();
        VendaDAO vendaDAO = new VendaDAO();
        EncomendaDAO encomendaDAO = new EncomendaDAO();
        FornecedorDAO fornecedorDAO = new FornecedorDAO();
        InventarioDAO inventarioDAO = new InventarioDAO();
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        PromocaoDAO promocaoDAO = new PromocaoDAO();
        DevolucaoDAO devolucaoDAO = new DevolucaoDAO();
        pt.uminho.taki.dao.StatisticsDAO statisticsDAO = new pt.uminho.taki.dao.StatisticsDAO();
        FaturaService faturaService = new FaturaService(new FaturaDAO(), vendaDAO);
        RelatorioService relatorioService = new RelatorioService();
        GlobalApiController controller = new GlobalApiController(
                this.takiLNGlobal,
                ((TakiLNGlobal) this.takiLNGlobal).getSubSistemaLojas(),
                produtoDAO,
                vendaDAO,
                encomendaDAO,
                fornecedorDAO,
                inventarioDAO,
                funcionarioDAO,
                promocaoDAO,
                devolucaoDAO,
                statisticsDAO,
                faturaService,
                relatorioService
        );
        GlobalApiRoutes.registar(this.app, controller);
    }

    private static String[] parseCorsOrigins(String raw) {
        String[] tokens = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        return tokens.length == 0 ? new String[] { "http://localhost:5173" } : tokens;
    }

    /**
     * Inicia a aplicação.
     */
    public void iniciar() {
        this.app.start(ApiBootstrapHelper.resolverPorta(VAR_PORTA, PORTA_OMISSAO));
    }

    /**
     * Para a aplicação.
     */
    public void parar() {
        this.app.stop();
    }

    /**
     * Método principal.
     *
     * @param args os argumentos
     */
    public static void main(String[] args) {
        new GlobalApiApp().iniciar();
    }
}
