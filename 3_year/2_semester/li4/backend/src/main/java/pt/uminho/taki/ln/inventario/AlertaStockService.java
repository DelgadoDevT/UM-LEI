package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.dao.StatisticsDAO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacao do servico de monitorizacao de limites de inventario.
 * Identifica produtos com stock abaixo do limite mínimo definido.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class AlertaStockService implements IAlertaStockService {
    private final StatisticsDAO statisticsDAO;

    /**
     * Construtor para injeção de dependências.
     * @param statisticsDAO o DAO para consulta da vista de alertas
     */
    public AlertaStockService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertaStock> verificarAlertasLoja(int idLoja) {
        // Consulta a vista da base de dados através do DAO de estatísticas
        List<java.util.Map<String, Object>> alertasRaw = this.statisticsDAO.getAlertasStockCritico(idLoja);

        // Processa e enriquece os alertas
        return alertasRaw.stream()
            .map(row -> {
                AlertaStock alerta = new AlertaStock();
                alerta.setIdLoja((Integer) row.get("idLoja"));
                alerta.setCodigoBarras((String) row.get("codigoBarras"));
                alerta.setNomeProduto((String) row.get("produto"));
                alerta.setQuantidadeAtual(((Number) row.get("stockAtual")).doubleValue());
                alerta.setLimiteMinimo(((Number) row.get("limiteMinimo")).doubleValue());
                alerta.setDataAlerta(LocalDateTime.now());
                alerta.setMensagem(String.format("Aviso: O produto [%s] atingiu o limite critico. Stock atual: [%.2f]", 
                                                   alerta.getNomeProduto(), 
                                                   alerta.getQuantidadeAtual()));
                return alerta;
            })
            .collect(Collectors.toList());
    }
}
