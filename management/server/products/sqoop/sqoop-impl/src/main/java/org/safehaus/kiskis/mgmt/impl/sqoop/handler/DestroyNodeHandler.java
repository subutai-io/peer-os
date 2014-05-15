package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandFactory;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandType;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class DestroyNodeHandler extends AbstractHandler {

    public DestroyNodeHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Cluster not found: " + clusterName);
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        String s = CommandFactory.build(CommandType.PURGE, null);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<Agent>(Arrays.asList(agent)));

        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Sqoop successfully removed from " + hostname);
            config.getNodes().remove(agent);

            boolean saved = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
                    config.getClusterName(), config);
            if(saved) po.addLogDone("Cluster info successfully saved");
            else po.addLogFailed("Failed to save cluster info");

        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove node from cluster");
        }

    }

}
