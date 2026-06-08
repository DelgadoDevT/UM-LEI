package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.ln.inventario.exceptions.DataInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.MotivoObrigatorioException;
import pt.uminho.taki.ln.inventario.exceptions.QuantidadeInvalidaException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação do serviço de auditoria e tipificação de movimentos físicos.
 * Consolidado para validação de quantidade, data e justificação de quebras.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class MovimentoInventarioService implements IMovimentoInventarioService {
    private final InventarioDAO inventarioDAO;

    /**
     * Construtor para injeção de dependências.
     * @param inventarioDAO o DAO para historico de movimentos
     */
    public MovimentoInventarioService(InventarioDAO inventarioDAO) {
        this.inventarioDAO = inventarioDAO;
    }

    /**
     * Regista um novo movimento de inventário aplicando as regras de validação.
     *
     * @param movimento o movimento a registar
     * @throws QuantidadeInvalidaException se a quantidade for inferior ou igual a zero
     * @throws DataInvalidaException se a data do registo for futura
     * @throws MotivoObrigatorioException se for um movimento de quebra sem justificação
     */
    @Override
    public void registarMovimento(MovimentoInventario movimento) throws QuantidadeInvalidaException, DataInvalidaException, MotivoObrigatorioException {
        
        // 1. Valida quantidade estritamente positiva
        if (movimento.getQuantidade() <= 0) {
            throw new QuantidadeInvalidaException("A quantidade do movimento deve ser estritamente superior a zero.");
        }

        // 2. Valida se a data do registo nao e futura
        if (movimento.getDataRegisto() != null && movimento.getDataRegisto().isAfter(LocalDateTime.now())) {
            throw new DataInvalidaException("Nao e possivel registar movimentos com data futura.");
        }

        // 3. Valida obrigatoriedade de motivo para Quebra
        if (movimento.getTipo() == TipoMovimento.QUEBRA && 
            (movimento.getMotivo() == null || movimento.getMotivo().isBlank())) {
            throw new MotivoObrigatorioException("Os movimentos de Quebra manual exigem uma justificacao.");
        }

        // Os movimentos sao gerados por triggers na base de dados (conforme InventarioDAO)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MovimentoInventario> consultarHistorico(String idInventario) {
        // No InventarioDAO, a chave de consulta de movimentos e o idProduto (que corresponde ao idInventario aqui)
        return this.inventarioDAO.getMovimentos(idInventario);
    }
}
