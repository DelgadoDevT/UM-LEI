package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;

/**
 * Interface que define o contrato para a gestão de fornecedores.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IFornecedorService {
    /**
     * Regista um novo fornecedor validado pelas regras de negocio.
     * @param fornecedor o fornecedor a registar
     * @throws CamposObrigatoriosEmFaltaException se falhar a regex
     * @throws FornecedorExistenteException se existir duplicação de NIF
     */
    void adicionarFornecedor(Fornecedor fornecedor) throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException;

    /**
     * Inativa o fornecedor.
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException caso algo falhe
     */
    void inativarFornecedor(String idFornecedor) throws FornecedorInativoException;

    /**
     * Atualiza os dados de um fornecedor existente.
     * @param fornecedor fornecedor com dados atualizados
     * @throws CamposObrigatoriosEmFaltaException se os dados forem invalidos
     * @throws FornecedorExistenteException se o NIF conflitar com outro fornecedor ativo
     */
    void editarFornecedor(Fornecedor fornecedor) throws CamposObrigatoriosEmFaltaException, FornecedorExistenteException;

    /**
     * Pesquisa fornecedores por critérios livres.
     * @param termo texto de pesquisa (nome, NIF, email ou telefone)
     * @return lista filtrada de fornecedores
     */
    java.util.List<Fornecedor> pesquisarFornecedores(String termo);

    /**
     * Retorna a lista de todos os fornecedores.
     * @return lista de fornecedores
     */
    java.util.List<Fornecedor> listarFornecedores();
}
