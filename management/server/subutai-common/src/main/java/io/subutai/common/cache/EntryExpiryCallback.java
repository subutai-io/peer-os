/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.cache;


/**
 * This interface should be implemented for supplying expiry callbacks for entries to {@code ExpiringCache}
 */
public interface EntryExpiryCallback<V>
{

    /**
     * This method of callback is called when entry expires
     *
     * @param entry - cache entry being expired
     */
    public void onEntryExpiry( V entry );
}
