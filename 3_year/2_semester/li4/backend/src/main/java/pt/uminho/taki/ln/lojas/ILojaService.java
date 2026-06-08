package pt.uminho.taki.ln.lojas;

import java.util.List;
import java.util.Optional;

/**
 * Interface para o serviço de gestão de lojas.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public interface ILojaService {
    /**
     * Regista uma nova loja no sistema.
     * 
     * @param loja a entidade de loja a registar
     * @return a entidade de loja registada com o identificador atribuído
     */
    Loja registarLoja(Loja loja);

    /**
     * Procura uma loja através do seu identificador.
     * 
     * @param idLoja o identificador da loja
     * @return um optional que contém a entidade de loja, se encontrada
     */
    Optional<Loja> buscarLoja(int idLoja);

    /**
     * Lista todas as lojas registadas no sistema.
     * 
     * @return uma lista com todas as entidades de loja
     */
    List<Loja> listarLojas();

    /**
     * Atualiza os dados de uma loja existente na base de dados.
     * 
     * @param loja a entidade de loja com os dados atualizados
     * @return a entidade de loja após a atualização
     */
    Loja atualizarLoja(Loja loja);

    /**
     * Remove uma loja do sistema através do seu identificador.
     * 
     * @param idLoja o identificador da loja a remover
     */
    void removerLoja(int idLoja);
}
