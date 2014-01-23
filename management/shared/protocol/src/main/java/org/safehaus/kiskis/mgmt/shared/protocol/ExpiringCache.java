/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author dilshat
 */
public class ExpiringCache<KeyType, ValueType> {

    private final long evictionRunIntervalMs = 10000;
    private final Map<KeyType, CacheEntry<ValueType>> entries = new ConcurrentHashMap<KeyType, CacheEntry<ValueType>>();
    private volatile long lastEvictionRun = System.currentTimeMillis();
    private final AtomicBoolean lock = new AtomicBoolean(true);

    public ValueType get(KeyType key) {
        try {
            CacheEntry<ValueType> entry = entries.get(key);
            if (!entry.isExpired()) {
                return entry.getValue();
            } else {
                entries.remove(key);
            }
        } catch (Exception ignore) {
        } finally {
            runEviction();
        }
        return null;
    }

    public boolean put(KeyType key, ValueType value, long ttlMs) {
        try {
            entries.put(key, new CacheEntry<ValueType>(value, ttlMs));
            return true;
        } catch (Exception ignore) {
        } finally {
            runEviction();
        }
        return false;
    }

    public ValueType remove(KeyType key) {
        try {
            return entries.remove(key).getValue();
        } catch (Exception ignore) {
        } finally {
            runEviction();
        }
        return null;
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    private void runEviction() {
        if (System.currentTimeMillis() - lastEvictionRun > evictionRunIntervalMs
                && lock.compareAndSet(true, false)) {
            lastEvictionRun = System.currentTimeMillis();
            Thread evictor = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (Iterator<Map.Entry<KeyType, CacheEntry<ValueType>>> it
                                = entries.entrySet().iterator(); it.hasNext();) {
                            Map.Entry<KeyType, CacheEntry<ValueType>> entry = it.next();
                            if (entry.getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    } catch (Exception ignore) {
                    } finally {
                        lock.set(true);
                    }
                }
            });
            evictor.start();
        }
    }
}
