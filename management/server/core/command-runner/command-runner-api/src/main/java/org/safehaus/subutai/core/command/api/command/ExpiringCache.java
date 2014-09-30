/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api.command;


import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This is a cache with entries having time-to-live setting. After the specified interval entry gets evicted (expires).
 * It is possible to add expiry callback to an entry to handle the expiration event
 */
public class ExpiringCache<KeyType, ValueType>
{

    private static final long evictionRunIntervalMs = 10;
    private final Map<KeyType, CacheEntry<ValueType>> entries = new ConcurrentHashMap<>();

    private ExecutorService evictor;


    /**
     * Initializes {@code ExpiringCache}. Starts evictor executor service
     */
    public ExpiringCache()
    {

        evictor = Executors.newCachedThreadPool();

        evictor.execute( new Runnable()
        {

            public void run()
            {
                while ( !Thread.interrupted() )
                {
                    try
                    {
                        for ( Iterator<Map.Entry<KeyType, CacheEntry<ValueType>>> it = entries.entrySet().iterator();
                              it.hasNext(); )
                        {
                            final Map.Entry<KeyType, CacheEntry<ValueType>> entry = it.next();
                            if ( entry.getValue().isExpired() )
                            {
                                it.remove();
                                if ( entry.getValue() instanceof CacheEntryWithExpiryCallback )
                                {
                                    evictor.execute( new Runnable()
                                    {

                                        public void run()
                                        {
                                            try
                                            {
                                                ( ( CacheEntryWithExpiryCallback ) entry.getValue() )
                                                        .callExpiryCallback();
                                            }
                                            catch ( Exception ignored )
                                            {
                                            }
                                        }
                                    } );
                                }
                            }
                        }

                        Thread.sleep( evictionRunIntervalMs );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
            }
        } );
    }


    /**
     * Returns entry or null if missing or expired. Resets lifespan of entry
     *
     * @param key - key for the entry
     *
     * @return value of entry or null
     */
    public ValueType get( KeyType key )
    {
        if ( key != null )
        {
            CacheEntry<ValueType> entry = entries.get( key );
            if ( entry != null && !entry.isExpired() )
            {
                entry.resetCreationTimestamp();
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * adds entry to the cache.
     *
     * @param key - key for the entry
     * @param value - entry value
     * @param ttlMs - time-to-live in milliseconds
     *
     * @return - return true if added successfully and false in case of error
     */
    public boolean put( KeyType key, ValueType value, long ttlMs )
    {
        if ( key != null && value != null && ttlMs > 0 )
        {
            entries.put( key, new CacheEntry<>( value, ttlMs ) );
            return true;
        }
        return false;
    }


    /**
     * adds entry to the cache.
     *
     * @param key - key for the entry
     * @param value - entry value
     * @param ttlMs - time-to-live in milliseconds
     * @param callback - expiry callback which is called when entry is being evicted
     *
     * @return - return true if added successfully and false in case of error
     */
    public boolean put( KeyType key, ValueType value, long ttlMs, EntryExpiryCallback<ValueType> callback )
    {
        if ( key != null && value != null && ttlMs > 0 )
        {
            entries.put( key, new CacheEntryWithExpiryCallback<>( value, ttlMs, callback ) );
            return true;
        }
        return false;
    }


    /**
     * Removes and return value from the cache by key or null if missing.
     *
     * @param key - entry key
     *
     * @return entry value
     */
    public ValueType remove( KeyType key )
    {
        if ( key != null )
        {
            CacheEntry<ValueType> entry = entries.remove( key );
            if ( entry != null )
            {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * Returns map of entries.
     *
     * @return map of entries
     */
    public Map<KeyType, CacheEntry<ValueType>> getEntries()
    {
        return Collections.unmodifiableMap( entries );
    }


    /**
     * Clears all cache entries.
     */
    public void clear()
    {
        entries.clear();
    }


    /**
     * Returns number of cache entries.
     *
     * @return number of cache entries
     */
    public int size()
    {
        return entries.size();
    }


    /**
     * Disposes cache
     */
    public void dispose()
    {
        try
        {
            evictor.shutdown();
            entries.clear();
        }
        catch ( Exception e )
        {
        }
    }
}
