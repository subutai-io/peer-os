package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

abstract class AbstractHandler extends AbstractOperationHandler<StormImpl> {

    public AbstractHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
    }

    static boolean isZero(Integer i) {
        return i != null && i == 0;
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

    boolean configure(StormConfig config) {
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

    private String makeZookeeperServersList(StormConfig config) {
        if(config.isExternalZookeeper()) {
            ZookeeperClusterConfig zkConfig = manager.getZookeeperManager().getCluster(
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
