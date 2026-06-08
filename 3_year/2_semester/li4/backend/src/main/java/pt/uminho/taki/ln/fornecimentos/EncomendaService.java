package pt.uminho.taki.ln.fornecimentos;

import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.ProdutoFornecedorDAO;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para a gestão de encomendas.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class EncomendaService implements IEncomendaService {

    private final EncomendaDAO encomendaDAO;
    private final FornecedorDAO fornecedorDAO;
    private final ProdutoFornecedorDAO produtoFornecedorDAO;
    private pt.uminho.taki.ln.inventario.IInventarioService inventarioService;

    /**
     * Constrói uma instância de EncomendaService.
     * 
     * @param encomendaDAO o DAO de encomenda
     * @param fornecedorDAO o DAO de fornecedor
     */
    public EncomendaService(EncomendaDAO encomendaDAO, FornecedorDAO fornecedorDAO) {
        this(encomendaDAO, fornecedorDAO, null);
    }

    /**
     * Constrói uma instância de EncomendaService.
     * 
     * @param encomendaDAO o DAO de encomenda
     * @param fornecedorDAO o DAO de fornecedor
     * @param produtoFornecedorDAO o DAO de produto-fornecedor
     */
    public EncomendaService(EncomendaDAO encomendaDAO, FornecedorDAO fornecedorDAO, ProdutoFornecedorDAO produtoFornecedorDAO) {
        this.encomendaDAO = encomendaDAO;
        this.fornecedorDAO = fornecedorDAO;
        this.produtoFornecedorDAO = produtoFornecedorDAO;
    }

    /**
     * Define o serviço de inventário.
     * 
     * @param inventarioService o serviço de inventário
     */
    @Override
    public void setInventarioService(pt.uminho.taki.ln.inventario.IInventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Cria uma guia.
     * 
     * @param idEncomenda o identificador da encomenda
     * @param idFornecedor o identificador do fornecedor
     * @param idLoja o identificador da loja
     * @param linhas as linhas
     * @throws FornecedorInativoException se o fornecedor estiver inativo
     * @throws CamposObrigatoriosEmFaltaException se existirem campos obrigatórios em falta
     */
    @Override
    public void criarGuia(String idEncomenda, String idFornecedor, String idLoja, List<LinhaEncomenda> linhas) throws FornecedorInativoException, CamposObrigatoriosEmFaltaException {
        Optional<Fornecedor> forn = fornecedorDAO.findById(idFornecedor);
        if (forn.isEmpty()) {
            throw new CamposObrigatoriosEmFaltaException("Fornecedor inexistente.");
        }
        if (forn.isPresent() && forn.get().isInativo()) {
            throw new FornecedorInativoException("Não é possível criar encomendas faturadas a um Fornecedor Inativo.");
        }
        
        if (linhas == null || linhas.isEmpty()) {
            throw new CamposObrigatoriosEmFaltaException("Não é possível criar uma encomenda sem linhas de artigos.");
        }
        for (LinhaEncomenda l : linhas) {
            if (l.getQuantidade() <= 0) {
                throw new CamposObrigatoriosEmFaltaException("As linhas da encomenda devem ter quantidade positiva.");
            }
            if (produtoFornecedorDAO != null) {
                Optional<ProdutoFornecedor> assoc = produtoFornecedorDAO.findByIdProdutoAndIdFornecedor(l.getIdProduto(), idFornecedor);
                if (assoc.isEmpty()) {
                    throw new CamposObrigatoriosEmFaltaException(
                        "O produto " + l.getIdProduto() + " não está associado ao fornecedor da encomenda.");
                }
                // Override the cost price with the actual negotiated price from the database
                l.setPrecoCustoAplicado(assoc.get().getPrecoCusto());
            } else if (l.getPrecoCustoAplicado() <= 0) {
                throw new CamposObrigatoriosEmFaltaException("Preço de custo inválido e não foi possível validar a associação.");
            }
        }
        
        Encomenda enc = new Encomenda(idEncomenda, idFornecedor, idLoja);
        for (LinhaEncomenda l : linhas) {
            enc.adicionarLinha(l);
        }
        
        encomendaDAO.save(enc.getIdEncomenda(), enc);
    }

    /**
     * Processa a transição de estado.
     * 
     * @param idEncomenda o identificador da encomenda
     * @throws IllegalStateException se a transição de estado for inválida
     */
    @Override
    public void processarTransicaoEstado(String idEncomenda) throws IllegalStateException {
        Optional<Encomenda> encOpt = encomendaDAO.findById(idEncomenda);
        if (encOpt.isPresent()) {
            Encomenda enc = encOpt.get();
            String estadoAnterior = enc.getEstadoAtual().toString();
            enc.avancarEstado();
            String novoEstado = enc.getEstadoAtual().toString();
            encomendaDAO.save(enc.getIdEncomenda(), enc);

            // Se mudou para Concluída, atualizar stock
            if (!estadoAnterior.equalsIgnoreCase(novoEstado) && 
                (novoEstado.equalsIgnoreCase("Concluída") || novoEstado.contains("Concluida"))) {
                
                if (this.inventarioService != null) {
                    try {
                        int idLoja = Integer.parseInt(enc.getIdLoja());
                        for (LinhaEncomenda l : enc.getLinhas()) {
                            this.inventarioService.processarEntradaEncomenda(idLoja, l.getIdProduto(), l.getQuantidade());
                        }
                    } catch (NumberFormatException e) {
                        // Log error
                    }
                }
            }
        } else {
            throw new IllegalStateException("Encomenda inexistente.");
        }
    }

    /**
     * Calcula o valor total da guia.
     * 
     * @param idEncomenda o identificador da encomenda
     * @return o valor total
     */
    @Override
    public double calcularTotalGuia(String idEncomenda) {
        Optional<Encomenda> enc = encomendaDAO.findById(idEncomenda);
        if (enc.isPresent()) {
            return enc.get().getValorTotal();
        }
        return 0.0;
    }

    /**
     * Lista todas as encomendas.
     * 
     * @return uma lista de encomendas
     */
    @Override
    public List<Encomenda> listarEncomendas() {
        return new java.util.ArrayList<>(this.encomendaDAO.findAll());
    }
}
