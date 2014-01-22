package org.safehaus.kiskis.mgmt.shared.protocol.api;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/8/13 Time: 8:22 PM To
 * change this template use File | Settings | File Templates.
 */
public interface CommunicationService {

    public void sendCommand(Command command);

    public void addListener(ResponseListener listener);

    public void removeListener(ResponseListener listener);
}
