package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Iterator;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

abstract class AbstractHandler extends AbstractOperationHandler<HiveImpl> {

    final ProductOperation po;

    public AbstractHandler(HiveImpl manager, String clusterName, String desc) {
        super(manager, clusterName);
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY, desc);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
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
            Agent a = it.next();
            if(manager.getAgentManager().getAgentByHostname(a.getHostname()) != null) {
                connected++;
                continue;
            }
            String m = String.format("Node '%s' is not connected.", a.getHostname());
            if(removeDisconnected) {
                it.remove();
                m += " Omitting from clients list";
            }
            po.addLog(m);
        }
        return connected;
    }

    boolean isZero(Integer i) {
        return i != null && i == 0;
    }
}
