package org.safehaus.kiskis.mgmt.shared.protocol.api;


import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;


/**
 * Used by Management Server components query and manage the managed Agents
 */
public interface BrokerInterface {
    /**
     * For Communication Bundle
     * @param response
     * @return
     */
    public Request distribute(Response response);

}
