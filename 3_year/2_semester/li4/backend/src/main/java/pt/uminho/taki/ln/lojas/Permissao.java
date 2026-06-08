package pt.uminho.taki.ln.lojas;

/**
 * Enumeracao que define as permissoes base do sistema.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public enum Permissao {
    /** Permissao para efetuar o registo de vendas no POS. */
    REGISTAR_VENDA,
    /** Permissao para gerir o catalogo de produtos e categorias. */
    GERIR_PRODUTOS,
    /** Permissao para aceder a configuracoes criticas do sistema. */
    ADMINISTRAR_SISTEMA
}
