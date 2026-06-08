package pt.uminho.taki.ln.vendas;

import pt.uminho.taki.dao.PromocaoDAO;
import pt.uminho.taki.ln.lojas.Produto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Serviço para Promoção.
 * @author TakiLN Team
 * @since 1.0
 */
public class PromocaoService implements IPromocaoService {

    private final PromocaoDAO promocaoDAO;
    private final pt.uminho.taki.dao.ProdutoDAO produtoDAO;
    private final pt.uminho.taki.ln.lojas.ICategoriaService categoriaService;
    private final List<Promocao> cachedPromocoes;

    /**
     * Construtor para PromocaoService.
     * @param promocaoDAO o DAO de promoção
     * @param produtoDAO o DAO de produto
     * @param categoriaService o serviço de categoria
     */
    public PromocaoService(PromocaoDAO promocaoDAO, 
                           pt.uminho.taki.dao.ProdutoDAO produtoDAO,
                           pt.uminho.taki.ln.lojas.ICategoriaService categoriaService) {
        this.promocaoDAO = promocaoDAO;
        this.produtoDAO = produtoDAO;
        this.categoriaService = categoriaService;
        this.cachedPromocoes = new ArrayList<>();
    }

    /**
     * Adiciona uma promoção.
     * @param promocao a promoção
     */
    @Override
    public void adicionarPromocao(Promocao promocao) {
        validarPromocao(promocao);
        validarConflitos(promocao);
        if (promocaoDAO != null) {
            promocaoDAO.save(promocao.getIdPromocao(), promocao);
            if (promocao.getProdutos() != null) {
                for (String idProduto : promocao.getProdutos()) {
                    promocaoDAO.addProduto(promocao.getIdPromocao(), idProduto);
                }
            }
            if (promocao.getCategorias() != null) {
                for (String idCategoria : promocao.getCategorias()) {
                    promocaoDAO.addCategoria(promocao.getIdPromocao(), idCategoria);
                }
            }
        } else {
            cachedPromocoes.add(promocao);
        }
    }

    /**
     * Cancela uma promoção.
     * @param idPromocao o identificador da promoção
     * @param motivo o motivo
     */
    @Override
    public void cancelarPromocao(String idPromocao, String motivo) {
        if (idPromocao == null || idPromocao.isBlank()) {
            throw new IllegalArgumentException("ID de promoção obrigatório para cancelamento.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo de cancelamento obrigatório.");
        }

        if (promocaoDAO != null) {
            Promocao promocao = promocaoDAO.findById(idPromocao)
                    .orElseThrow(() -> new IllegalArgumentException("Promoção inexistente."));
            promocao.setEstado("Cancelada");
            promocaoDAO.save(idPromocao, promocao);
            return;
        }

        for (Promocao p : cachedPromocoes) {
            if (idPromocao.equals(p.getIdPromocao())) {
                p.setEstado("Cancelada");
                return;
            }
        }
        throw new IllegalArgumentException("Promoção inexistente.");
    }

    /**
     * Obtém as promoções ativas.
     * @return a lista
     */
    @Override
    public List<Promocao> getPromocoesAtivas() {
        LocalDateTime agora = LocalDateTime.now();
        List<Promocao> ativas = new ArrayList<>();
        
        Collection<Promocao> todas = (promocaoDAO != null) ? promocaoDAO.findAll() : cachedPromocoes;

        for (Promocao p : todas) {
            if ("Ativa".equalsIgnoreCase(p.getEstado())) {
                boolean inicioOk = p.getDataInicio() == null || !agora.isBefore(p.getDataInicio());
                boolean fimOk = p.getDataFim() == null || !agora.isAfter(p.getDataFim());
                if (inicioOk && fimOk) {
                    if (promocaoDAO != null) {
                        p.setProdutos(promocaoDAO.getProdutos(p.getIdPromocao()));
                        p.setCategorias(promocaoDAO.getCategorias(p.getIdPromocao()));
                    }
                    ativas.add(p);
                }
            }
        }
        return ativas;
    }

    /**
     * Calcula o melhor desconto para um produto.
     * @param produto o produto
     * @return o valor decimal
     */
    @Override
    public double calcularMelhorDesconto(Produto produto) {
        if (produto == null) return 0.0;
        
        List<Promocao> ativas = getPromocoesAtivas();
        double melhorDesconto = 0.0;

        java.util.Set<String> categoriasDoProduto = (produtoDAO != null) 
            ? produtoDAO.getCategorias(produto.getIdProduto()) 
            : new java.util.HashSet<>();
        
        // Resolve a hierarquia completa de todas as categorias do produto
        java.util.Set<String> linhagemCompleta = new java.util.HashSet<>();
        if (categoriaService != null) {
            for (String catId : categoriasDoProduto) {
                linhagemCompleta.addAll(categoriaService.obterCaminhoHierarquico(catId));
            }
        } else {
            linhagemCompleta.addAll(categoriasDoProduto);
        }

        for (Promocao p : ativas) {
            boolean seAplica = false;
            boolean semFiltros = (p.getProdutos() == null || p.getProdutos().isEmpty()) && 
                                 (p.getCategorias() == null || p.getCategorias().isEmpty());
            
            if (semFiltros) {
                seAplica = true;
            } else {
                // Verifica se o produto está explicitamente na promoção
                if (p.getProdutos() != null && p.getProdutos().contains(produto.getIdProduto())) {
                    seAplica = true;
                }
                // Verifica se alguma categoria da linhagem do produto está na promoção
                if (!seAplica && p.getCategorias() != null && !p.getCategorias().isEmpty()) {
                    for (String catHierarquia : linhagemCompleta) {
                        if (p.getCategorias().contains(catHierarquia)) {
                            seAplica = true;
                            break;
                        }
                    }
                }
            }

            if (seAplica && p.getDesconto() > melhorDesconto) {
                melhorDesconto = p.getDesconto();
            }
        }
        return melhorDesconto;
    }

    /**
     * Valida os dados de uma promoção, incluindo o intervalo temporal e o valor do desconto.
     * 
     * @param promocao o objeto de promoção a validar
     * @throws IllegalArgumentException se as datas forem inválidas ou o desconto estiver fora do intervalo permitido
     */
    private void validarPromocao(Promocao promocao) {
        if (promocao == null) {
            throw new IllegalArgumentException("Promoção inválida.");
        }
        if (promocao.getDataInicio() == null || promocao.getDataFim() == null) {
            throw new IllegalArgumentException("As datas de início e fim da promoção são obrigatórias.");
        }
        if (!promocao.getDataFim().isAfter(promocao.getDataInicio())) {
            throw new IllegalArgumentException("A data de fim deve ser posterior à data de início.");
        }
        if (promocao.getDesconto() <= 0 || promocao.getDesconto() > 100) {
            throw new IllegalArgumentException("O desconto deve estar no intervalo ]0, 100].");
        }
        if (promocao.getEstado() == null || promocao.getEstado().isBlank()) {
            promocao.setEstado("Ativa");
        }
    }

    /**
     * Verifica se a nova promoção entra em conflito com campanhas já existentes no mesmo período e âmbito.
     * 
     * @param nova a promoção a submeter a validação
     * @throws IllegalArgumentException se for detetada uma sobreposição de promoções para os mesmos artigos
     */
    private void validarConflitos(Promocao nova) {
        Collection<Promocao> todas = (promocaoDAO != null) ? promocaoDAO.findAll() : cachedPromocoes;
        for (Promocao existente : todas) {
            if (existente.getIdPromocao() != null && existente.getIdPromocao().equals(nova.getIdPromocao())) {
                continue;
            }
            if ("Cancelada".equalsIgnoreCase(existente.getEstado())) {
                continue;
            }
            if (existente.getIdLoja() != null && nova.getIdLoja() != null && !existente.getIdLoja().equals(nova.getIdLoja())) {
                continue;
            }
            if (!intervalosSobrepostos(nova, existente)) {
                continue;
            }
            Set<String> produtosExistente = carregarProdutos(existente);
            Set<String> categoriasExistente = carregarCategorias(existente);
            Set<String> produtosNova = nova.getProdutos() != null ? nova.getProdutos() : new java.util.HashSet<>();
            Set<String> categoriasNova = nova.getCategorias() != null ? nova.getCategorias() : new java.util.HashSet<>();

            boolean existenteGlobal = produtosExistente.isEmpty() && categoriasExistente.isEmpty();
            boolean novaGlobal = produtosNova.isEmpty() && categoriasNova.isEmpty();
            boolean conflitoProdutos = !Collections.disjoint(produtosExistente, produtosNova);
            boolean conflitoCategorias = !Collections.disjoint(categoriasExistente, categoriasNova);

            if (existenteGlobal || novaGlobal || conflitoProdutos || conflitoCategorias) {
                throw new IllegalArgumentException("Conflito de promoção: já existe uma campanha sobreposta no mesmo intervalo.");
            }
        }
    }

    /**
     * Determina se dois intervalos temporais de promoções possuem interseção.
     * 
     * @param a a primeira promoção
     * @param b a segunda promoção
     * @return verdadeiro se os intervalos se sobrepuserem, falso caso contrário
     */
    private boolean intervalosSobrepostos(Promocao a, Promocao b) {
        return !a.getDataInicio().isAfter(b.getDataFim()) && !a.getDataFim().isBefore(b.getDataInicio());
    }

    /**
     * Carrega a lista de identificadores de produtos associados a uma promoção.
     * 
     * @param p a promoção alvo
     * @return o conjunto de identificadores de produtos
     */
    private Set<String> carregarProdutos(Promocao p) {
        if (promocaoDAO != null) {
            return promocaoDAO.getProdutos(p.getIdPromocao());
        }
        return p.getProdutos() != null ? p.getProdutos() : new java.util.HashSet<>();
    }

    /**
     * Carrega a lista de identificadores de categorias associadas a uma promoção.
     * 
     * @param p a promoção alvo
     * @return o conjunto de identificadores de categorias
     */
    private Set<String> carregarCategorias(Promocao p) {
        if (promocaoDAO != null) {
            return promocaoDAO.getCategorias(p.getIdPromocao());
        }
        return p.getCategorias() != null ? p.getCategorias() : new java.util.HashSet<>();
    }
}
