package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

abstract class AbstractHandler extends AbstractOperationHandler<StormImpl> {

    public AbstractHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
    }

    boolean isNimbusNode(StormConfig config, String hostname) {
        return config.getNimbus().getHostname().equalsIgnoreCase(hostname);
    }

    /**
     * Checks if client nodes are connected and, optionally, removes nodes that
     * are not connected.
     *
     * @param removeDisconnected
     * @return number of connected nodes
     */
    int checkSupervisorNodes(StormConfig config, boolean removeDisconnected) {
        int connected = 0;
        Iterator<Agent> it = config.getSupervisors().iterator();
        while(it.hasNext()) {
            Agent a = it.next();
            if(isNodeConnected(a.getHostname())) connected++;
            else if(removeDisconnected) it.remove();
        }
        return connected;
    }

    boolean isNodeConnected(String hostname) {
        return manager.getAgentManager().getAgentByHostname(hostname) != null;
    }

}
