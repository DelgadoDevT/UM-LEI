package pt.uminho.taki.performance;

import org.junit.jupiter.api.*;
import pt.uminho.taki.api.local.LocalApiApp;
import pt.uminho.taki.ln.TakiLNLocal;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testes de Stress para o Sistema Taki.
 * Procura encontrar o ponto de rutura aumentando progressivamente a carga.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StressPerformanceTest {

    private LocalApiApp app;
    private final String BASE_URL = "http://localhost:8080";
    private final int INITIAL_USERS = 50;
    private final int STEP_USERS = 50;
    private final int MAX_USERS = 500;

    @BeforeAll
    void setup() {
        app = new LocalApiApp(new TakiLNLocal());
        app.iniciar();
    }

    @AfterAll
    void tearDown() {
        app.parar();
    }

    @Test
    @DisplayName("Stress: Procura do Ponto de Rutura")
    void testStressSaturation() throws InterruptedException {
        List<String> summaryLines = new ArrayList<>();
        summaryLines.add(String.format("| %-12s | %-12s | %-12s | %-12s | %-12s |", "Utilizadores", "Sucesso %", "Taxa Erro %", "Tempo Total", "Status"));
        summaryLines.add("|--------------|--------------|--------------|--------------|--------------|");

        for (int users = INITIAL_USERS; users <= MAX_USERS; users += STEP_USERS) {
            int currentUsers = users;
            ExecutorService executor = Executors.newFixedThreadPool(currentUsers);
            AtomicInteger success = new AtomicInteger(0);
            AtomicInteger errors = new AtomicInteger(0);
            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            long start = System.currentTimeMillis();

            for (int i = 0; i < currentUsers; i++) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        HttpClient client = HttpClient.newBuilder()
                                .cookieHandler(new CookieManager())
                                .connectTimeout(Duration.ofSeconds(10))
                                .build();

                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(BASE_URL + "/api/local/auth/login"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"tiago.rocha@taki.pt\", \"password\":\"admin\"}"))
                                .build();

                        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                        if (res.statusCode() == 200) success.incrementAndGet();
                        else errors.incrementAndGet();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            long duration = System.currentTimeMillis() - start;
            double successRate = (double) success.get() / (success.get() + errors.get()) * 100;
            double errorRate = (double) errors.get() / (success.get() + errors.get()) * 100;

            String status = "OK";
            if (errorRate > 10.0) status = "BROKEN";
            else if (duration > 5000) status = "SATURATED";

            System.out.printf("Utilizadores: %d | Sucesso: %d | Erros: %d | Tempo: %dms | Status: %s\n", 
                               currentUsers, success.get(), errors.get(), duration, status);

            summaryLines.add(String.format("| %-12d | %-12.1f%% | %-12.1f%% | %-10d ms | %-12s |", 
                                            currentUsers, successRate, errorRate, duration, status));

            executor.shutdown();
            
            if (errorRate > 10.0) {
                System.out.println(">>> PONTO DE RUTURA ATINGIDO! Taxa de erro superior a 10%.");
                break;
            }
        }

        System.out.println("\n============================================================================");
        System.out.println("RESUMO FINAL: TESTE DE STRESS");
        System.out.println("============================================================================");
        for (String line : summaryLines) {
            System.out.println(line);
        }
        System.out.println("============================================================================\n");
    }
}
