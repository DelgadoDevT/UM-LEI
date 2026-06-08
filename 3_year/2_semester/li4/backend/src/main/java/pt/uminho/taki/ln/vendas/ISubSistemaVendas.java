package pt.uminho.taki.ln.vendas;

/**
 * Interface do subsistema para a gestão de vendas.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ISubSistemaVendas {
    /**
     * Obtém o serviço de vendas.
     * 
     * @return o serviço de vendas
     */
    IVendaService getVendaService();

    /**
     * Obtém o serviço de devoluções.
     * 
     * @return o serviço de devoluções
     */
    IDevolucaoService getDevolucaoService();

    /**
     * Obtém o serviço de promoções.
     * 
     * @return o serviço de promoções
     */
    IPromocaoService getPromocaoService();

    /**
     * Processa uma venda.
     * 
     * @param venda a venda a processar
     * @param metodoPagamento o método de pagamento
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento estiver indisponível
     */
    void processarVenda(Venda venda, String metodoPagamento) throws MetodoPagamentoIndisponivelException;

    /**
     * Processa uma venda com um dado valor entregue.
     * 
     * @param venda a venda a processar
     * @param metodoPagamento o método de pagamento
     * @param valorEntregue o montante entregue pelo cliente
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento estiver indisponível
     */
    void processarVenda(Venda venda, String metodoPagamento, double valorEntregue) throws MetodoPagamentoIndisponivelException;

    /**
     * Processa uma devolução através do identificador da venda.
     * 
     * @param idVenda o identificador da venda a devolver
     * @throws pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException se o prazo de devolução tiver sido excedido
     */
    void processarDevolucao(String idVenda) throws pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;

    /**
     * Processa uma devolução para linhas de venda específicas.
     * 
     * @param vendaOriginal a venda original
     * @param linhasADevolver a lista de linhas a devolver
     * @throws pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException se o prazo de devolução tiver sido excedido
     */
    void processarDevolucao(Venda vendaOriginal, java.util.List<LinhaVenda> linhasADevolver) throws pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;
    
    /**
     * Lista todas as vendas.
     * 
     * @return uma lista de vendas
     */
    java.util.List<Venda> listarVendas();

    /**
     * Lista todas as promoções ativas.
     * 
     * @return uma lista de promoções ativas
     */
    java.util.List<Promocao> listarPromocoesAtivas();

    /**
     * Adiciona uma nova promoção.
     * 
     * @param promocao a promoção a adicionar
     */
    void adicionarPromocao(Promocao promocao);

    /**
     * Cancela uma promoção.
     * 
     * @param idPromocao o identificador da promoção a cancelar
     * @param motivo o motivo do cancelamento
     */
    void cancelarPromocao(String idPromocao, String motivo);

    /**
     * Inicia uma nova venda.
     * 
     * @param idLoja o identificador da loja
     * @param idFuncionario o identificador do funcionário
     * @return a venda inicializada
     */
    Venda iniciarVenda(int idLoja, String idFuncionario);

    /**
     * Adiciona uma linha a uma venda.
     * 
     * @param venda a venda
     * @param produto o produto
     * @param quantidade a quantidade
     */
    void adicionarLinhaVenda(Venda venda, pt.uminho.taki.ln.lojas.Produto produto, int quantidade);
}
