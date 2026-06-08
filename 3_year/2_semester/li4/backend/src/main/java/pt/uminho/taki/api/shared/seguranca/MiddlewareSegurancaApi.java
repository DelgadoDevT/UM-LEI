package pt.uminho.taki.api.shared.seguranca;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Middleware para proteger os pontos de extremidade da API através de autenticação JWT.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public final class MiddlewareSegurancaApi {

    private static final String VARIAVEL_CHAVE_PUBLICA = "TAKI_JWT_PUBLIC_KEY";
    private static final String CHAVE_CONTEXTO_AUTH = "taki.auth.context";

    /**
     * Construtor privado para impedir a instanciação desta classe utilitária.
     */
    private MiddlewareSegurancaApi() {
    }

    /**
     * Regista o middleware de segurança na aplicação Javalin fornecida.
     *
     * @param app a aplicação Javalin.
     */
    public static void registar(Javalin app) {
        Objects.requireNonNull(app, "app");
        String publicKey = System.getenv(VARIAVEL_CHAVE_PUBLICA);
        app.before(ctx -> carregarContextoAutenticacao(ctx, publicKey));
    }

    static void carregarContextoAutenticacao(Context ctx, String publicKey) {
        String tokenStr = ctx.cookie("auth_token");
        Optional<String> token = (tokenStr != null && !tokenStr.isBlank()) ? Optional.of(tokenStr) : JwtAuthUtils.extrairBearerToken(ctx.header("Authorization"));
        if (token.isEmpty()) {
            return;
        }
        try {
            ContextoAutenticacao contexto = JwtAuthUtils.validarToken(token.get(), publicKey);
            ctx.attribute(CHAVE_CONTEXTO_AUTH, contexto);
        } catch (TokenJwtInvalidoException e) {
            throw new UnauthorizedResponse(e.getMessage());
        }
    }

    /**
     * Obtém o contexto de autenticação a partir do contexto HTTP.
     *
     * @param ctx o contexto HTTP.
     * @return um Optional que contém o contexto de autenticação, caso esteja disponível.
     */
    public static Optional<ContextoAutenticacao> obterContexto(Context ctx) {
        return Optional.ofNullable(ctx.attribute(CHAVE_CONTEXTO_AUTH));
    }

    /**
     * Cria um manipulador que exige a autenticação do utilizador.
     *
     * @return o manipulador de autenticação.
     */
    public static Handler exigirAutenticacao() {
        return ctx -> obterContextoObrigatorio(ctx);
    }

    /**
     * Cria um manipulador que exige que o utilizador possua pelo menos uma das funções especificadas.
     *
     * @param roles as funções exigidas.
     * @return o manipulador de autorização de funções.
     */
    public static Handler exigirQualquerRole(String... roles) {
        Set<String> rolesObrigatorias = normalizarLista(roles, true, "roles");
        return ctx -> {
            ContextoAutenticacao contexto = obterContextoObrigatorio(ctx);
            boolean autorizado = contexto.getRoles().stream().anyMatch(rolesObrigatorias::contains);
            if (!autorizado) {
                throw new ForbiddenResponse("Permissões insuficientes para este recurso.");
            }
        };
    }

    /**
     * Cria um manipulador que exige que o utilizador possua todos os âmbitos especificados.
     *
     * @param scopes os âmbitos exigidos.
     * @return o manipulador de autorização de âmbitos.
     */
    public static Handler exigirScopes(String... scopes) {
        Set<String> scopesObrigatorios = normalizarLista(scopes, false, "scopes");
        return ctx -> {
            ContextoAutenticacao contexto = obterContextoObrigatorio(ctx);
            boolean autorizado = contexto.getScopes().containsAll(scopesObrigatorios);
            if (!autorizado) {
                throw new ForbiddenResponse("Scopes insuficientes para este recurso.");
            }
        };
    }

    /**
     * Obtém o contexto de autenticação de forma obrigatória.
     * 
     * @param ctx o contexto do Javalin
     * @return o contexto de autenticação validado
     * @throws UnauthorizedResponse se o contexto estiver ausente
     */
    private static ContextoAutenticacao obterContextoObrigatorio(Context ctx) {
        return obterContexto(ctx)
                .orElseThrow(() -> new UnauthorizedResponse("Autenticação obrigatória."));
    }

    /**
     * Normaliza uma lista de strings para critérios de validação.
     * 
     * @param valores os valores a normalizar
     * @param paraMaiusculas indica se deve ocorrer conversão para maiúsculas
     * @param nomeParametro o nome do parâmetro para mensagens de erro
     * @return um conjunto de strings normalizado e sem elementos vazios
     * @throws IllegalArgumentException se a lista resultante estiver vazia
     */
    private static Set<String> normalizarLista(String[] valores, boolean paraMaiusculas, String nomeParametro) {
        if (valores == null || valores.length == 0) {
            throw new IllegalArgumentException("É obrigatório indicar " + nomeParametro + " para o guard.");
        }
        Set<String> normalizado = new LinkedHashSet<>();
        Arrays.stream(valores)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .forEach(valor -> {
                    if (paraMaiusculas) {
                        normalizado.add(valor.toUpperCase(Locale.ROOT));
                    } else {
                        normalizado.add(valor);
                    }
                });
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar " + nomeParametro + " para o guard.");
        }
        return normalizado;
    }
}
