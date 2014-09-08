package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.impl.StormService;

public class StartHandler extends AbstractHandler {

    private final String hostname;

    public StartHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME,
                "Start node " + hostname);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        StormConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist",
                    clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(hostname + " is not connected");
            return;
        }
        Set<Agent> set = new HashSet<>(2);
        set.add(agent);

        StormService[] services = isNimbusNode(config, hostname)
                ? new StormService[]{StormService.NIMBUS, StormService.UI}
                : new StormService[]{StormService.SUPERVISOR};
        boolean result = true;
        for(StormService service : services) {
            if(service == StormService.NIMBUS) {
                Helper h = new Helper(manager);
                if(!h.startZookeeper(config.getZookeeperClusterName(), hostname)) {
                    po.addLog("Failed to start Zookeeper on Nimbus");
                    result = false;
                    break;
                }
            }
            String s = Commands.make(CommandType.START, service);
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s).withTimeout(60), set);
            manager.getCommandRunner().runCommand(cmd);
            result = result && cmd.hasSucceeded();

            po.addLog(String.format("Storm %s %s started on %s", service,
                    cmd.hasSucceeded() ? "" : "not",
                    agent.getHostname()));
        }
        if(result) po.addLogDone(null);
        else po.addLogFailed(null);

    }

}
