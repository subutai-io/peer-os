package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public abstract class BaseHandler implements Runnable {

    final HiveImpl manager;
    final String clusterName;
    final ProductOperation po;

    final Config config;
    String hostname;

    public BaseHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        this.manager = manager;
        this.clusterName = clusterName;
        this.po = po;

        this.config = manager.getDbManager().getInfo(Config.PRODUCT_KEY,
                clusterName, Config.class);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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
    int checkClientNodes(boolean removeDisconnected) {
        Iterator<Agent> it = config.getClients().iterator();
        while(it.hasNext()) {
            Agent a = it.next();
            if(manager.getAgentManager().getAgentByHostname(a.getHostname()) == null) {
                String m = String.format("Node '%s' is not connected.", a.getHostname());
                if(removeDisconnected) {
                    it.remove();
                    m += " Omitting from installation";
                }
                po.addLog(m);
            }
        }
        return config.getClients().size();
    }
}
