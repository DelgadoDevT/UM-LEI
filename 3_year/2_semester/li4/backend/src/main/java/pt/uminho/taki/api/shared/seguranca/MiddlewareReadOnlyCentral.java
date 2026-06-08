package pt.uminho.taki.api.shared.seguranca;

import io.javalin.Javalin;
import io.javalin.http.ForbiddenResponse;
import pt.uminho.taki.api.NodeRole;
import pt.uminho.taki.api.NodeRoleConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Middleware que impõe modo apenas-leitura quando APP_MODE=CENTRAL.
 * Bloqueia métodos de escrita (POST/PUT/PATCH/DELETE) com 403, exceto endpoints de autenticação.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public final class MiddlewareReadOnlyCentral {

    private static final Set<String> METODOS_LEITURA = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> SUFIXOS_AUTH_PERMITIDOS = Set.of(
            "/auth/login",
            "/auth/logout",
            "/auth/verify-password"
    );

    private MiddlewareReadOnlyCentral() {
    }

    /**
     * Regista o middleware na aplicação Javalin. Não tem efeito se o nó não for CENTRAL.
     *
     * @param app a aplicação Javalin.
     */
    public static void registar(Javalin app) {
        Objects.requireNonNull(app, "app");
        if (NodeRoleConfig.resolverRoleRuntime() != NodeRole.CENTRAL) {
            return;
        }

        app.before(ctx -> {
            String metodo = ctx.method().name().toUpperCase(Locale.ROOT);
            if (METODOS_LEITURA.contains(metodo)) {
                return;
            }
            String path = ctx.path() == null ? "" : ctx.path();
            for (String sufixo : SUFIXOS_AUTH_PERMITIDOS) {
                if (path.endsWith(sufixo)) {
                    return;
                }
            }
            throw new ForbiddenResponse(
                    "Modo central é apenas de leitura: operações de escrita não são permitidas."
            );
        });
    }
}
