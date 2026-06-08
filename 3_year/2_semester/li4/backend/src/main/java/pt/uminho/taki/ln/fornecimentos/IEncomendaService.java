package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import java.util.List;

/**
 * Interface que define o contrato para a gestão de encomendas a fornecedores.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IEncomendaService {
    /**
     * Cria a guia de encomenda.
     */
    void criarGuia(String idEncomenda, String idFornecedor, String idLoja, List<LinhaEncomenda> linhas) throws FornecedorInativoException, CamposObrigatoriosEmFaltaException;
    
    /**
     * Define o serviço de inventário para atualização de stock na receção.
     */
    void setInventarioService(pt.uminho.taki.ln.inventario.IInventarioService inventarioService);

    /**
     * Avanca a maquina de estados de uma dada encomenda.
     */
    void processarTransicaoEstado(String idEncomenda) throws IllegalStateException;
    
    /**
     * Calcula dinamicamente o total da fatura.
     */
    double calcularTotalGuia(String idEncomenda);

    /**
     * Lista encomendas registadas.
     * @return encomendas
     */
    List<Encomenda> listarEncomendas();
}
