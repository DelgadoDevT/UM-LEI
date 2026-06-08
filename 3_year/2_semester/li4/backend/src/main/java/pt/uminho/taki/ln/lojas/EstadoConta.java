package pt.uminho.taki.ln.lojas;

/**
 * Enumeracao que define a maquina de estados de uma conta de Funcionario.
 * Reflete as restricoes do esquema PostgreSQL ('Ativo' ou 'Bloqueado').
 *
 * @author TakiLN Team
 * @since 1.0
 */
public enum EstadoConta {
    /** A conta encontra-se operacional e o acesso e permitido. */
    ATIVO,
    /** A conta encontra-se bloqueada temporariamente por motivos de seguranca. */
    BLOQUEADO,
    /** A conta foi removida logicamente, mantendo histórico. */
    INATIVO
}
