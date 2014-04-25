package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandFactory;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandType;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;

public class RemoveHandler extends AbstractHandler {

    public RemoveHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Cluster not found: " + clusterName);
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

            if(manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, clusterName))
                po.addLogDone("Cluster info deleted from DB");
            else
                po.addLogFailed("Failed to delete from DB");

        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Remove failed");
        }

    }

}
