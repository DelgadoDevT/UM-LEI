package pt.uminho.taki.dao;

import java.util.Collection;
import java.util.Optional;

/**
 * Interface de Objeto de Acesso a Dados (DAO).
 * 
 * @author TakiLN Team
 * @since 1.0
 * @param <K> o tipo da chave
 * @param <V> o tipo do valor
 */
public interface DAO<K, V> {
    /**
     * Guarda uma entidade.
     * 
     * @param key a chave
     * @param entity a entidade
     * @return a entidade guardada
     */
    V save(K key, V entity);

    /**
     * Procura uma entidade por identificador.
     * 
     * @param key a chave
     * @return um Optional que contém a entidade, se encontrada, ou vazio caso contrário
     */
    Optional<V> findById(K key);

    /**
     * Procura todas as entidades.
     * 
     * @return uma coleção de todas as entidades
     */
    Collection<V> findAll();

    /**
     * Verifica se uma entidade existe por identificador.
     * 
     * @param key a chave
     * @return true se a entidade existir, false caso contrário
     */
    boolean exists(K key);

    /**
     * Elimina uma entidade por identificador.
     * 
     * @param key a chave
     */
    void delete(K key);

    /**
     * Conta as entidades.
     * 
     * @return o número de entidades
     */
    int count();

    /**
     * Elimina todas as entidades.
     */
    void clear();
}
