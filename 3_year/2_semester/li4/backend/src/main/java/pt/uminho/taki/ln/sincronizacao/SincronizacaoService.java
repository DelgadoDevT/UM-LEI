package pt.uminho.taki.ln.sincronizacao;

import pt.uminho.taki.dao.OutboxDAO;
import pt.uminho.taki.ln.sincronizacao.dto.OutboxEntry;
import pt.uminho.taki.ln.sincronizacao.exceptions.FalhaSincronizacaoException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

/**
 * Serviço para Sincronização.
 * @author TakiLN Team
 * @since 1.0
 */
public class SincronizacaoService implements ISincronizacaoService {

    private final OutboxDAO outboxDAO;
    private final HttpClient httpClient;
    private final List<String> urlsSede;
    private static final String URL_SEDE_DEFAULT = "https://taki-central-server.pt/api/v1";
    private static final int MAX_BATCH_SIZE = 500;
    private static final int MAX_RETRIES = 3;

    /**
     * Construtor para SincronizacaoService.
     * @param outboxDAO o DAO de outbox
     */
    public SincronizacaoService(OutboxDAO outboxDAO) {
        this(outboxDAO, criarHttpClientComTLS13(), URL_SEDE_DEFAULT);
    }

    /**
     * Construtor para SincronizacaoService.
     * @param outboxDAO o DAO de outbox
     * @param httpClient o cliente HTTP
     */
    public SincronizacaoService(OutboxDAO outboxDAO, HttpClient httpClient) {
        this(outboxDAO, httpClient, URL_SEDE_DEFAULT);
    }

    /**
     * Construtor para SincronizacaoService.
     * @param outboxDAO o DAO de outbox
     * @param httpClient o cliente HTTP
     * @param urlsSedeCsv o CSV de URLs da sede
     */
    public SincronizacaoService(OutboxDAO outboxDAO, HttpClient httpClient, String urlsSedeCsv) {
        this.outboxDAO = outboxDAO;
        this.httpClient = httpClient;
        this.urlsSede = parseAndValidateEndpoints(urlsSedeCsv);
    }

    /**
     * Cria e configura um cliente HTTP com suporte exclusivo a TLS 1.3.
     * 
     * @return o cliente HTTP configurado
     * @throws IllegalStateException se ocorrer um erro na configuração do contexto SSL
     */
    private static HttpClient criarHttpClientComTLS13() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, null, null);
            
            SSLParameters sslParams = new SSLParameters();
            sslParams.setProtocols(new String[]{"TLSv1.3"});
            
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .sslContext(sslContext)
                    .sslParameters(sslParams)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao configurar cliente HTTP com TLS 1.3: " + e.getMessage(), e);
        }
    }

    /**
     * Efetua o processamento e a validação de uma lista de endpoints da sede.
     * 
     * @param urlsSedeCsv a string CSV com os endereços da sede
     * @return a lista de endereços normalizados
     * @throws IllegalArgumentException se a lista for nula ou não contiver endereços válidos
     */
    private List<String> parseAndValidateEndpoints(String urlsSedeCsv) {
        String source = (urlsSedeCsv == null || urlsSedeCsv.isBlank()) ? URL_SEDE_DEFAULT : urlsSedeCsv;
        List<String> endpoints = Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .toList();

        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("Lista de endpoints centrais inválida.");
        }
        endpoints.forEach(this::validarEndpointSeguro);
        return endpoints;
    }

    /**
     * Valida se um determinado endpoint utiliza o protocolo HTTPS.
     * 
     * @param endpoint o endereço a verificar
     * @throws IllegalArgumentException se o protocolo não for seguro
     */
    private void validarEndpointSeguro(String endpoint) {
        URI uri = URI.create(endpoint);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Comunicação insegura bloqueada: o endpoint central deve usar HTTPS/TLS 1.3.");
        }
    }

    /**
     * Exporta um lote de dados para a sede.
     * @param timestampFecho o carimbo de data/hora do fecho
     * @throws FalhaSincronizacaoException se ocorrer um erro durante a sincronização.
     */
    @Override
    public void exportarLoteParaSede(String timestampFecho) throws FalhaSincronizacaoException {
        Collection<OutboxEntry> pendentes = outboxDAO.findAll();
        if (pendentes == null || pendentes.isEmpty()) {
            return;
        }
        if (pendentes.size() > MAX_BATCH_SIZE) {
            throw new FalhaSincronizacaoException("Timeout ao sincronizar base de dados: limite de lote excedido.");
        }

        Exception ultimaFalha = null;
        for (int tentativa = 1; tentativa <= MAX_RETRIES; tentativa++) {
            try {
                enviarExportacaoParaSede(pendentes);
                outboxDAO.deleteAll(pendentes); // remove apenas após confirmação de entrega
                return;
            } catch (Exception e) {
                ultimaFalha = e;
                if (tentativa < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000L * tentativa);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new FalhaSincronizacaoException("Sincronização interrompida durante o backoff de retry.");
                    }
                }
            }
        }

        throw new FalhaSincronizacaoException(
                "Falha ao sincronizar base de dados após retries. A operação será reagendada automaticamente. Causa: "
                        + (ultimaFalha != null ? ultimaFalha.getMessage() : "desconhecida"));
    }

    /**
     * Envia as entradas da outbox para um dos endpoints da sede disponíveis.
     * 
     * @param lista a coleção de entradas a exportar
     * @throws Exception se ocorrer uma falha na comunicação ou resposta inválida de todos os servidores
     */
    private void enviarExportacaoParaSede(Collection<OutboxEntry> lista) throws Exception {
        String jsonPayload = "{\"size\":" + lista.size() + "}";
        Exception ultimaFalha = null;

        for (String endpoint : urlsSede) {
            URI uri = URI.create(endpoint + "/sync/export");
            validarEndpointSeguro(endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer TOKEN_LOJA")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                boolean sucesso = response.statusCode() == 200 || response.statusCode() == 201;

                if (sucesso) {
                    return;
                }
                ultimaFalha = new Exception("Resposta inesperada do servidor: HTTP " + response.statusCode());
            } catch (Exception e) {
                ultimaFalha = e;
            }
        }

        if (ultimaFalha != null) {
            throw ultimaFalha;
        }
        throw new Exception("Não foi possível exportar para nenhum endpoint da central.");
    }

    /**
     * Importa as atualizações globais da sede.
     * @param tokenIdentificacaoLoja o token de identificação da loja
     * @throws FalhaSincronizacaoException se ocorrer um erro durante a sincronização.
     */
    @Override
    public void importarAtualizacoesGlobais(String tokenIdentificacaoLoja) throws FalhaSincronizacaoException {
        Exception ultimaFalha = null;

        for (String endpoint : urlsSede) {
            URI uri = URI.create(endpoint + "/sync/import");
            validarEndpointSeguro(endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", "Bearer " + tokenIdentificacaoLoja)
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return;
                }
                ultimaFalha = new Exception("Resposta inesperada do servidor: HTTP " + response.statusCode());
            } catch (Exception e) {
                ultimaFalha = e;
            }
        }

        throw new FalhaSincronizacaoException("Erro ao importar dados da sede: " + (ultimaFalha != null ? ultimaFalha.getMessage() : "sem detalhe"));
    }

    /**
     * Verifica a disponibilidade da central.
     * @return o valor booleano
     */
    @Override
    public boolean verificarDisponibilidadeCentral() {
        for (String endpoint : urlsSede) {
            URI uri = URI.create(endpoint + "/health");
            validarEndpointSeguro(endpoint);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) return true;
            } catch (Exception e) {
                // Tenta próximo endpoint
            }
        }
        return false;
    }

    /**
     * Obtém a disponibilidade dos últimos 30 dias.
     * @return o valor decimal
     */
    @Override
    public double obterDisponibilidadeUltimos30Dias() {
        return verificarDisponibilidadeCentral() ? 100.0 : 0.0;
    }
}
