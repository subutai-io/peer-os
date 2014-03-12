package org.safehaus.kiskis.mgmt.api.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public interface ResponseListener {

    public void onResponse(Response response);

}
