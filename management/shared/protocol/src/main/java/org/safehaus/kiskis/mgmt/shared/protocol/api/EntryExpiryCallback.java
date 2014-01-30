/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

/**
 *
 * @author dilshat
 */
public interface EntryExpiryCallback<ValueType> {

    public void onEntryExpiry(ValueType entry);
}
