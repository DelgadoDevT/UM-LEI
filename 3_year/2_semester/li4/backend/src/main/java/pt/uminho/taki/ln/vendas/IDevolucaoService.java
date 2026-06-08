package pt.uminho.taki.ln.vendas;

import java.util.List;

/**
 * Interface para as operações do serviço de Devolucao.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IDevolucaoService {
    /**
     * Processa uma devolução.
     *
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    Devolucao processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException;
    /**
     * Processa uma devolução com um método de reembolso alternativo.
     *
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @param metodoReembolsoAlternativo o método de reembolso alternativo
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    Devolucao processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver, String metodoReembolsoAlternativo) throws PrazoDevolucaoExcedidoException;
    /**
     * Processa uma devolução através do número da fatura.
     *
     * @param numeroFatura o número da fatura
     * @param linhasADevolver as linhas a devolver
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    Devolucao processarDevolucaoPorNumeroFatura(String numeroFatura, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException;
    /**
     * Adiciona um observador.
     *
     * @param observer o observador a adicionar
     */
    void adicionarObserver(IDevolucaoObserver observer);
    /**
     * Remove um observador.
     *
     * @param observer o observador a remover
     */
    void removerObserver(IDevolucaoObserver observer);

    /**
     * Lista todas as devoluções registadas.
     *
     * @return a lista de devoluções
     */
    List<Devolucao> listarDevolucoes();
}
