package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;
import java.util.List;

/**
 * Interface para o servico de gestao de perfis de acesso.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IPerfilAcessoService {
    /**
     * Regista um novo perfil no sistema.
     * @param perfil o perfil a registar
     * @throws PerfilDuplicadoException se o nome do perfil ja existir
     */
    void registarPerfil(PerfilAcesso perfil) throws PerfilDuplicadoException;

    /**
     * Edita as permissões de um perfil existente.
     * @param nomePerfil o nome do perfil a editar
     * @param permissoes lista final de permissões do perfil
     */
    void editarPerfil(String nomePerfil, List<Permissao> permissoes);

    /**
     * Verifica se um perfil possui uma determinada permissao.
     * @param nomePerfil o nome do perfil a verificar
     * @param permissao a permissao em causa
     * @return true se o perfil possuir a permissao, false caso contrario
     */
    boolean temPermissao(String nomePerfil, Permissao permissao);

    /**
     * Verifica se um perfil existe (por id ou nome).
     * @param nomePerfil identificador/nome do perfil
     * @return true se existir e estiver ativo
     */
    boolean existePerfil(String nomePerfil);

    /**
     * Lista todos os perfis disponíveis.
     * @return lista de perfis de acesso
     */
    List<PerfilAcesso> listarPerfis();
}
