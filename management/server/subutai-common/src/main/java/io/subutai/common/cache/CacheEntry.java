/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.cache;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;


/**
 * This class represents entry for {@code ExpiringCache}. Holds generic value for the specified ttl.
 */
public class CacheEntry<V>
{

    /**
     * entry value
     */
    private final V value;
    /**
     * time-to-live for this value
     */
    private final long ttlMs;
    /**
     * creation timestamp
     */
    private AtomicLong createTimestamp;


    /**
     * Initializes {@code CacheEntry} with the give type and ttl
     *
     * @param value - entry value
     * @param ttlMs - entry time to live in milliseconds
     */
    public CacheEntry( V value, long ttlMs )
    {
        Preconditions.checkNotNull( value, "Value is null" );
        Preconditions.checkArgument( ttlMs > 0, "Time-to-live must be greater than 0" );

        this.value = value;
        this.ttlMs = ttlMs;
        this.createTimestamp = new AtomicLong( System.nanoTime() );
    }


    /**
     * Return entry value
     *
     * @return entry value
     */
    public V getValue()
    {
        return value;
    }


    /**
     * Resets creation timestamp to the current timestamp to prolong entry's lifespan
     */
    public void resetCreationTimestamp()
    {
        createTimestamp.set( System.nanoTime() );
    }


    /**
     * Returns boolean indicating if entry has expired. Entry is considered to be expired if specified {@code ttl} has
     * passed since the moment of entry creation
     *
     * @return boolean indicating if entry has expired
     */
    public boolean isExpired()
    {
        return System.nanoTime() > createTimestamp.get() + TimeUnit.MILLISECONDS.toNanos( ttlMs );
    }
}
