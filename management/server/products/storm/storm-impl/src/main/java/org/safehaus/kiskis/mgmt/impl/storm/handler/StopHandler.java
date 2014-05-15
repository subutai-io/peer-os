package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.CommandType;
import org.safehaus.kiskis.mgmt.impl.storm.Commands;
import org.safehaus.kiskis.mgmt.impl.storm.StormImpl;
import org.safehaus.kiskis.mgmt.impl.storm.StormService;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StopHandler extends AbstractHandler {

    private final ProductOperation po;
    private final String hostname;

    public StopHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_NAME,
                "Stop node " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
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
            String s = Commands.make(CommandType.STOP, service);
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s), set);
            manager.getCommandRunner().runCommand(cmd);
            result = result && cmd.hasSucceeded();

            po.addLog(String.format("Storm %s %s stopped on %s", service,
                    cmd.hasSucceeded() ? "" : "not",
                    agent.getHostname()));
        }
        if(result) po.addLogDone(null);
        else po.addLogFailed(null);

    }

}
