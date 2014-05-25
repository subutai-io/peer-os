package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.shared.protocol.Agent;

abstract class AbstractHandler implements Runnable {

    final SqoopImpl manager;
    final String clusterName;
    final ProductOperation po;

    String hostname;

    public AbstractHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        this.manager = manager;
        this.clusterName = clusterName;
        this.po = po;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    Config getClusterConfig() {
        return manager.getDbManager().getInfo(Config.PRODUCT_KEY, clusterName,
                Config.class);
    }

    boolean isNodeConnected(String hostname) {
        return manager.getAgentManager().getAgentByHostname(hostname) != null;
    }

    /**
     * Checks if nodes are connected and, optionally, removes nodes that are not
     * connected.
     *
     * @param removeDisconnected
     * @return number of connected nodes
     */
    int checkNodes(Config config, boolean removeDisconnected) {
        int connected = 0;
        Iterator<Agent> it = config.getNodes().iterator();
        while(it.hasNext()) {
            Agent a = it.next();
            if(isNodeConnected(a.getHostname())) {
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
