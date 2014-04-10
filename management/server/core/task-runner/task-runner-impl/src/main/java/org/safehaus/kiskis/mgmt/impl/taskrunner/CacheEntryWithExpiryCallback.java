/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

/**
 * This class represents entry for {@code ExpiringCache}. Holds generic value
 * for the specified ttl. When entry is expired the supplied ExpiryCallback is
 * called. Notice: the callback is not guaranteed to be called immediately when
 * entry expires. Entry callback can be called much more later or not called at
 * all. This class is used internally by {@code ExpiringCache}. The time when
 * {@code EntryExpiryCallback} is triggered solely depends on the internal
 * operation of this class.
 *
 * @author dilshat
 */
class CacheEntryWithExpiryCallback<ValueType> extends CacheEntry<ValueType> {

    /**
     * callback to trigger when entry expires
     */
    private final EntryExpiryCallback<ValueType> expiryCallback;

    /**
     * Initialized the {@code CacheEntryWithExpiryCallback}
     *
     * @param value - entry value
     * @param ttlMs - entry ttl in milliseconds
     * @param expiryCallback -- callback to trigger when entry expires
     */
    public CacheEntryWithExpiryCallback(ValueType value, long ttlMs, EntryExpiryCallback<ValueType> expiryCallback) {
        super(value, ttlMs);
        this.expiryCallback = expiryCallback;
    }

    /**
     * triggers the entry expiry callback
     */
    public void callExpiryCallback() {
        expiryCallback.onEntryExpiry(super.getValue());
    }

}
