package pt.uminho.taki.performance;

import org.junit.jupiter.api.*;
import pt.uminho.taki.api.local.LocalApiApp;
import pt.uminho.taki.ln.ITakiLNLocal;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Testes de Carga para o Sistema Taki.
 * Simula um cenário de utilização normal elevada (50 utilizadores simultâneos).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadPerformanceTest {

    private LocalApiApp app;
    private final String BASE_URL = "http://localhost:8080";
    private final int CONCURRENT_USERS = 50;
    private final int REQUESTS_PER_USER = 20;

    @BeforeAll
    void setup() {
        // Inicia a aplicação localmente
        app = new LocalApiApp(new TakiLNLocal());
        app.iniciar();
    }

    @AfterAll
    void tearDown() {
        app.parar();
    }

    @Test
    @DisplayName("Carga: Simulação de 50 Operadores Simultâneos")
    void testLoadConcurrentSales() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalLatency = new AtomicLong(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        long testStartTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                try {
                    // 1. Autenticação
                    String loginJson = "{\"email\":\"tiago.rocha@taki.pt\", \"password\":\"admin\"}";
                    HttpRequest loginReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/local/auth/login"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                            .build();

                    long start = System.currentTimeMillis();
                    HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
                    totalLatency.addAndGet(System.currentTimeMillis() - start);

                    if (loginRes.statusCode() == 200) {
                        // Extrair token do corpo (formato: {"token":"...", ...})
                        String body = loginRes.body();
                        String token = "";
                        int tokenIdx = body.indexOf("\"token\":\"");
                        if (tokenIdx != -1) {
                            token = body.substring(tokenIdx + 9, body.indexOf("\"", tokenIdx + 9));
                        }

                        // 2. Operações Repetitivas
                        for (int j = 0; j < REQUESTS_PER_USER; j++) {
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(BASE_URL + "/api/local/produtos"))
                                    .header("Authorization", "Bearer " + token)
                                    .GET()
                                    .build();

                            long reqStart = System.currentTimeMillis();
                            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                            totalLatency.addAndGet(System.currentTimeMillis() - reqStart);

                            if (res.statusCode() == 200) {
                                successCount.incrementAndGet();
                            } else {
                                // Log erro ocasional para debug se necessário
                                // System.err.println("Erro " + res.statusCode() + ": " + res.body());
                                failureCount.incrementAndGet();
                            }
                        }
                    } else {
                        failureCount.addAndGet(REQUESTS_PER_USER);
                    }
                } catch (Exception e) {
                    failureCount.addAndGet(REQUESTS_PER_USER);
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long testDuration = System.currentTimeMillis() - testStartTime;

        printResults("TESTE DE CARGA", successCount.get(), failureCount.get(), testDuration, totalLatency.get());
        
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }

    private void printResults(String title, int success, int failure, long duration, long totalLat) {
        int totalRequests = success + failure;
        double successRate = (double) success / totalRequests * 100;
        double errorRate = (double) failure / totalRequests * 100;
        String status = (errorRate < 1.0) ? "ESTÁVEL" : "INSTÁVEL";

        System.out.println("\n============================================================================");
        System.out.println(title);
        System.out.println("============================================================================");
        System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-12s |\n", "Métrica", "Sucesso %", "Taxa Erro %", "Duração", "Status");
        System.out.println("|-----------------|--------------|--------------|--------------|--------------|");
        System.out.printf("| %-15s | %-12.1f%% | %-12.1f%% | %-10d ms | %-12s |\n", 
                        "Performance", successRate, errorRate, duration, status);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Utilizadores Concurrentes : " + CONCURRENT_USERS);
        System.out.println("Pedidos Totais            : " + totalRequests);
        System.out.println("Throughput                : " + String.format("%.2f", (double)totalRequests / (duration/1000.0)) + " req/s");
        System.out.println("Latência Média            : " + (totalRequests > 0 ? (totalLat / totalRequests) : 0) + " ms");
        System.out.println("========================================\n");
    }
}
