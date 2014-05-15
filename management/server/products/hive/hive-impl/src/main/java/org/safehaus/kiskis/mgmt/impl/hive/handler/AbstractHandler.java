package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

abstract class AbstractHandler extends AbstractOperationHandler<HiveImpl> {

    public AbstractHandler(HiveImpl manager, String clusterName) {
        super(manager, clusterName);
    }

    boolean isServerNode(Config config, String hostname) {
        return config.getServer().getHostname().equalsIgnoreCase(hostname);
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
    int checkClientNodes(Config config, boolean removeDisconnected) {
        int connected = 0;
        Iterator<Agent> it = config.getClients().iterator();
        while(it.hasNext()) {
            if(isNodeConnected(it.next().getHostname())) connected++;
            else if(removeDisconnected) it.remove();
        }
        return connected;
    }

    boolean isZero(Integer i) {
        return i != null && i == 0;
    }
}
