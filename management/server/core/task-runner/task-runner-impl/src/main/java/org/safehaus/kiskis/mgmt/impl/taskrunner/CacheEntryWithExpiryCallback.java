/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

/**
 *
 * @author dilshat
 */
public class CacheEntryWithExpiryCallback<ValueType> extends CacheEntry<ValueType> {

    private final EntryExpiryCallback<ValueType> expiryCallback;

    public CacheEntryWithExpiryCallback(ValueType value, long ttlMs, EntryExpiryCallback<ValueType> expiryCallback) {
        super(value, ttlMs);
        this.expiryCallback = expiryCallback;
    }

    public void callExpiryCallback() {
        expiryCallback.onEntryExpiry(super.getValue());
    }

}
