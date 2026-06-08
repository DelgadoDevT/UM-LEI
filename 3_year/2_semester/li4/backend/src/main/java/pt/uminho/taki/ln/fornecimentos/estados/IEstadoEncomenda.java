package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Interface para o estado de uma encomenda.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface IEstadoEncomenda {
    /**
     * Avança o estado da encomenda.
     * 
     * @param encomenda a encomenda a avançar
     */
    void avancar(Encomenda encomenda);

    /**
     * Cancela a encomenda.
     * 
     * @param encomenda a encomenda a cancelar
     */
    void cancelar(Encomenda encomenda);

    /**
     * Obtém a designação do estado.
     * 
     * @return a designação
     */
    String getDesignacao();

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     * 
     * @return true se as linhas puderem ser modificadas, false caso contrário
     */
    boolean podeModificarLinhas();
}
