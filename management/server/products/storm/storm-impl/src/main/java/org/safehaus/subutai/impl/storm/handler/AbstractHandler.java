package org.safehaus.subutai.impl.storm.handler;

import java.util.*;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.impl.storm.Commands;
import org.safehaus.subutai.impl.storm.StormImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;

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

    boolean configure(Config config) {
        String zk_servers = makeZookeeperServersList(config);
        if(zk_servers == null) return false;

        Map<String, String> paramValues = new LinkedHashMap<>();
        paramValues.put("storm.zookeeper.servers", zk_servers);
        paramValues.put("storm.local.dir", "/var/lib/storm");
        paramValues.put("nimbus.host", config.getNimbus().getListIP().get(0));

        Set<Agent> allNodes = new HashSet<>(config.getSupervisors());
        allNodes.add(config.getNimbus());

        for(Map.Entry<String, String> e : paramValues.entrySet()) {
            String s = Commands.configure("add", "storm.xml", e.getKey(), e.getValue());
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s), allNodes);
            manager.getCommandRunner().runCommand(cmd);
            if(!cmd.hasSucceeded()) return false;
        }
        return true;
    }

    private String makeZookeeperServersList(Config config) {
        if(config.isExternalZookeeper()) {
            org.safehaus.subutai.api.zookeeper.Config zkConfig
                    = manager.getZookeeperManager().getCluster(
                            config.getZookeeperClusterName());
            if(zkConfig != null) {
                StringBuilder sb = new StringBuilder();
                for(Agent a : zkConfig.getNodes()) {
                    if(sb.length() > 0) sb.append(",");
                    sb.append(a.getListIP().get(0));
                }
                return sb.toString();
            }
        } else if(config.getNimbus() != null)
            return config.getNimbus().getListIP().get(0);

        return null;
    }
}
