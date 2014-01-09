/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

/**
 *
 * @author dilshat
 */
class CacheEntry<ValueType> {

    private final ValueType value;
    private final long createTimestamp;
    private final long ttlMs;

    public CacheEntry(ValueType value, long ttlMs) {
        this.value = value;
        this.ttlMs = ttlMs;
        this.createTimestamp = System.currentTimeMillis();
    }

    public ValueType getValue() {
        return value;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > createTimestamp + ttlMs;
    }

}
