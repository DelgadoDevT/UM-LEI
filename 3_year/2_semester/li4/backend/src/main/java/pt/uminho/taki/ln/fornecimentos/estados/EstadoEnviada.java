package pt.uminho.taki.ln.fornecimentos.estados;

import pt.uminho.taki.ln.fornecimentos.Encomenda;

/**
 * Representa o estado enviado de uma encomenda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstadoEnviada implements IEstadoEncomenda {
    /**
     * Avança a encomenda para o estado seguinte (Concluída).
     *
     * @param encomenda a encomenda a avançar
     */
    @Override
    public void avancar(Encomenda encomenda) {
        encomenda.setDataEntrega(java.time.LocalDateTime.now());
        encomenda.setEstadoAtual(new EstadoConcluida());
    }

    /**
     * Cancela a encomenda.
     *
     * @param encomenda a encomenda a cancelar
     * @throws IllegalStateException sempre, uma vez que uma encomenda enviada não pode ser cancelada eletronicamente
     */
    @Override
    public void cancelar(Encomenda encomenda) {
        throw new IllegalStateException("A encomenda j\u00e1 foi enviada ao fornecedor e n\u00e3o pode ser cancelada eletronicamente.");
    }

    /**
     * Obtém a designação do estado.
     *
     * @return a designação "Enviada"
     */
    @Override
    public String getDesignacao() {
        return "Enviada";
    }

    /**
     * Verifica se as linhas da encomenda podem ser modificadas neste estado.
     *
     * @return false, uma vez que as linhas da encomenda não podem ser modificadas quando enviada
     */
    @Override
    public boolean podeModificarLinhas() {
        return false;
    }
}
