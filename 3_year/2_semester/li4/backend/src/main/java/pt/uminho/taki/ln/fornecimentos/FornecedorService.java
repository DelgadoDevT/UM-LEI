package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import java.util.Optional;

/**
 * Classe de serviço para operações de Fornecedor.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FornecedorService implements IFornecedorService {

    private final FornecedorDAO fornecedorDAO;

    /**
     * Constrói uma nova instância de FornecedorService.
     *
     * @param fornecedorDAO o objeto de acesso a dados (DAO) do fornecedor
     */
    public FornecedorService(FornecedorDAO fornecedorDAO) {
        this.fornecedorDAO = fornecedorDAO;
    }

    /**
     * Valida se um Número de Identificação Fiscal (NIF) possui um formato correto.
     *
     * @param nif o número a validar
     * @return verdadeiro se o formato for válido, falso caso contrário
     */
    private boolean validarNIF(String nif) {
        if (nif == null) return false;
        return nif.matches("^[1-9]\\d{8}$");
    }

    /**
     * Valida se um contacto telefónico segue os padrões nacionais ou internacionais suportados.
     *
     * @param telefone o número de telefone a validar
     * @return verdadeiro se o formato for reconhecido, falso caso contrário
     */
    private boolean validarContacto(String telefone) {
        if (telefone == null) return false;
        // Valida numeros tipo PT (+351 912345678 ou 912345678, ou 2x)
        return telefone.matches("^(\\+351)?\\s?[29]\\d{8}$");
    }

    /**
     * Adiciona um fornecedor.
     *
     * @param fornecedor o fornecedor a adicionar
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     * @throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException se o fornecedor já existir
     */
    @Override
    public void adicionarFornecedor(Fornecedor fornecedor) throws CamposObrigatoriosEmFaltaException, pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException {
        validarFornecedor(fornecedor);
        if (!validarNIF(fornecedor.getNif())) {
            throw new CamposObrigatoriosEmFaltaException("Formato de NIF inv\u00e1lido (deve conter 9 d\u00edgitos num\u00e9ricos regulares).");
        }
        if (!validarContacto(fornecedor.getTelefone())) {
            throw new CamposObrigatoriosEmFaltaException("Formato de contacto telef\u00f3nico inv\u00e1lido.");
        }
        
        Optional<Fornecedor> existente = fornecedorDAO.findByNif(fornecedor.getNif());
        if (existente.isPresent() && !existente.get().isInativo()) {
            throw new pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException("O NIF fornecido j\u00e1 se encontra registado num Fornecedor ativo da rede Taki.");
        }
        
        fornecedorDAO.save(fornecedor.getIdFornecedor(), fornecedor);
    }

    /**
     * Edita um fornecedor.
     *
     * @param fornecedor o fornecedor a editar
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     * @throws pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException se o fornecedor já existir
     */
    @Override
    public void editarFornecedor(Fornecedor fornecedor) throws CamposObrigatoriosEmFaltaException, pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException {
        validarFornecedor(fornecedor);
        if (!fornecedorDAO.exists(fornecedor.getIdFornecedor())) {
            throw new CamposObrigatoriosEmFaltaException("Fornecedor inexistente.");
        }
        Optional<Fornecedor> porNif = fornecedorDAO.findByNif(fornecedor.getNif());
        if (porNif.isPresent()
                && !porNif.get().getIdFornecedor().equals(fornecedor.getIdFornecedor())
                && !porNif.get().isInativo()) {
            throw new pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException("O NIF fornecido já se encontra em uso.");
        }
        fornecedorDAO.save(fornecedor.getIdFornecedor(), fornecedor);
    }

    /**
     * Inativa um fornecedor.
     *
     * @param idFornecedor o identificador do fornecedor
     * @throws FornecedorInativoException se o fornecedor já estiver inativo
     */
    @Override
    public void inativarFornecedor(String idFornecedor) throws FornecedorInativoException {
        Optional<Fornecedor> forn = fornecedorDAO.findById(idFornecedor);
        if (forn.isPresent()) {
            if (forn.get().isInativo()) {
                throw new FornecedorInativoException("O Fornecedor j\u00e1 est\u00e1 inativo.");
            }
            if (fornecedorDAO.hasEncomendasPendentes(idFornecedor)) {
                throw new IllegalStateException("Não é possível inativar fornecedor com encomendas pendentes.");
            }
            forn.get().setEstado("Inativo");
            fornecedorDAO.save(forn.get().getIdFornecedor(), forn.get());
        }
    }

    /**
     * Lista todos os fornecedores.
     *
     * @return a lista de fornecedores
     */
    @Override
    public java.util.List<Fornecedor> listarFornecedores() {
        return new java.util.ArrayList<>(this.fornecedorDAO.findAll());
    }

    /**
     * Pesquisa fornecedores por termo.
     *
     * @param termo o termo de pesquisa
     * @return a lista de fornecedores correspondentes
     */
    @Override
    public java.util.List<Fornecedor> pesquisarFornecedores(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarFornecedores();
        }
        String q = termo.toLowerCase();
        return listarFornecedores().stream()
                .filter(f -> (f.getNome() != null && f.getNome().toLowerCase().contains(q))
                        || (f.getNif() != null && f.getNif().toLowerCase().contains(q))
                        || (f.getEmail() != null && f.getEmail().toLowerCase().contains(q))
                        || (f.getTelefone() != null && f.getTelefone().toLowerCase().contains(q)))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Verifica a integridade dos dados obrigatórios de um objeto Fornecedor.
     *
     * @param fornecedor o objeto a validar
     * @throws CamposObrigatoriosEmFaltaException se algum dado essencial estiver ausente
     */
    private void validarFornecedor(Fornecedor fornecedor) throws CamposObrigatoriosEmFaltaException {
        if (fornecedor == null
                || fornecedor.getIdFornecedor() == null || fornecedor.getIdFornecedor().isBlank()
                || fornecedor.getNome() == null || fornecedor.getNome().isBlank()
                || fornecedor.getEmail() == null || fornecedor.getEmail().isBlank()) {
            throw new CamposObrigatoriosEmFaltaException("Dados obrigatórios do fornecedor em falta.");
        }
    }
}
