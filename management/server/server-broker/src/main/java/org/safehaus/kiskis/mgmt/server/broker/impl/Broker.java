package org.safehaus.kiskis.mgmt.server.broker.impl;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/10/13 Time: 4:48 PM To
 * change this template use File | Settings | File Templates.
 */
public class Broker implements BrokerInterface {

    /**
     * For Communication Bundle
     *
     * @param response
     * @return
     */
    @Override
    public Request distribute(Response response) {
        Request req = null;
        //TO-DO Distribute response to Agent or Command Bundle
        return req;
    }
}
