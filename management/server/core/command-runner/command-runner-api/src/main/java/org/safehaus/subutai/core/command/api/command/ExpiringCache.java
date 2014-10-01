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
public class ExpiringCache<KEY, VALUE>
{

    private static final long EVICTION_RUN_INTERVAL_MS = 10;
    private final Map<KEY, CacheEntry<VALUE>> entries = new ConcurrentHashMap<>();

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

                        evictExpiredEntries();

                        Thread.sleep( EVICTION_RUN_INTERVAL_MS );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
            }
        } );
    }


    private void evictExpiredEntries()
    {
        for ( Iterator<Map.Entry<KEY, CacheEntry<VALUE>>> it = entries.entrySet().iterator(); it.hasNext(); )
        {
            final Map.Entry<KEY, CacheEntry<VALUE>> entry = it.next();
            if ( entry.getValue().isExpired() )
            {
                it.remove();
                if ( entry.getValue() instanceof CacheEntryWithExpiryCallback )
                {
                    evictEntry( ( CacheEntryWithExpiryCallback ) entry.getValue() );
                }
            }
        }
    }


    private void evictEntry( final CacheEntryWithExpiryCallback callback )
    {
        evictor.execute( new Runnable()
        {

            public void run()
            {
                callback.callExpiryCallback();
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
    public VALUE get( KEY key )
    {
        if ( key != null )
        {
            CacheEntry<VALUE> entry = entries.get( key );
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
    public boolean put( KEY key, VALUE value, long ttlMs )
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
    public boolean put( KEY key, VALUE value, long ttlMs, EntryExpiryCallback<VALUE> callback )
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
    public VALUE remove( KEY key )
    {
        if ( key != null )
        {
            CacheEntry<VALUE> entry = entries.remove( key );
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
    public Map<KEY, CacheEntry<VALUE>> getEntries()
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
        evictor.shutdown();
        entries.clear();
    }
}
