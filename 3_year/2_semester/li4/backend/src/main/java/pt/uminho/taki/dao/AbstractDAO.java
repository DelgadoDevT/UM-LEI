package pt.uminho.taki.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Classe abstrata que representa um Objeto de Acesso a Dados (DAO).
 *
 * @param <K> o tipo da chave
 * @param <V> o tipo da entidade
 * @author TakiLN Team
 * @since 1.0
 */
public abstract class AbstractDAO<K, V> implements DAO<K, V> {

    /**
     * Obtém uma conexão à base de dados através do ConnectionManager.
     *
     * @return a conexão à base de dados
     * @throws SQLException se ocorrer um erro ao obter a conexão
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionManager.getConnection();
    }

    /**
     * Guarda a entidade com a chave fornecida.
     *
     * @param key a chave
     * @param entity a entidade a guardar
     * @return a entidade guardada
     */
    @Override
    public abstract V save(K key, V entity);

    /**
     * Procura uma entidade pela sua chave.
     *
     * @param key a chave
     * @return um Optional que contém a entidade, se encontrada, ou vazio caso contrário
     */
    @Override
    public abstract Optional<V> findById(K key);

    /**
     * Procura todas as entidades.
     *
     * @return uma coleção de todas as entidades
     */
    @Override
    public abstract Collection<V> findAll();

    /**
     * Verifica se uma entidade existe através da sua chave.
     *
     * @param key a chave
     * @return true se existir, false caso contrário
     */
    @Override
    public abstract boolean exists(K key);

    /**
     * Elimina uma entidade pela sua chave.
     *
     * @param key a chave
     */
    @Override
    public abstract void delete(K key);

    /**
     * Conta o número de entidades.
     *
     * @return o número total de registos presentes na base de dados
     */
    @Override
    public abstract int count();

    /**
     * Elimina todas as entidades.
     */
    @Override
    public abstract void clear();
}
