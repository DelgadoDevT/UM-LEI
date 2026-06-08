package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Representa o estado pendente de uma encomenda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstadoPendente implements IEstadoEncomenda {
    /**
     * Avança a encomenda para o estado seguinte (Enviada).
     *
     * @param encomenda a encomenda a avançar
     */
    @Override
    public void avancar(Encomenda encomenda) {
        encomenda.setEstadoAtual(new EstadoEnviada());
    }

    /**
     * Cancela a encomenda.
     *
     * @param encomenda a encomenda a cancelar
     */
    @Override
    public void cancelar(Encomenda encomenda) {
        encomenda.setEstadoAtual(new EstadoCancelada());
    }

    /**
     * Obtém a designação do estado.
     *
     * @return a designação "Pendente"
     */
    @Override
    public String getDesignacao() {
        return "Pendente";
    }

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     *
     * @return false, uma vez que as linhas da encomenda não podem ser modificadas quando pendente
     */
    @Override
    public boolean podeModificarLinhas() {
        return false;
    }
}
