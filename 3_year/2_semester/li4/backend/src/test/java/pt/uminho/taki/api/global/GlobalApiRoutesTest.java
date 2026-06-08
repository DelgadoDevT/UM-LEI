package pt.uminho.taki.api.global;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários de binding de rotas da API Global utilizando JavalinTest e Mockito.
 * 
 * @author TakiLN Team
 */
class GlobalApiRoutesTest {

    private Javalin app;
    private GlobalApiController controllerMock;
    private MockedStatic<MiddlewareSegurancaApi> mockedSeguranca;

    @BeforeEach
    void setUp() {
        mockedSeguranca = Mockito.mockStatic(MiddlewareSegurancaApi.class);
        mockedSeguranca.when(MiddlewareSegurancaApi::exigirAutenticacao).thenReturn((io.javalin.http.Handler) ctx -> {});
        mockedSeguranca.when(() -> MiddlewareSegurancaApi.exigirQualquerRole(any(String[].class))).thenReturn((io.javalin.http.Handler) ctx -> {});
        
        controllerMock = Mockito.mock(GlobalApiController.class);
        app = Javalin.create();
        GlobalApiRoutes.registar(app, controllerMock);
    }

    @AfterEach
    void tearDown() {
        mockedSeguranca.close();
    }

    @Test
    void rotaFuncionario_post_invocaRegistarFuncionarioCompat() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/global/funcionarios", "{}");
            System.out.println("Status: " + response.code());
            System.out.println("Body: " + (response.body() != null ? response.body().string() : "null"));
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).registarFuncionarioCompat(any());
        });
    }

    @Test
    void rotaFuncionario_patch_invocaAtualizarFuncionarioCompat() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.patch("/api/global/funcionarios/123", "{}");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).atualizarFuncionarioCompat(any());
        });
    }

    @Test
    void rotaFuncionario_delete_invocaRemoverFuncionarioCompat() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.delete("/api/global/funcionarios/123");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).removerFuncionarioCompat(any());
        });
    }

    @Test
    void rotaFuncionarioBloquear_post_invocaBloquearFuncionarioCompat() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/global/funcionarios/123/bloquear");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).bloquearFuncionarioCompat(any());
        });
    }

    @Test
    void rotaPerfil_post_invocaRegistarPerfil() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/global/perfis", "{}");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).registarPerfil(any());
        });
    }

    @Test
    void rotaPerfil_patch_invocaEditarPerfil() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.patch("/api/global/perfis/GERENTE", "{}");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).editarPerfil(any());
        });
    }

    @Test
    void rotaLojas_get_invocaListarLojas() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/api/global/lojas");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).listarLojas(any());
        });
    }

    @Test
    void rotaLojas_post_invocaRegistarLoja() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/global/lojas", "{}");
            assertNotEquals(501, response.code(), "Não deve retornar 501 Não Implementado");
            verify(controllerMock).registarLoja(any());
        });
    }
}
