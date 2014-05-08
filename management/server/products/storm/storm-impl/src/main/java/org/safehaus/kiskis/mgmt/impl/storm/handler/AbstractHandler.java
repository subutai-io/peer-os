package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.impl.storm.StormImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

abstract class AbstractHandler extends AbstractOperationHandler<StormImpl> {

    public AbstractHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
    }

    boolean isNimbusNode(Config config, String hostname) {
        return config.getNimbus().getHostname().equalsIgnoreCase(hostname);
    }

    boolean isNodeConnected(String hostname) {
        return manager.getAgentManager().getAgentByHostname(hostname) != null;
    }

    /**
     * Checks if client nodes are connected and, optionally, removes nodes that
     * are not connected.
     *
     * @param removeDisconnected
     * @return number of connected nodes
     */
    int checkSupervisorNodes(Config config, boolean removeDisconnected) {
        int connected = 0;
        Iterator<Agent> it = config.getSupervisors().iterator();
        while(it.hasNext()) {
            Agent a = it.next();
            if(isNodeConnected(a.getHostname())) connected++;
            else if(removeDisconnected) it.remove();
        }
        return connected;
    }

    static boolean isZero(Integer i) {
        return i != null && i == 0;
    }
}
