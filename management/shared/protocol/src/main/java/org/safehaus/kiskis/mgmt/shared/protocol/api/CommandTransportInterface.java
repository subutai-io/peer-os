package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/8/13 Time: 8:22 PM To
 * change this template use File | Settings | File Templates.
 */
public interface CommandTransportInterface {

    public void sendCommand(Command command);

    public void addListener(BrokerListener listener);

    public void removeListener(BrokerListener listener);
}
