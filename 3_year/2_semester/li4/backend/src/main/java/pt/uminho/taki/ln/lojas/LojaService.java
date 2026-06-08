package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.dao.LojaDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementação do serviço de gestão de lojas utilizando o LojaDAO para persistência.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class LojaService implements ILojaService {

    private final LojaDAO lojaDAO;

    /**
     * Construtor para injeção do LojaDAO.
     * 
     * @param lojaDAO o DAO de lojas
     */
    public LojaService(LojaDAO lojaDAO) {
        this.lojaDAO = lojaDAO;
    }

    /**
     * Regista uma nova loja no sistema.
     * 
     * @param loja a entidade de loja a registar
     * @return a entidade de loja registada com o identificador atribuído
     * @throws IllegalArgumentException se a loja for nula
     */
    @Override
    public Loja registarLoja(Loja loja) {
        if (loja == null) {
            throw new IllegalArgumentException("A loja não pode ser nula.");
        }
        loja.setIdLoja(null);
        return this.lojaDAO.save(null, loja);
    }

    /**
     * Procura uma loja através do seu identificador.
     * 
     * @param idLoja o identificador da loja
     * @return um optional que contém a entidade de loja, se encontrada
     */
    @Override
    public Optional<Loja> buscarLoja(int idLoja) {
        return this.lojaDAO.findById(idLoja);
    }

    /**
     * Lista todas as lojas registadas no sistema.
     * 
     * @return uma lista com todas as entidades de loja
     */
    @Override
    public List<Loja> listarLojas() {
        return new ArrayList<>(this.lojaDAO.findAll());
    }

    /**
     * Atualiza os dados de uma loja existente na base de dados.
     * 
     * @param loja a entidade de loja com os dados atualizados
     * @return a entidade de loja após a atualização
     * @throws IllegalArgumentException se a loja for nula ou não tiver identificador
     */
    @Override
    public Loja atualizarLoja(Loja loja) {
        if (loja == null || loja.getIdLoja() == null) {
            throw new IllegalArgumentException("Loja inválida ou sem identificador para atualização.");
        }
        
        Loja lojaExistente = this.lojaDAO.findById(loja.getIdLoja())
                .orElseThrow(() -> new NoSuchElementException("Loja com ID " + loja.getIdLoja() + " não encontrada para atualização."));
                
        if (loja.getNome() != null && !loja.getNome().isBlank()) lojaExistente.setNome(loja.getNome());
        if (loja.getTelefone() != null && !loja.getTelefone().isBlank()) lojaExistente.setTelefone(loja.getTelefone());
        if (loja.getEmail() != null && !loja.getEmail().isBlank()) lojaExistente.setEmail(loja.getEmail());
        if (loja.getNif() != null && !loja.getNif().isBlank()) lojaExistente.setNif(loja.getNif());
        if (loja.getRua() != null && !loja.getRua().isBlank()) lojaExistente.setRua(loja.getRua());
        if (loja.getCidade() != null && !loja.getCidade().isBlank()) lojaExistente.setCidade(loja.getCidade());
        if (loja.getDistrito() != null && !loja.getDistrito().isBlank()) lojaExistente.setDistrito(loja.getDistrito());

        return this.lojaDAO.save(lojaExistente.getIdLoja(), lojaExistente);
    }

    /**
     * Remove uma loja do sistema através do seu identificador.
     * 
     * @param idLoja o identificador da loja a remover
     */
    @Override
    public void removerLoja(int idLoja) {
        if (!this.lojaDAO.exists(idLoja)) {
            throw new NoSuchElementException("Loja com ID " + idLoja + " não encontrada para remoção.");
        }
        this.lojaDAO.delete(idLoja);
    }
}
