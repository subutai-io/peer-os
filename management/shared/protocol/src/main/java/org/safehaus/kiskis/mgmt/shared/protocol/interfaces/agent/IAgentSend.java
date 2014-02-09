package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.agent;

import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

public interface IAgentSend {
    public Request send(Response response);
}
