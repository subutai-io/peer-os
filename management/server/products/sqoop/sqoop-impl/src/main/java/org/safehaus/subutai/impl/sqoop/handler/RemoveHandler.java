package org.safehaus.subutai.impl.sqoop.handler;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;

public class RemoveHandler extends AbstractHandler {

    public RemoveHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    @Override
    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Cluster not found: " + clusterName);
            return;
        }
        if(config.getNodes() == null || config.getNodes().isEmpty()) {
            boolean deleted = deleteClusterInfo(config);
            if(deleted) po.addLogDone("Cluster successfully removed");
            else po.addLogFailed("Failed to delete cluster info");
            return;
        }
        if(checkNodes(config, false) < config.getNodes().size()) {
            po.addLogFailed("Not all nodes are connected");
            return;
        }

        String s = CommandFactory.build(CommandType.PURGE, null);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s), config.getNodes());

        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Cluster successfully removed");

            boolean deleted = deleteClusterInfo(config);
            if(deleted) po.addLogDone(null);
            else po.addLogFailed(null);

        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Remove failed");
        }

    }

    private boolean deleteClusterInfo(Config config) {
        boolean deleted = manager.getDbManager().deleteInfo(Config.PRODUCT_KEY,
                config.getClusterName());
        if(deleted) po.addLog("Cluster info deleted from DB");
        else po.addLog("Failed to delete cluster info");
        return deleted;
    }

}
