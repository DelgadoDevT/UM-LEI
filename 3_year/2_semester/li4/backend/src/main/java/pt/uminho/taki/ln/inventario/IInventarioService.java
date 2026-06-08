package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.ln.inventario.exceptions.ArtigoNaoEncontradoException;
import pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException;

/**
 * Interface para o servico de gestao de inventario.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IInventarioService {
    /**
     * Regista um movimento manual de stock.
     * @param movimento o movimento a realizar
     * @throws StockInsuficienteException se a quantidade para saida ou quebra for superior ao stock atual
     * @throws ArtigoNaoEncontradoException se o inventario nao for encontrado
     */
    void registarMovimentoManual(MovimentoInventario movimento) throws StockInsuficienteException, ArtigoNaoEncontradoException;

    /**
     * Atualiza o limite minimo de seguranca de stock com base em previsoes de venda e prazos de entrega.
     * @param idInventario o identificador do stock
     * @param mediaVendasDiarias a media de vendas por dia
     * @param tempoEntregaDias o tempo que o fornecedor demora a entregar em dias
     * @param fatorSeguranca a margem de erro (ex: 0.2 para 20%)
     * @throws ArtigoNaoEncontradoException se o inventario nao for encontrado
     */
    void definirLimiteSeguranca(String idInventario, double novoLimite) throws ArtigoNaoEncontradoException;

    /**
     * Processa a entrada de stock proveniente de uma encomenda concluída.
     * @param idLoja ID da loja onde o stock entra
     * @param idProduto ID do produto
     * @param quantidade Quantidade a adicionar
     */
    void processarEntradaEncomenda(int idLoja, String idProduto, double quantidade);

    /**
     * Exporta o estado de stock em formato CSV.
     * @return conteúdo CSV com cabeçalho e linhas por artigo
     */
    String exportarStockCsv();

    /**
     * Exporta o estado de stock em formato JSON.
     * @return conteúdo JSON com a lista de artigos em stock
     */
    String exportarStockJson();
}
