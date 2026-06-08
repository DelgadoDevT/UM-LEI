package pt.uminho.taki.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import pt.uminho.taki.api.shared.erros.TratadorGlobalExcecoesApi;
import pt.uminho.taki.api.shared.seguranca.MiddlewareReadOnlyCentral;
import pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi;

import java.util.Objects;

/**
 * Classe auxiliar para a inicialização da API.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public final class ApiBootstrapHelper {

    private ApiBootstrapHelper() {
    }

    /**
     * Resolve o porto a partir de uma variável de ambiente ou retorna o porto por omissão.
     *
     * @param nomeVariavelAmbiente o nome da variável de ambiente
     * @param portaOmissao o porto por omissão
     * @return o porto resolvido
     */
    public static int resolverPorta(String nomeVariavelAmbiente, int portaOmissao) {
        String portaEnv = System.getenv(nomeVariavelAmbiente);
        if (portaEnv == null || portaEnv.isBlank()) {
            return portaOmissao;
        }

        try {
            int porta = Integer.parseInt(portaEnv.trim());
            if (porta < 1 || porta > 65535) {
                return portaOmissao;
            }
            return porta;
        } catch (NumberFormatException e) {
            return portaOmissao;
        }
    }

    /**
     * Regista o endpoint de estado de saúde (health).
     *
     * @param app a aplicação Javalin
     */
    public static void registarEndpointHealth(Javalin app) {
        app.get("/health", ctx -> ctx.result("OK"));
    }

    /**
     * Cria um JsonMapper tolerante a campos JSON desconhecidos.
     * Evita que payloads do frontend com campos extra rebentem a desserialização.
     */
    public static JavalinJackson criarJsonMapper() {
        return new JavalinJackson().updateMapper(mapper ->
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        );
    }

    /**
     * Configura a camada partilhada.
     *
     * @param app a aplicação Javalin
     */
    public static void configurarCamadaPartilhada(Javalin app) {
        Objects.requireNonNull(app, "app");
        
        app.before(ctx -> {
            ctx.header("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            ctx.header("Pragma", "no-cache");
            ctx.header("Expires", "0");
        });
        
        TratadorGlobalExcecoesApi.registar(app);
        MiddlewareSegurancaApi.registar(app);
        MiddlewareReadOnlyCentral.registar(app);
    }
}
