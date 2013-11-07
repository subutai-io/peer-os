package org.safehaus.kiskis.mgmt.server.persistence;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/7/13
 * Time: 10:57 PM
 */
public class PersistenceAgent implements PersistenceAgentInterface {
    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return null;
    }

    @Override
    public boolean saveAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " saveAgent called");
        System.out.println(agent.toString());
        return false;
    }

    // TODO Remove this method and reference in blueprint
    public void init(){
        System.out.println(this.getClass().getName() + " started");
    }

    public void destroy(){
        System.out.println(this.getClass().getName() + " stopped");
    }
}
