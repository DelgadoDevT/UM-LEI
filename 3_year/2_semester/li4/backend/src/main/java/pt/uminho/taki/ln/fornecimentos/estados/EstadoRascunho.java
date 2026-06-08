package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Representa o estado rascunho de uma encomenda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstadoRascunho implements IEstadoEncomenda {
    /**
     * Avança a encomenda para o estado seguinte (Pendente).
     *
     * @param encomenda a encomenda a avançar
     */
    @Override
    public void avancar(Encomenda encomenda) {
        encomenda.setEstadoAtual(new EstadoPendente());
    }

    /**
     * Cancela a encomenda.
     *
     * @param encomenda a encomenda a cancelar
     * @throws IllegalStateException sempre, uma vez que um rascunho não pode ser cancelado, apenas eliminado
     */
    @Override
    public void cancelar(Encomenda encomenda) {
        throw new IllegalStateException("N\u00e3o \u00e9 poss\u00edvel cancelar uma encomenda que ainda \u00e9 um rascunho. Simplesmente elimine-a.");
    }

    /**
     * Obtém a designação do estado.
     *
     * @return a designação "Rascunho"
     */
    @Override
    public String getDesignacao() {
        return "Rascunho";
    }

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     *
     * @return true, uma vez que as linhas da encomenda podem ser modificadas no estado de rascunho
     */
    @Override
    public boolean podeModificarLinhas() {
        return true;
    }
}
