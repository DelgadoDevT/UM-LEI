package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.ln.inventario.exceptions.DataInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.MotivoObrigatorioException;
import pt.uminho.taki.ln.inventario.exceptions.QuantidadeInvalidaException;
import java.util.List;

/**
 * Interface para o servico de auditoria e registo de movimentos de inventario.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IMovimentoInventarioService {
    /**
     * Regista um novo movimento fisico de stock com validacao de integridade.
     * @param movimento o movimento a registar
     * @throws QuantidadeInvalidaException se a quantidade for nula/negativa
     * @throws DataInvalidaException se a data for no futuro
     * @throws MotivoObrigatorioException se o motivo nao for fornecido para quebras
     */
    void registarMovimento(MovimentoInventario movimento) throws QuantidadeInvalidaException, DataInvalidaException, MotivoObrigatorioException;

    /**
     * Consulta o historico de auditoria para um produto especifico na loja.
     * @param idInventario o identificador do stock
     * @return lista de movimentos históricos
     */
    List<MovimentoInventario> consultarHistorico(String idInventario);
}
