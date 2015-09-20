package io.subutai.common.protocol.api;


import java.util.Collection;


/**
 * Database entity object manager
 * @param <K> - entity key
 * @param <T> - entity type
 */
public interface DataService<K, T>
{
    /**
     * Gets list of all {@link T} exist in database
     * @return - {@link java.util.Collection} of {@link T}
     */
    Collection<T> getAll();

    /**
     * Returns {@link T} object for requested id {@link K}
     * <p>@param id - entity id to retrieve an object from database</p>
     * <p>@return - {@link T} object or {@code null} value</p>
     */
    T find( K id );

    /**
     * Save {@link T} object to database <b>Warning your entity object
     * key must be unique in database otherwise rollback transaction will be applied </b>
     * @param item - entity object to save
     */
    void persist( T item );

    /**
     * Delete {@link T} from database by {@link K} key
     * @param id - entity id to remove
     */
    void remove( K id );

    /**
     * Update {@link T} entity saved in database
     * @param item - entity to update
     */
    void update( T item );

}
