package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/7/13
 * Time: 10:40 PM
 */
public interface PersistenceCommandInterface {
    List<Command> getCommandList(Agent agent);

    boolean saveCommand(Command command);

    boolean saveResponse(Response response);
}
