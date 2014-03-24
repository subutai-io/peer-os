package org.safehaus.kiskis.mgmt.api.communicationmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public interface ResponseListener {

    public void onResponse(Response response);

}
