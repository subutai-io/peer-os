package org.safehaus.subutai.plugin.sqoop.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;

public class DestroyNodeHandler extends AbstractHandler {

    public DestroyNodeHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        SqoopConfig config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Sqoop installation not found: " + clusterName);
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }
        if(!config.getNodes().contains(agent)) {
            po.addLogFailed("Node does not belong to Sqoop installation group");
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

            try {
                PluginDAO dao = manager.getPluginDao();
                if(config.getNodes().isEmpty()) {

                    destroyNodes(config);

                    dao.deleteInfo(SqoopConfig.PRODUCT_KEY, config.getClusterName());
                    po.addLogDone("Installation info deleted");
                } else {
                    dao.saveInfo(SqoopConfig.PRODUCT_KEY, config.getClusterName(), config);
                    po.addLogDone("Installation info updated");
                }
            } catch(DBException ex) {
                String m = "Failed to update installation info";
                po.addLogFailed(m);
                manager.getLogger().error(m, ex);
            }

        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove Sqoop from node");
        }

    }

    private void destroyNodes(SqoopConfig config) {
        if(config.getHadoopNodes() == null || config.getHadoopNodes().isEmpty())
            return;

        productOperation.addLog("Destroying nodes...");
        try {
            manager.getContainerManager().clonesDestroy(config.getHadoopNodes());
            manager.getLogger().info("Destroyed {} node(s)", config.getHadoopNodes().size());
            productOperation.addLog("Nodes successfully destroyed");
        } catch(LxcDestroyException ex) {
            String m = "Failed to detroy node(s)";
            productOperation.addLog(m + ": " + ex.getMessage());
            manager.getLogger().error(m, ex);
        }
    }

}
