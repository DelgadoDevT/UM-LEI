package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Representa o estado cancelado de uma encomenda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstadoCancelada implements IEstadoEncomenda {
    /**
     * Avança a encomenda para o estado seguinte.
     *
     * @param encomenda a encomenda a avançar
     * @throws IllegalStateException sempre, uma vez que uma encomenda cancelada não pode ser avançada
     */
    @Override
    public void avancar(Encomenda encomenda) {
        throw new IllegalStateException("Não é possível avançar uma encomenda cancelada.");
    }

    /**
     * Cancela a encomenda.
     *
     * @param encomenda a encomenda a cancelar
     * @throws IllegalStateException sempre, uma vez que a encomenda já se encontra cancelada
     */
    @Override
    public void cancelar(Encomenda encomenda) {
        throw new IllegalStateException("A encomenda já se encontra cancelada.");
    }

    /**
     * Obtém a designação do estado.
     *
     * @return a designação "Cancelada"
     */
    @Override
    public String getDesignacao() {
        return "Cancelada";
    }

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     *
     * @return false, uma vez que as linhas da encomenda não podem ser modificadas quando cancelada
     */
    @Override
    public boolean podeModificarLinhas() {
        return false;
    }
}
