package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.dao.CategoriaDAO;
import pt.uminho.taki.ln.lojas.exceptions.CategoriaInvalidaException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe de serviço para a gestão de categorias.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class CategoriaService implements ICategoriaService {

    private final CategoriaDAO categoriaDAO;

    /**
     * Constrói uma nova instância de CategoriaService.
     *
     * @param categoriaDAO o objeto de acesso a dados da categoria
     */
    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    /**
     * Valida a hierarquia de uma categoria.
     *
     * @param idCategoriaPai o identificador da categoria pai
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    @Override
    public void validarHierarquia(String idCategoriaPai) throws CategoriaInvalidaException {
        if (idCategoriaPai != null && !idCategoriaPai.trim().isEmpty()) {
            Optional<Categoria> pai = categoriaDAO.findById(idCategoriaPai);
            if (pai.isEmpty()) {
                throw new CategoriaInvalidaException("A categoria pai (ID: " + idCategoriaPai + ") n\u00e3o existe no sistema.");
            }
            // Em cen\u00e1rios reais e mais complexos, poder\u00eda validar ciclos aqui.
        }
    }

    /**
     * Adiciona uma nova categoria.
     *
     * @param categoria a categoria a adicionar
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    @Override
    public void adicionarCategoria(Categoria categoria) throws CategoriaInvalidaException {
        validarHierarquia(categoria.getIdCategoriaPai());
        categoriaDAO.save(categoria.getIdCategoria(), categoria);
    }

    /**
     * Lista todas as categorias.
     *
     * @return uma lista de todas as categorias
     */
    @Override
    public List<Categoria> listarCategorias() {
        return new ArrayList<>(categoriaDAO.findAll());
    }

    /**
     * Edita uma categoria existente.
     *
     * @param categoria a categoria a editar
     * @throws CategoriaInvalidaException se a categoria for inválida
     */
    @Override
    public void editarCategoria(Categoria categoria) throws CategoriaInvalidaException {
        if (categoria == null || categoria.getIdCategoria() == null || categoria.getIdCategoria().isBlank()) {
            throw new CategoriaInvalidaException("Categoria inválida para edição.");
        }
        if (categoriaDAO.findById(categoria.getIdCategoria()).isEmpty()) {
            throw new CategoriaInvalidaException("Categoria não encontrada.");
        }
        validarHierarquia(categoria.getIdCategoriaPai());
        categoriaDAO.atualizarCategoria(categoria.getIdCategoria(), categoria.getDesignacao(), categoria.getIdCategoriaPai());
    }

    /**
     * Inativa uma categoria.
     *
     * @param idCategoria o identificador da categoria
     * @throws CategoriaInvalidaException se o identificador da categoria for inválido
     */
    @Override
    public void inativarCategoria(String idCategoria) throws CategoriaInvalidaException {
        if (idCategoria == null || idCategoria.isBlank()) {
            throw new CategoriaInvalidaException("ID da categoria é obrigatório.");
        }
        if (categoriaDAO.findById(idCategoria).isEmpty()) {
            throw new CategoriaInvalidaException("Categoria não encontrada.");
        }
        categoriaDAO.descontinuarCategoria(idCategoria);
    }

    /**
     * Obtém o caminho hierárquico de uma categoria.
     *
     * @param idCategoria o identificador da categoria
     * @return o caminho hierárquico como uma lista de identificadores de categoria
     */
    @Override
    public List<String> obterCaminhoHierarquico(String idCategoria) {
        List<String> caminho = new ArrayList<>();
        String atual = idCategoria;
        
        while (atual != null && !atual.isEmpty()) {
            caminho.add(atual);
            Optional<Categoria> cat = categoriaDAO.findById(atual);
            if (cat.isPresent()) {
                atual = cat.get().getIdCategoriaPai();
            } else {
                atual = null;
            }
        }
        return caminho;
    }
}
