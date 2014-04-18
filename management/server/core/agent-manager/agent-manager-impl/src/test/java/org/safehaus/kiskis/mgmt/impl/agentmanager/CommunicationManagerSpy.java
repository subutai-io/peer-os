/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;

/**
 *
 * @author dilshat
 */
public class CommunicationManagerSpy implements CommunicationManager {

    private Request request;
    private boolean isListenerAdded;
    private boolean isListenerRemoved;

    public Request getRequest() {
        return request;
    }

    public boolean isIsListenerAdded() {
        return isListenerAdded;
    }

    public boolean isIsListenerRemoved() {
        return isListenerRemoved;
    }

    public void sendRequest(Request request) {
        this.request = request;
    }

    public void addListener(ResponseListener listener) {
        isListenerAdded = true;
    }

    public void removeListener(ResponseListener listener) {
        isListenerRemoved = true;
    }

}
