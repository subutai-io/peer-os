package org.safehaus.subutai.common.protocol.api;


import java.util.Collection;


public interface DataService<K, T>
{
    Collection<T> getAll();

    T find( K id );

    void persist( T item );

    void remove( T item );
}
