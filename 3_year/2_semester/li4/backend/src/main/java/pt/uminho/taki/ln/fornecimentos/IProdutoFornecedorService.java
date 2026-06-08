package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;

/**
 * Interface que define o contrato para a gestão das relações entre produtos e fornecedores.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IProdutoFornecedorService {
    /**
     * Associa um produto a um fornecedor e gere a preferencia (menor preco de custo).
     * @param idProduto Id do produto
     * @param idFornecedor Id do fornecedor
     * @param precoCusto custo acordado
     * @throws FornecedorInativoException se fornecedor estiver inativo
     */
    void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto) throws FornecedorInativoException;

    /**
     * Remove uma associação produto-fornecedor.
     * @param idProduto produto alvo
     * @param idFornecedor fornecedor alvo
     */
    void removerAssociacao(String idProduto, String idFornecedor);

    /**
     * Consulta associações por produto.
     * @param idProduto produto alvo
     * @return lista de associações
     */
    java.util.List<ProdutoFornecedor> consultarPorProduto(String idProduto);

    /**
     * Consulta associações por fornecedor.
     * @param idFornecedor fornecedor alvo
     * @return lista de associações
     */
    java.util.List<ProdutoFornecedor> consultarPorFornecedor(String idFornecedor);
}
