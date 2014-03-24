package org.safehaus.kiskis.mgmt.api.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;

public interface Communication {

    public void sendRequest(Request request);

    public void addListener(ResponseListener listener);

    public void removeListener(ResponseListener listener);
}
