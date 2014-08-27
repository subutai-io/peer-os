package org.safehaus.subutai.plugin.flume.impl.handler;

import java.util.*;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

public class AddNodeHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;

    public AddNodeHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Add node to cluster: " + clusterName);
    }

    @Override
    public void run() {
        FlumeConfig config = manager.getCluster(clusterName);
        if(config == null) {
            productOperation.addLogFailed("Cluster not found: " + clusterName);
            return;
        }
        //check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            productOperation.addLogFailed("Node is not connected: " + hostname);
            return;
        }

        boolean added = addNode(config, agent);

        if(added) productOperation.addLogDone(null);
        else productOperation.addLogFailed(null);
    }

    boolean addNode(FlumeConfig config, Agent agent) {
        ProductOperation po = this.productOperation;
        HadoopClusterConfig hc = manager.getHadoopManager().getCluster(config.getHadoopClusterName());
        if(hc == null) {
            po.addLog(String.format("Hadoop cluster '%s' not found", config.getHadoopClusterName()));
            return false;
        }
        if(!hc.getAllNodes().contains(agent)) {
            po.addLog("Node does not belong to Hadoop cluster");
            return false;
        }

        Set<Agent> set = new HashSet<>(Arrays.asList(agent));

        po.addLog("Checking installed packages...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)), set);
        manager.getCommandRunner().runCommand(cmd);
        if(!cmd.hasSucceeded()) {
            po.addLog("Failed to check installed packages");
            return false;
        }

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(!res.getStdOut().contains("ksks-hadoop")) {
            po.addLog("Hadoop not installed on " + hostname);
            return false;
        }
        boolean installed = res.getStdOut().contains(Commands.PACKAGE_NAME);
        if(!installed && config.getSetupType() == SetupType.WITH_HADOOP) {
            po.addLog("Flume not installed on " + hostname);
            return false;
        }

        if(!installed && config.getSetupType() == SetupType.OVER_HADOOP) {
            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(Commands.make(CommandType.INSTALL)),
                    set);
            manager.getCommandRunner().runCommand(cmd);

            if(cmd.hasSucceeded()) {
                installed = true;
                po.addLog("Installed Flume");
            } else {
                po.addLog(cmd.getAllErrors());
                po.addLog("Installation failed");
                return false;
            }
        }

        if(installed) {
            config.getNodes().add(agent);

            po.addLog("Updating db...");
            try {
                manager.getPluginDao().saveInfo(FlumeConfig.PRODUCT_KEY,
                        config.getClusterName(), config);
                po.addLog("Cluster info updated");
                return true;
            } catch(DBException ex) {
                po.addLog("Failed to update cluster info: " + ex.getMessage());
                return false;
            }
        }
        return false;
    }

}
