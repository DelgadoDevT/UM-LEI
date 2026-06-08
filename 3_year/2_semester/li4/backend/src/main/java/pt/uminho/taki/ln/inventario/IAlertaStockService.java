package pt.uminho.taki.ln.inventario;

import java.util.List;

/**
 * Interface para o servico de deteccao de rutura de stock.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IAlertaStockService {
    /**
     * Verifica e devolve os alertas de stock para uma loja especifica.
     * @param idLoja o identificador da loja
     * @return lista de alertas processados
     */
    List<AlertaStock> verificarAlertasLoja(int idLoja);
}
