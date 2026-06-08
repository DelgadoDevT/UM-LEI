package pt.uminho.taki.api.shared.seguranca;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa o contexto de autenticação.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class ContextoAutenticacao {

    private final String subject;
    private final String email;
    private final Set<String> roles;
    private final Set<String> scopes;
    private final String token;

    /**
     * Constrói um novo contexto de autenticação.
     * 
     * @param subject o sujeito (subject)
     * @param email o correio eletrónico
     * @param roles as funções (roles)
     * @param scopes os âmbitos (scopes)
     * @param token o token
     */
    public ContextoAutenticacao(String subject, String email, Set<String> roles, Set<String> scopes, String token) {
        this.subject = Objects.requireNonNull(subject, "subject");
        this.email = email;
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(roles, "roles")));
        this.scopes = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(scopes, "scopes")));
        this.token = Objects.requireNonNull(token, "token");
    }

    /**
     * Obtém o sujeito (subject).
     * 
     * @return o sujeito (subject)
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Obtém o correio eletrónico.
     * 
     * @return o correio eletrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Obtém as funções (roles).
     * 
     * @return as funções (roles)
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Obtém os âmbitos (scopes).
     * 
     * @return os âmbitos (scopes)
     */
    public Set<String> getScopes() {
        return scopes;
    }

    /**
     * Obtém o token.
     * 
     * @return o token
     */
    public String getToken() {
        return token;
    }
}
