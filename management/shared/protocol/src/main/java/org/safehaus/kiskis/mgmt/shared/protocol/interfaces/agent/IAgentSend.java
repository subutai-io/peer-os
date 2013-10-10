package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.agent;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

public interface IAgentSend {
    public Request send(Response response);
}
