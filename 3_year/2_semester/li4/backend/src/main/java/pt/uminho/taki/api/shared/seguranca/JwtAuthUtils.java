package pt.uminho.taki.api.shared.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitária que fornece métodos para a autenticação via JSON Web Token (JWT), com inclusão de extração, validação e geração de tokens.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public final class JwtAuthUtils {

    /**
     * Construtor privado para impedir a instanciação desta classe utilitária.
     */
    private JwtAuthUtils() {
    }

    /**
     * Extração do token Bearer.
     * @param authorizationHeader o cabeçalho de autorização (Authorization Header)
     * @return o token extraído
     */
    public static Optional<String> extrairBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        String valor = authorizationHeader.trim();
        if (!valor.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        String token = valor.substring(7).trim();
        return token.isBlank() ? Optional.empty() : Optional.of(token);
    }

    /**
     * Validação do token.
     * @param token o token
     * @param publicKeyStr a chave pública
     * @return o contexto de autenticação validado
     */
    public static ContextoAutenticacao validarToken(String token, String publicKeyStr) {
        if (token == null || token.isBlank()) {
            throw new TokenJwtInvalidoException("Token JWT em falta.");
        }
        if (publicKeyStr == null || publicKeyStr.isBlank()) {
            throw new TokenJwtInvalidoException("Chave pública JWT não configurada.");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new TokenJwtInvalidoException("Formato JWT inválido.");
        }

        String header = decodeBase64Url(partes[0], "header");
        String payload = decodeBase64Url(partes[1], "payload");

        String algoritmo = extrairClaimString(header, "alg");
        if (algoritmo == null || !"RS256".equalsIgnoreCase(algoritmo)) {
            throw new TokenJwtInvalidoException("Algoritmo JWT não suportado.");
        }

        validarAssinaturaRSA(partes[0], partes[1], partes[2], publicKeyStr);
        validarExpiracao(extrairClaimLong(payload, "exp"));

        String subject = extrairSubject(payload);
        String email = extrairClaimString(payload, "email");
        Set<String> roles = extrairConjuntoClaim(payload, "roles", "role", true);
        Set<String> scopes = extrairConjuntoClaim(payload, "scopes", "scope", false);

        return new ContextoAutenticacao(subject, email, roles, scopes, token);
    }

    /**
     * Geração de token RSA.
     * @param payloadJson o payload em formato JSON
     * @param privateKeyStr a chave privada
     * @return o token gerado
     */
    public static String gerarTokenRSA(String payloadJson, String privateKeyStr) {
        if (privateKeyStr == null || privateKeyStr.isBlank()) {
            throw new RuntimeException("Chave privada JWT não configurada.");
        }
        try {
            String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
            String headerB64 = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
            String dadosAssinados = headerB64 + "." + payloadB64;

            RSAPrivateKey privateKey = parsePrivateKey(privateKeyStr);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(dadosAssinados.getBytes(StandardCharsets.UTF_8));
            byte[] assinaturaBytes = sig.sign();
            String assinatura = base64Url(assinaturaBytes);

            return dadosAssinados + "." + assinatura;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar e assinar JWT via RSA.", e);
        }
    }

    /**
     * Valida a assinatura digital RSA de um token JWT.
     * 
     * @param header o cabeçalho codificado
     * @param payload o corpo codificado
     * @param assinatura a assinatura codificada
     * @param publicKeyStr a chave pública em formato Base64
     * @throws TokenJwtInvalidoException se a assinatura for inválida ou ocorrer erro na validação
     */
    private static void validarAssinaturaRSA(String header, String payload, String assinatura, String publicKeyStr) {
        String dadosAssinados = header + "." + payload;
        try {
            RSAPublicKey publicKey = parsePublicKey(publicKeyStr);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(dadosAssinados.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getUrlDecoder().decode(assinatura);
            
            if (!sig.verify(signatureBytes)) {
                throw new TokenJwtInvalidoException("Assinatura JWT inválida.");
            }
        } catch (Exception e) {
            throw new TokenJwtInvalidoException("Falha ao validar assinatura JWT.", e);
        }
    }

    /**
     * Converte uma string Base64 numa chave pública RSA.
     * 
     * @param b64 a chave pública em formato Base64 (PEM ou similar)
     * @return a chave pública RSA processada
     * @throws Exception se ocorrer erro no processamento da chave
     */
    private static RSAPublicKey parsePublicKey(String b64) throws Exception {
        byte[] pemBytes = Base64.getDecoder().decode(b64.replaceAll("\\s+", ""));
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        String publicKeyPEM = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    /**
     * Converte uma string Base64 numa chave privada RSA.
     * 
     * @param b64 a chave privada em formato Base64 (PEM ou similar)
     * @return a chave privada RSA processada
     * @throws Exception se ocorrer erro no processamento da chave
     */
    private static RSAPrivateKey parsePrivateKey(String b64) throws Exception {
        byte[] pemBytes = Base64.getDecoder().decode(b64.replaceAll("\\s+", ""));
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        String privateKeyPEM = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    /**
     * Descodifica uma secção de um JWT em formato Base64Url para texto.
     * 
     * @param valorBase64 o valor codificado
     * @param secao o nome da secção para mensagens de erro
     * @return o conteúdo descodificado em formato string
     * @throws TokenJwtInvalidoException se a descodificação falhar
     */
    private static String decodeBase64Url(String valorBase64, String secao) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(valorBase64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new TokenJwtInvalidoException("Não foi possível ler o " + secao + " do JWT.", e);
        }
    }

    /**
     * Codifica um array de bytes para uma string Base64Url sem preenchimento (padding).
     * 
     * @param valor os dados a codificar
     * @return a string codificada
     */
    private static String base64Url(byte[] valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor);
    }

    /**
     * Valida se a data de expiração de um token é posterior ao instante atual.
     * 
     * @param exp o instante de expiração (Unix epoch)
     * @throws TokenJwtInvalidoException se o token já tiver expirado
     */
    private static void validarExpiracao(Long exp) {
        if (exp == null) {
            return;
        }
        long agora = Instant.now().getEpochSecond();
        if (exp <= agora) {
            throw new TokenJwtInvalidoException("Token JWT expirado.");
        }
    }

    /**
     * Extrai o identificador do sujeito (claim 'sub') do corpo do token.
     * 
     * @param payload o corpo do token em formato JSON
     * @return o identificador extraído
     * @throws TokenJwtInvalidoException se a claim estiver em falta
     */
    private static String extrairSubject(String payload) {
        String sub = extrairClaimString(payload, "sub");
        if (sub == null || sub.isBlank()) {
            throw new TokenJwtInvalidoException("Claim 'sub' em falta no JWT.");
        }
        return sub.trim();
    }

    /**
     * Extrai um conjunto de valores de claims primárias ou secundárias.
     * 
     * @param payload o corpo do token em formato JSON
     * @param claimPrimaria o nome da claim principal
     * @param claimSecundaria o nome da claim alternativa
     * @param paraMaiusculas indica se os valores devem ser normalizados para maiúsculas
     * @return o conjunto de valores únicos extraídos
     */
    private static Set<String> extrairConjuntoClaim(
            String payload,
            String claimPrimaria,
            String claimSecundaria,
            boolean paraMaiusculas
    ) {
        Set<String> resultado = new LinkedHashSet<>();
        adicionarClaim(resultado, extrairClaimArray(payload, claimPrimaria), paraMaiusculas);
        adicionarClaim(resultado, extrairClaimArray(payload, claimSecundaria), paraMaiusculas);
        adicionarClaim(resultado, extrairClaimString(payload, claimPrimaria), paraMaiusculas);
        adicionarClaim(resultado, extrairClaimString(payload, claimSecundaria), paraMaiusculas);
        return resultado;
    }

    /**
     * Adiciona um conjunto de valores a um destino com normalização opcional.
     * 
     * @param destino o conjunto de destino
     * @param valores os valores a adicionar
     * @param paraMaiusculas indica se deve ocorrer normalização para maiúsculas
     */
    private static void adicionarClaim(Set<String> destino, Set<String> valores, boolean paraMaiusculas) {
        if (valores == null || valores.isEmpty()) {
            return;
        }
        for (String valor : valores) {
            adicionarValor(destino, valor, paraMaiusculas);
        }
    }

    /**
     * Adiciona valores extraídos de uma string (possivelmente separada por espaços ou vírgulas).
     * 
     * @param destino o conjunto de destino
     * @param valor a string com os valores
     * @param paraMaiusculas indica se deve ocorrer normalização para maiúsculas
     */
    private static void adicionarClaim(Set<String> destino, String valor, boolean paraMaiusculas) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        String[] itens = valor.split("[,\\s]+");
        for (String item : itens) {
            adicionarValor(destino, item, paraMaiusculas);
        }
    }

    /**
     * Adiciona um valor individual ao conjunto com limpeza de espaços e normalização.
     * 
     * @param destino o conjunto de destino
     * @param valor o texto a adicionar
     * @param paraMaiusculas indica se deve ocorrer normalização para maiúsculas
     */
    private static void adicionarValor(Set<String> destino, String valor, boolean paraMaiusculas) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        String normalizado = valor.trim();
        if (paraMaiusculas) {
            normalizado = normalizado.toUpperCase();
        }
        destino.add(normalizado);
    }

    /**
     * Extrai o valor de uma claim de texto via expressão regular simplificada.
     * 
     * @param json o texto JSON
     * @param claim o nome da claim
     * @return o valor extraído ou null se não for encontrado
     */
    private static String extrairClaimString(String json, String claim) {
        String regex = "\""+ Pattern.quote(claim) + "\"\\s*:\\s*\"([^\"]*)\"";
        Matcher matcher = Pattern.compile(regex).matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    /**
     * Extrai o valor de uma claim numérica (long) via expressão regular.
     * 
     * @param json o texto JSON
     * @param claim o nome da claim
     * @return o valor extraído ou null se não for encontrado
     * @throws TokenJwtInvalidoException se o formato numérico for inválido
     */
    private static Long extrairClaimLong(String json, String claim) {
        String regex = "\""+ Pattern.quote(claim) + "\"\\s*:\\s*(-?\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(json);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new TokenJwtInvalidoException("Claim '" + claim + "' inválido.", e);
        }
    }

    /**
     * Extrai um conjunto de strings de uma claim que representa um array JSON.
     * 
     * @param json o texto JSON
     * @param claim o nome da claim
     * @return o conjunto de valores extraídos
     */
    private static Set<String> extrairClaimArray(String json, String claim) {
        String regex = "\""+ Pattern.quote(claim) + "\"\\s*:\\s*\\[(.*?)\\]";
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            return Set.of();
        }

        String conteudo = matcher.group(1);
        Matcher elementos = Pattern.compile("\"([^\"]+)\"").matcher(conteudo);
        Set<String> valores = new LinkedHashSet<>();
        while (elementos.find()) {
            valores.add(elementos.group(1));
        }
        return valores;
    }
}
