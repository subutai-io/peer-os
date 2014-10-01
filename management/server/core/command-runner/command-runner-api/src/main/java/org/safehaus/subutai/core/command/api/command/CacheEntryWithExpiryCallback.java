/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api.command;


import com.google.common.base.Preconditions;


/**
 * This class represents entry for {@code ExpiringCache}. Holds generic value for the specified ttl. When entry is
 * expired the supplied ExpiryCallback is called.
 */
public class CacheEntryWithExpiryCallback<VALUE> extends CacheEntry<VALUE>
{

    /**
     * callback to trigger when entry expires
     */
    private final EntryExpiryCallback<VALUE> expiryCallback;


    /**
     * Initialized the {@code CacheEntryWithExpiryCallback}
     *
     * @param value - entry value
     * @param ttlMs - entry ttl in milliseconds
     * @param expiryCallback -- callback to trigger when entry expires
     */
    public CacheEntryWithExpiryCallback( VALUE value, long ttlMs, EntryExpiryCallback<VALUE> expiryCallback )
    {
        super( value, ttlMs );
        Preconditions.checkNotNull( expiryCallback, "Callback is null" );
        this.expiryCallback = expiryCallback;
    }


    /**
     * triggers the entry expiry callback
     */
    public void callExpiryCallback()
    {
        expiryCallback.onEntryExpiry( super.getValue() );
    }
}
