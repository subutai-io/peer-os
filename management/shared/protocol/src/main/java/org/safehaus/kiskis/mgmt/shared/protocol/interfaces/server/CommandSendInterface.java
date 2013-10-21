package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server;

import org.safehaus.kiskismgmt.protocol.Command;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/8/13 Time: 8:22 PM To
 * change this template use File | Settings | File Templates.
 */
public interface CommandSendInterface {

    public Response sendRequestToAgent(Request request);

    public Response sendCommandToAgent(Command command);
}
