/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used internally by {@code TaskRunnerImpl}. This is a cache with
 * entries having time-to-live setting After the specified interval entry gets
 * evicted (expires) It is possible to add expiry callback to an entry to handle
 * the expiration event
 *
 * @author dilshat
 */
class ExpiringCache<KeyType, ValueType> {

    private static final Logger LOG = Logger.getLogger(ExpiringCache.class.getName());

    private final long evictionRunIntervalMs = 200;
    private final Map<KeyType, CacheEntry<ValueType>> entries = new ConcurrentHashMap<KeyType, CacheEntry<ValueType>>();
//    private volatile long lastEvictionRun = System.currentTimeMillis();
//    private final AtomicBoolean lock = new AtomicBoolean(true);

    /**
     * Initializes {@code ExpiringCache}
     *
     * @param evictor - executor service used to evict expired entries
     */
    public ExpiringCache(ExecutorService evictor) {
        evictor.execute(new Runnable() {

            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        for (Iterator<Map.Entry<KeyType, CacheEntry<ValueType>>> it
                                = entries.entrySet().iterator(); it.hasNext();) {
                            Map.Entry<KeyType, CacheEntry<ValueType>> entry = it.next();
                            if (entry.getValue().isExpired()) {
                                it.remove();
                                if (entry.getValue() instanceof CacheEntryWithExpiryCallback) {
                                    try {
                                        ((CacheEntryWithExpiryCallback) entry.getValue()).callExpiryCallback();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }

                        Thread.sleep(evictionRunIntervalMs);
                    } catch (Exception ex) {
                    }
                }
            }

        });

    }

    /**
     * Returns entry or null if missing or expired
     *
     * @param key - key for the entry
     * @return value of entry or null
     */
    public ValueType get(KeyType key) {
        try {
            CacheEntry<ValueType> entry = entries.get(key);
            if (!entry.isExpired()) {
                return entry.getValue();
            }
//            else {
//                entries.remove(key);
//            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in get", ex);
//        } finally {
//            runEviction();
        }
        return null;
    }

    /**
     * adds entry to the cache.
     *
     * @param key - key for the entry
     * @param value - entry value
     * @param ttlMs - time-to-live in milliseconds
     * @return - return true if added successfully and false in case of error
     */
    public boolean put(KeyType key, ValueType value, long ttlMs) {
        try {
            entries.put(key, new CacheEntry<ValueType>(value, ttlMs));
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in put", ex);
//        } finally {
//            runEviction();
        }
        return false;
    }

    public boolean put(KeyType key, ValueType value, long ttlMs, EntryExpiryCallback<ValueType> callback) {
        try {
            entries.put(key, new CacheEntryWithExpiryCallback<ValueType>(value, ttlMs, callback));
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in put", ex);
//        } finally {
//            runEviction();
        }
        return false;
    }

    public ValueType remove(KeyType key) {
        try {
            return entries.remove(key).getValue();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in remove", ex);
//        } finally {
//            runEviction();
        }
        return null;
    }

    public Map<KeyType, CacheEntry<ValueType>> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public void clear() {
//        for (Map.Entry<KeyType, CacheEntry<ValueType>> entry : entries.entrySet()) {
//            if (entry.getValue() instanceof CacheEntryWithExpiryCallback) {
//                try {
//                    ((CacheEntryWithExpiryCallback) entry.getValue()).callExpiryCallback();
//                } catch (Exception e) {
//                }
//            }
//        }
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

//    private void runEviction() {
//        if (System.currentTimeMillis() - lastEvictionRun > evictionRunIntervalMs
//                && lock.compareAndSet(true, false)) {
//            lastEvictionRun = System.currentTimeMillis();
//            Thread evictor = new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        for (Iterator<Map.Entry<KeyType, CacheEntry<ValueType>>> it
//                                = entries.entrySet().iterator(); it.hasNext();) {
//                            Map.Entry<KeyType, CacheEntry<ValueType>> entry = it.next();
//                            if (entry.getValue().isExpired()) {
//                                it.remove();
//                                if (entry.getValue() instanceof CacheEntryWithExpiryCallback) {
//                                    try {
//                                        ((CacheEntryWithExpiryCallback) entry.getValue()).callExpiryCallback();
//                                    } catch (Exception e) {
//                                    }
//                                }
//                            }
//                        }
//                    } catch (Exception ignore) {
//                    } finally {
//                        lock.set(true);
//                    }
//                }
//            });
//            evictor.start();
//        }
//    }
}
