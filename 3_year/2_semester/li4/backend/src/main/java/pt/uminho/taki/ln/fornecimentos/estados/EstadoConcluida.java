package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Representa o estado concluído de uma encomenda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstadoConcluida implements IEstadoEncomenda {
    /**
     * Avança a encomenda para o estado seguinte.
     *
     * @param encomenda a encomenda a avançar
     * @throws IllegalStateException sempre, uma vez que uma encomenda concluída não pode ser avançada
     */
    @Override
    public void avancar(Encomenda encomenda) {
        throw new IllegalStateException("A encomenda j\u00e1 se encontra conclu\u00edda.");
    }

    /**
     * Cancela a encomenda.
     *
     * @param encomenda a encomenda a cancelar
     * @throws IllegalStateException sempre, uma vez que uma encomenda concluída não pode ser cancelada
     */
    @Override
    public void cancelar(Encomenda encomenda) {
        throw new IllegalStateException("A encomenda j\u00e1 foi encerrada e contabilizada.");
    }

    /**
     * Obtém a designação do estado.
     *
     * @return a designação "Concluída"
     */
    @Override
    public String getDesignacao() {
        return "Concluída";
    }

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     *
     * @return false, uma vez que as linhas da encomenda não podem ser modificadas quando concluída
     */
    @Override
    public boolean podeModificarLinhas() {
        return false;
    }
}
