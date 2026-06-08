package pt.uminho.taki.api.local;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalApiDtosTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    @Test
    void pedidoEditarProdutoAceitaCamposDesconhecidos() throws Exception {
        String json = """
                {
                  "idProduto":"48103cfa-13e8-4cd7-8ccb-371f3a8315d0",
                  "idCategoria":"4a09da34-6f7f-46a8-bcd4-02cff83600e6",
                  "codigoBarras":"1743508256829",
                  "nome":"Café S/ Gás 766cl",
                  "descricao":"Café S/ Gás 766cl",
                  "precoCusto":106.61,
                  "precoVenda":106.61,
                  "taxaIva":"NORMAL_23",
                  "unidadeMedida":"unidade",
                  "estado":"Ativo"
                }
                """;

        LocalApiDtos.PedidoEditarProduto pedido = mapper.readValue(json, LocalApiDtos.PedidoEditarProduto.class);

        assertEquals("1743508256829", pedido.getCodigoBarras());
        assertEquals("Café S/ Gás 766cl", pedido.getNome());
        assertEquals(106.61, pedido.getPrecoVenda());
    }

    @Test
    void pedidoAdicionarProdutoAceitaCamposDesconhecidos() throws Exception {
        String json = """
                {
                  "idProduto":"48103cfa-13e8-4cd7-8ccb-371f3a8315d0",
                  "idCategoria":"4a09da34-6f7f-46a8-bcd4-02cff83600e6",
                  "codigoBarras":"1743508256829",
                  "nome":"Café S/ Gás 766cl",
                  "descricao":"Café S/ Gás 766cl",
                  "precoCusto":106.61,
                  "precoVenda":106.61,
                  "taxaIva":"NORMAL_23",
                  "unidadeMedida":"unidade",
                  "estado":"Ativo"
                }
                """;

        LocalApiDtos.PedidoAdicionarProduto pedido = mapper.readValue(json, LocalApiDtos.PedidoAdicionarProduto.class);

        assertEquals("48103cfa-13e8-4cd7-8ccb-371f3a8315d0", pedido.getIdProduto());
        assertEquals("1743508256829", pedido.getCodigoBarras());
        assertEquals(106.61, pedido.getPrecoCusto());
    }
}
