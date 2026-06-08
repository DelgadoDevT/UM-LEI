package pt.uminho.taki.api.shared.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthUtilsTest {

    private static final String CHAVE_PRIVADA = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQzNyZnNHRXo3WHQ0aEoKUGxseUNoVTJwR3pVemhjemZqdG9HUW51TFRlRG92VkJVUVg3Z3ZJdDIwUDJaNElhd2pBWFpHU3N1RTQrd3J1WQpqdVV3eEwwTCt1UmxPUXB2Q25jeERSTzR5eGl6OFd2eXUzUzFocmhnRlFWbXVOWEVEUks3RDdFTzQ0cUNhNlRQCnRNN1lIdUV4SzY4UUUxdFJ2bHFqRk5BSWt4NGJjY2t1em1CaUdBeVQ2bFNTR1dSMmU4K2UvMWVITU1PQ1hkaTUKMjdETzZZTFQxNmFvNWlDNG1pV09US2cvRmRmSUdCS0V3K002anM2RkkxTWVoeEpndUJ5SUo2Uy81L2xPNUZ1RApOS1YzbFNWR0RiOXJ4ZTdxY3lnWHRLazVyTCtHR05OVlZjTmExS2twYTZaOGkwcDA4c0tleG5Yd0xUOVVHM2ExCmZhQlJwZTZoQWdNQkFBRUNnZ0VBQTNGeTgrTSt1d2JwTlFiNld5SHozT1J5bXJHVnk2c043MEFPZVZadGtUSisKTnlrbFVNZnRXMUFZazBTQm9oMWpLc3BPby9QSzh3RFhyU2M0UGJCblQ2OWx0MExUaEIwdmdESW02RkZLZjdEYgp2WVg2N2tVOXppc2NSblkrZFZhNHM5eDNjUXZWcjZTSnpNL1Zlb2hjY0ZEckR0aFpMVEIrdk9ZaXpoeWEyWEVyCmt4YnFkYW9BUkRyWE10QzkyQnpCbnJlOEZEQmJ4bHFma3pTenA1MnozOW9VWTcrNnJ6SkI3Z3JENjg5NGdHVUQKRVVQYjdjYTRVR1k5Q0lwZUluaXJzVS9aQzJ2UnVWaWFZdW9KU3NVZjBabTRQQWJpSXRKWEErZ0ZsbExLRUcwZwprbE9pd2NXS3F1U1ZkR2oyTWtTUjVYaHkrcElYa3JqVU1WY3hBbHJLZ1FLQmdRRHRHUGkrcFdvejdrdk1SbEZ1CmUrQUNGK0NSb1hpUm9YWjNFT2NGUmh1SGNWMlBaWHRtd3FDMnl6eUpqVVZ4RFNxWHRvZ0MvWXhwQjBZbXFZT0UKRkQyUDFkZzVMVTFNL2tBTEhodWRGUjEwV3NRNGo3RHVhQklvVEFxSXF4eG5OaHdlQ3NUQ0U1UlY0ZGFXMVpVKwoxaXRSQ00ydC9RQVdxV2xDZjNJaHV0d09Id0tCZ1FER1VzZGJSaS9Nc2Fma2kxdlQ4K2Zva0ZVejV1V1RNcUlECnZqNVVkS21GbTByMjlHeksydXlXSVBZTmhnM1Y4Vjc4cjh4b0EreEF5R0tqSk9jenh3QncwRHRKZW1XaDloQm0KeUgwZkF2cU81dFhVRmZYSWo4d2F3dUN1OW42VVBGNVpOZFNERFB6S2ZjNzVESGt5NGtMenNvd3ZRZGtSNjJqYwoydlQ0aW5qclB3S0JnSGc4aUxDTnpFem5sTExVR2ltd1VrcGRkM29DNjlUV2ovQVR0MFJhYUUySWRRenFSdWsyCjNDKzI0MDFTUWh2eHRGN1pweUgxR01hT2RNbllmeVIwNno5TkQrajNDMU4zUUYwaStKZnhoRTFrVmt2a3VySTgKUHJFRzF2UzE2YUtCczBLYzdXa0thdGdldmljaHl4emhtWGZ2d2ZadzdpR1A2aHhaNnpRbHdjQzdBb0dCQUk2dwpJQjdiY0tpbGFWWlZyY3ZoUUlsaXp1cDR4ZFFTS2Q3bmtaRFNIdVd4eFUzSXJqMkZKWk1lUSttVytYMWtIQ05SCm1JOTl3dzBaSDE2aHZuYlNSczdrQXNQcS9CUjdKWEhON2JmU1BOTno0WVNtQ0NBMDFYU3JvVnhBblBHcVBYZVIKODBRV3BBcE1XZzExbS8yd0xOdUpLck1WZlVnWFIyc2ZYZ0UwSDZ1aEFvR0JBTWJIdDJCYVRXWm16UlJUM01JKwpubVRIWDFNc1RvNjN4K29CSkxYOVBrQS9meVNRYUxGYjlwVHRjNnhUbWlZNGN0dVVLMDJ5OGV2eXV5ajE0L25lCmN2cXNXeTBtQTNVOFVJU0czRUdHSmRGZTF3Y292ZVNHYW94ZHJJKzYzYkhSRVJFandJOEJlbFpaM2ZRNk1kV0EKeGpyQzhXc0dpWktNWmltZnU4VEtGSTJJCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    private static final String CHAVE_PUBLICA = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF0NjM3QmhNKzE3ZUlTVDVaY2dvVgpOcVJzMU00WE0zNDdhQmtKN2kwM2c2TDFRVkVGKzRMeUxkdEQ5bWVDR3NJd0YyUmtyTGhPUHNLN21JN2xNTVM5CkMvcmtaVGtLYndwM01RMFR1TXNZcy9GcjhydDB0WWE0WUJVRlpyalZ4QTBTdXcreER1T0tnbXVrejdUTzJCN2gKTVN1dkVCTmJVYjVhb3hUUUNKTWVHM0hKTHM1Z1loZ01rK3BVa2hsa2RudlBudjlYaHpERGdsM1l1ZHV3enVtQwowOWVtcU9ZZ3VKb2xqa3lvUHhYWHlCZ1NoTVBqT283T2hTTlRIb2NTWUxnY2lDZWt2K2Y1VHVSYmd6U2xkNVVsClJnMi9hOFh1Nm5Nb0Y3U3BPYXkvaGhqVFZWWERXdFNwS1d1bWZJdEtkUExDbnNaMThDMC9WQnQydFgyZ1VhWHUKb1FJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    private static final String OUTRA_CHAVE_PUBLICA = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFtUDMvN293dUZYTkcxL3d3RGRBMAprL2pQc1VMM3Z1RW40dkR2RjAvU3NYVW4wTXZ2R2VOMU8yNW1HQU1rZlBYTTR2K1cweGlhS3ZqRzBCNWNscXAKazZZTzVlNDBvTk5xNGYvYnBnczNmdmlUZEVWMklKZnkweEl1eTh2OVMxOWlqSGFIVndkZ3o3WjhPazJ1K2p0CjZ5YnpvTTh2TFBZZTZQOXg3T3FwWUhjT043OUZOV1RPQ0ZwdXhONXFhMjV5YXZHUnAvYWxYcE0wbTlkb21OQwoxd01XUzhvR1RXTExOaTRsMmUzTWJwaWV1QUQxb2w2LzBSREw2Mnh3OUIweFRtK0t5dWQxaDFzWjdSdjZtY1EKbklCR3BCN3FjNWU4anl2N0p1bWVMTnFidnhyRmN4dEtycUNzMktlMWNTR0ZtVWt1eFRiYnZOT3ZWMkVtYmhPCkJ3SURBUUFCCi0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQo=";

    @Test
    @DisplayName("Deve extrair token Bearer do header Authorization")
    void deveExtrairBearerToken() {
        // Arrange
        String header = "Bearer abc.def.ghi";

        // Act
        Optional<String> token = JwtAuthUtils.extrairBearerToken(header);

        // Assert
        assertTrue(token.isPresent());
        assertEquals("abc.def.ghi", token.get());
    }

    @Test
    @DisplayName("Deve validar JWT com roles e scopes usando RS256")
    void deveValidarJwtComRolesEScopes() {
        // Arrange
        String payload = """
                {
                  "sub":"funcionario-1",
                  "roles":["ADMIN","GESTOR"],
                  "scope":"inventario:read inventario:write",
                  "exp":%d
                }
                """.formatted(Instant.now().plusSeconds(600).getEpochSecond());
        String token = JwtAuthUtils.gerarTokenRSA(payload, CHAVE_PRIVADA);

        // Act
        ContextoAutenticacao contexto = JwtAuthUtils.validarToken(token, CHAVE_PUBLICA);

        // Assert
        assertEquals("funcionario-1", contexto.getSubject());
        assertTrue(contexto.getRoles().contains("ADMIN"));
        assertTrue(contexto.getScopes().contains("inventario:read"));
        assertTrue(contexto.getScopes().contains("inventario:write"));
    }

    @Test
    @DisplayName("Deve rejeitar JWT com assinatura invalida (outra chave publica)")
    void deveRejeitarJwtAssinaturaInvalida() {
        // Arrange
        String payload = """
                {
                  "sub":"funcionario-1",
                  "exp":%d
                }
                """.formatted(Instant.now().plusSeconds(600).getEpochSecond());
        String token = JwtAuthUtils.gerarTokenRSA(payload, CHAVE_PRIVADA);

        // Act & Assert
        assertThrows(TokenJwtInvalidoException.class, () -> JwtAuthUtils.validarToken(token, OUTRA_CHAVE_PUBLICA));
    }

    @Test
    @DisplayName("Deve rejeitar JWT expirado")
    void deveRejeitarJwtExpirado() {
        // Arrange
        String payload = """
                {
                  "sub":"funcionario-1",
                  "exp":%d
                }
                """.formatted(Instant.now().minusSeconds(5).getEpochSecond());
        String token = JwtAuthUtils.gerarTokenRSA(payload, CHAVE_PRIVADA);

        // Act & Assert
        assertThrows(TokenJwtInvalidoException.class, () -> JwtAuthUtils.validarToken(token, CHAVE_PUBLICA));
    }
}