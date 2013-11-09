package org.safehaus.kiskis.mgmt.server.persistence;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceCommandInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:05 PM
 */
public class PersistenceCommand implements PersistenceCommandInterface {

    @Override
    public List<Command> getCommandList(Agent agent) {
        System.out.println(this.getClass().getName() + " getCommandList called");
        System.out.println(agent.toString());
        return null;
    }

    @Override
    public boolean saveCommand(Command command) {
        System.out.println(this.getClass().getName() + " saveCommand called!!!");
        //TODO save to cassandra
        return false;
    }
}
