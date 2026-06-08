package pt.uminho.taki.ln.lojas;

import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementacao do servico de gestao de perfis de acesso.
 * Gere os perfis e permissoes em memoria para garantir a integridade do sistema.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class PerfilAcessoService implements IPerfilAcessoService {
    private final Map<String, PerfilAcesso> perfisEmMemoria = new HashMap<>();

    /**
     * Construtor por omissao que inicializa os perfis base do sistema.
     */
    public PerfilAcessoService() {
        inicializarPerfis();
    }

    /**
     * Inicializa os perfis pré-definidos do sistema (ADMIN, GESTOR, GERENTE, OPERADOR).
     */
    private void inicializarPerfis() {
        // ADMIN: Todas as permissoes
        PerfilAcesso admin = new PerfilAcesso("ADMIN", "Administrador do Sistema", 
            List.of(Permissao.REGISTAR_VENDA, Permissao.GERIR_PRODUTOS, Permissao.ADMINISTRAR_SISTEMA));
        
        // GESTOR_CENTRAL: Proprietario
        PerfilAcesso gestorCentral = new PerfilAcesso("GESTOR_CENTRAL", "Proprietário da Cadeia", 
            List.of(Permissao.REGISTAR_VENDA, Permissao.GERIR_PRODUTOS, Permissao.ADMINISTRAR_SISTEMA));

        // GERENTE/GESTOR_LOJA: Gestao de inventario e vendas
        PerfilAcesso gerente = new PerfilAcesso("GERENTE", "Gerente de Loja", 
            List.of(Permissao.REGISTAR_VENDA, Permissao.GERIR_PRODUTOS));
        PerfilAcesso gestorLoja = new PerfilAcesso("GESTOR_LOJA", "Gerente de Loja", 
            List.of(Permissao.REGISTAR_VENDA, Permissao.GERIR_PRODUTOS));
        
        // OPERADOR/OPERADOR_CAIXA: Apenas registo de vendas
        PerfilAcesso operador = new PerfilAcesso("OPERADOR", "Operador de Caixa", 
            List.of(Permissao.REGISTAR_VENDA));
        PerfilAcesso operadorCaixa = new PerfilAcesso("OPERADOR_CAIXA", "Operador de Caixa", 
            List.of(Permissao.REGISTAR_VENDA));

        perfisEmMemoria.put("ADMIN", admin);
        perfisEmMemoria.put("GESTOR_CENTRAL", gestorCentral);
        perfisEmMemoria.put("GERENTE", gerente);
        perfisEmMemoria.put("GESTOR_LOJA", gestorLoja);
        perfisEmMemoria.put("OPERADOR", operador);
        perfisEmMemoria.put("OPERADOR_CAIXA", operadorCaixa);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registarPerfil(PerfilAcesso perfil) throws PerfilDuplicadoException {
        String chave = (perfil.getId() != null && !perfil.getId().isBlank()) 
                       ? perfil.getId().toUpperCase() 
                       : perfil.getNome().toUpperCase();

        if (this.perfisEmMemoria.containsKey(chave) || existePerfil(perfil.getNome())) {
            throw new PerfilDuplicadoException("Ja existe um perfil com a chave ou nome: " + perfil.getNome());
        }

        // Regista o perfil em memoria
        this.perfisEmMemoria.put(chave, perfil.clone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void editarPerfil(String nomePerfil, List<Permissao> permissoes) {
        if (nomePerfil == null || nomePerfil.isBlank()) {
            throw new IllegalArgumentException("Nome do perfil é obrigatório.");
        }
        
        // Procurar por nome ou ID
        PerfilAcesso perfilExistente = this.perfisEmMemoria.get(nomePerfil.toUpperCase());
        if (perfilExistente == null) {
            perfilExistente = this.perfisEmMemoria.values().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nomePerfil))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado: " + nomePerfil));
        }

        if (permissoes == null || permissoes.isEmpty()) {
            throw new IllegalArgumentException("Perfil deve ter pelo menos uma permissão.");
        }
        perfilExistente.setPermissoes(permissoes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean temPermissao(String nomePerfil, Permissao permissao) {
        if (nomePerfil == null) return false;
        String chave = nomePerfil.toUpperCase();
        PerfilAcesso perfil = this.perfisEmMemoria.get(chave);
        
        if (perfil == null) {
            // Tenta procurar pelo Nome se a chave falhou
            perfil = this.perfisEmMemoria.values().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nomePerfil))
                .findFirst()
                .orElse(null);
        }

        // Se o perfil nao existe, o acesso e negado por omissao
        if (perfil == null) {
            return false;
        }

        return perfil.getPermissoes().contains(permissao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existePerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.isBlank()) return false;
        String chave = nomePerfil.toUpperCase();
        if (this.perfisEmMemoria.containsKey(chave)) return true;
        
        return this.perfisEmMemoria.values().stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase(nomePerfil));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PerfilAcesso> listarPerfis() {
        List<PerfilAcesso> perfis = new ArrayList<>();
        for (PerfilAcesso perfil : this.perfisEmMemoria.values()) {
            perfis.add(perfil.clone());
        }
        perfis.sort(java.util.Comparator.comparing(PerfilAcesso::getNome, String.CASE_INSENSITIVE_ORDER));
        return perfis;
    }
}
