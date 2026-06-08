package pt.uminho.taki.ln.vendas;

import pt.uminho.taki.ln.lojas.Produto;

/**
 * Interface que define o contrato para o processamento de vendas no POS.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IVendaService {
    /**
     * Inicia uma nova venda, instanciando um carrinho vazio.
     * @param idLoja Identificador da loja atual
     * @param idFuncionario Identificador do funcionário que opera o POS
     * @return A nova instância de Venda
     */
    Venda iniciarVenda(int idLoja, String idFuncionario);

    /**
     * Adiciona um produto ao carrinho em curso sob o formato de uma LinhaVenda.
     * Aqui deve ser calculada ativamente qualquer campanha pela invocação do IPromocaoService.
     * @param venda O carrinho/venda pendente
     * @param produto O artigo a adquirir
     * @param quantidade A quantidade de artigos
     */
    void adicionarLinha(Venda venda, Produto produto, int quantidade);

    /**
     * Processa a finalizacao de uma venda e deduz o stock.
     * @param venda a venda a registar
     * @param metodoPagamento o metodo de pagamento selecionado
     * @throws MetodoPagamentoIndisponivelException se o metodo de pagamento falhar
     */
    void processarVenda(Venda venda, String metodoPagamento) throws MetodoPagamentoIndisponivelException;

    /**
     * Processa a finalizacao com valor entregue pelo cliente (necessário para numerário).
     * @param venda a venda a registar
     * @param metodoPagamento o metodo de pagamento selecionado
     * @param valorEntregue montante entregue pelo cliente
     * @throws MetodoPagamentoIndisponivelException se o metodo for invalido ou o pagamento for insuficiente
     */
    void processarVenda(Venda venda, String metodoPagamento, double valorEntregue) throws MetodoPagamentoIndisponivelException;

    /**
     * Retorna a lista de todas as vendas processadas.
     * @return lista de vendas
     */
    java.util.List<Venda> listarVendas();
}
