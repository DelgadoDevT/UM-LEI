package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;

import java.util.List;
import java.util.Optional;

/**
 * Serviço para ProdutoFornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class ProdutoFornecedorService implements IProdutoFornecedorService {

    private final FornecedorDAO fornecedorDAO;
    private final ProdutoFornecedorDAO pfDAO;

    /**
     * Constrói um novo ProdutoFornecedorService.
     *
     * @param fornecedorDAO o DAO de fornecedor
     * @param pfDAO o DAO de produto-fornecedor
     */
    public ProdutoFornecedorService(FornecedorDAO fornecedorDAO, ProdutoFornecedorDAO pfDAO) {
        this.fornecedorDAO = fornecedorDAO;
        this.pfDAO = pfDAO;
    }

    /**
     * Associa um produto a um fornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     * @param precoCusto o preço de custo
     * @throws FornecedorInativoException se o fornecedor se encontrar inativo.
     */
    @Override
    public void associarProdutoAFornecedor(String idProduto, String idFornecedor, double precoCusto) throws FornecedorInativoException {
        Optional<Fornecedor> forn = fornecedorDAO.findById(idFornecedor);

        if (forn.isEmpty()) {
            throw new IllegalArgumentException("O Fornecedor com o ID indicado não existe no sistema.");
        }

        if (forn.get().isInativo()) {
            throw new FornecedorInativoException("Não é possível associar produtos a Fornecedores Inativos.");
        }

        List<ProdutoFornecedor> atuais = pfDAO.findByIdProduto(idProduto);
        
        // Obter ou criar a associação
        Optional<ProdutoFornecedor> existente = pfDAO.findByIdProdutoAndIdFornecedor(idProduto, idFornecedor);
        ProdutoFornecedor associacao = existente.orElseGet(() -> new ProdutoFornecedor(idProduto, idFornecedor, precoCusto));
        
        associacao.setPrecoCusto(precoCusto);

        boolean isPreferencial = true;
        
        for (ProdutoFornecedor p : atuais) {
            // Ignora o fornecedor atual na comparação
            if (p.getIdFornecedor().equals(idFornecedor)) {
                continue;
            }
            
            // Se um fornecedor atual tem preço menor ou igual, a nova associação não é preferencial
            if (p.getPrecoCusto() <= precoCusto) {
                isPreferencial = false;
            } 
            
            // Se o novo preço é estritamente menor que um antigo preferencial, retira o estatuto ao antigo
            if (precoCusto < p.getPrecoCusto() && p.isPreferencial()) {
                p.setPreferencial(false);
                pfDAO.save(null, p);
            }
        }
        
        associacao.setPreferencial(isPreferencial);
        // O ProdutoFornecedorDAO ignora o parâmetro key no save e utiliza o objeto
        pfDAO.save(null, associacao);
    }

    /**
     * Remove uma associação entre um produto e um fornecedor.
     *
     * @param idProduto o identificador do produto
     * @param idFornecedor o identificador do fornecedor
     */
    @Override
    public void removerAssociacao(String idProduto, String idFornecedor) {
        pfDAO.deleteAssociation(idProduto, idFornecedor);
    }

    /**
     * Consulta os fornecedores associados a um produto.
     *
     * @param idProduto o identificador do produto
     * @return uma lista de associações
     */
    @Override
    public List<ProdutoFornecedor> consultarPorProduto(String idProduto) {
        return pfDAO.findByIdProduto(idProduto);
    }

    /**
     * Consulta os produtos associados a um fornecedor.
     *
     * @param idFornecedor o identificador do fornecedor
     * @return uma lista de associações
     */
    @Override
    public List<ProdutoFornecedor> consultarPorFornecedor(String idFornecedor) {
        return pfDAO.findByIdFornecedor(idFornecedor);
    }
}
