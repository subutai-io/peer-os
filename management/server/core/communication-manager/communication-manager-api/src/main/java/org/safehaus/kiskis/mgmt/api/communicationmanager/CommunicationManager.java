package org.safehaus.kiskis.mgmt.api.communicationmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;

public interface CommunicationManager {

    public void sendRequest(Request request);

    public void addListener(ResponseListener listener);

    public void removeListener(ResponseListener listener);
}
