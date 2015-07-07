/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.cache;


import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a cache with entries having time-to-live setting. After the specified interval entry gets evicted (expires).
 * It is possible to add expiry callback to an entry to handle the expiration event
 */
public class ExpiringCache<K, V>
{
    private static final Logger LOG = LoggerFactory.getLogger( ExpiringCache.class.getName() );

    private static final long EVICTION_RUN_INTERVAL_MS = 10;
    private final Map<K, CacheEntry<V>> entries = new ConcurrentHashMap<>();

    private final ScheduledExecutorService evictor;
    private final ExecutorService expirationNotifier;


    /**
     * Initializes {@code ExpiringCache}. Starts evictor executor service
     */
    public ExpiringCache()
    {

        evictor = Executors.newSingleThreadScheduledExecutor();

        expirationNotifier = Executors.newCachedThreadPool();

        evictor.scheduleWithFixedDelay( new Runnable()
        {
            public void run()
            {
                try
                {
                    evictExpiredEntries();
                }
                catch ( Exception e )
                {
                    LOG.error( "Error in eviction task", e );
                }
            }
        }, 0, EVICTION_RUN_INTERVAL_MS, TimeUnit.MILLISECONDS );
    }


    private void evictExpiredEntries()
    {
        for ( Iterator<Map.Entry<K, CacheEntry<V>>> it = entries.entrySet().iterator(); it.hasNext(); )
        {
            final Map.Entry<K, CacheEntry<V>> entry = it.next();
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
        expirationNotifier.execute( new Runnable()
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
    public V get( K key )
    {
        if ( key != null )
        {
            CacheEntry<V> entry = entries.get( key );
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
    public boolean put( K key, V value, long ttlMs )
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
    public boolean put( K key, V value, long ttlMs, EntryExpiryCallback<V> callback )
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
    public V remove( K key )
    {
        if ( key != null )
        {
            CacheEntry<V> entry = entries.remove( key );
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
    public Map<K, CacheEntry<V>> getEntries()
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
        expirationNotifier.shutdown();
        entries.clear();
    }
}
