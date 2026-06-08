package pt.uminho.taki.api.local;

import io.javalin.Javalin;
import pt.uminho.taki.api.ApiBootstrapHelper;
import pt.uminho.taki.ln.ITakiLNLocal;
import pt.uminho.taki.ln.TakiLNLocal;

import java.util.Arrays;
import java.util.Objects;

/**
 * Ponto de entrada da aplicação para a API Local.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class LocalApiApp {

    private static final String VAR_PORTA = "TAKI_LOCAL_API_PORT";
    private static final int PORTA_OMISSAO = 8080;

    private final ITakiLNLocal takiLNLocal;
    private final Javalin app;

    /**
     * Construtor por omissão para LocalApiApp.
     */
    public LocalApiApp() {
        this(new TakiLNLocal());
    }

    /**
     * Construtor parametrizado para LocalApiApp.
     *
     * @param takiLNLocal a instância da camada de lógica local
     */
    public LocalApiApp(ITakiLNLocal takiLNLocal) {
        this.takiLNLocal = Objects.requireNonNull(takiLNLocal, "takiLNLocal");
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
            config.requestLogger.http((ctx, ms) -> {
                System.out.println(ctx.method() + " " + ctx.path() + " - " + ctx.statusCode() + " (" + ms + "ms)");
                if (ctx.statusCode() >= 400) {
                    System.out.println("Request body: " + ctx.body());
                    System.out.println("Response body: " + ctx.result());
                }
            });
        });
        ApiBootstrapHelper.configurarCamadaPartilhada(this.app);
        configurarRotasBase();
    }

    private void configurarRotasBase() {
        ApiBootstrapHelper.registarEndpointHealth(this.app);
        pt.uminho.taki.dao.StatisticsDAO statisticsDAO = new pt.uminho.taki.dao.StatisticsDAO();
        pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO = new pt.uminho.taki.dao.DevolucaoDAO();
        pt.uminho.taki.ln.report.RelatorioService relatorioService = new pt.uminho.taki.ln.report.RelatorioService();
        LocalApiController controller = new LocalApiController(this.takiLNLocal, statisticsDAO, devolucaoDAO, relatorioService);
        LocalApiRoutes.registar(this.app, controller);
    }

    private static String[] parseCorsOrigins(String raw) {
        String[] tokens = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        return tokens.length == 0 ? new String[] { "http://localhost:5173" } : tokens;
    }

    /**
     * Inicia o servidor da API local.
     */
    public void iniciar() {
        this.app.start(ApiBootstrapHelper.resolverPorta(VAR_PORTA, PORTA_OMISSAO));
    }

    /**
     * Para o servidor da API local.
     */
    public void parar() {
        this.app.stop();
    }

    /**
     * Método principal para iniciar a aplicação da API local.
     *
     * @param args argumentos de linha de comandos
     */
    public static void main(String[] args) {
        new LocalApiApp().iniciar();
    }
}
