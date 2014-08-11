package org.safehaus.subutai.impl.sqoop.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

public class DestroyNodeHandler extends AbstractHandler {

    public DestroyNodeHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    @Override
    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Sqoop installation not found: " + clusterName);
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
                new HashSet<>(Arrays.asList(agent)));

        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Sqoop successfully removed from " + hostname);
            config.getNodes().remove(agent);

            boolean b;
            if(config.getNodes().isEmpty())
                b = manager.getDbManager().deleteInfo(Config.PRODUCT_KEY,
                        clusterName);
            else
                b = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
                        config.getClusterName(), config);
            if(b) po.addLogDone("Installation info successfully updated");
            else po.addLogFailed("Failed to update installation info");

        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove Sqoop from node");
        }

    }

}
