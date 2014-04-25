package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandFactory;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandType;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class InstallHandler extends AbstractHandler {

    private Config config;

    public InstallHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void run() {
        if(getClusterConfig() != null) {
            po.addLogFailed("Cluster already exists: " + config.getClusterName());
            return;
        }

        if(checkNodes(config, true) == 0) {
            po.addLogFailed("No nodes are connected");
            return;
        }

        // check if already installed
        Command cmd = CommandFactory.make(manager.getCommandRunner(),
                CommandType.INSTALL, config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            Iterator<Agent> it = config.getNodes().iterator();
            while(it.hasNext()) {
                Agent a = it.next();
                AgentResult res = cmd.getResults().get(a.getUuid());
                if(isZero(res.getExitCode())) {
                    if(res.getStdOut().contains(CommandFactory.PACKAGE_NAME)) {
                        po.addLog(String.format("%s already installed on %s",
                                CommandFactory.PACKAGE_NAME, a.getHostname()));
                        it.remove();
                    }
                } else {
                    po.addLog(String.format("Failed to check installed packages on %s: %s",
                            a.getHostname(), res.getStdErr()));
                }
            }
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        if(config.getNodes().isEmpty()) {
            po.addLogFailed("No nodes for installation");
            return;
        }

        // installation
        cmd = CommandFactory.make(manager.getCommandRunner(),
                CommandType.INSTALL, config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            Iterator<Agent> it = config.getNodes().iterator();
            while(it.hasNext()) {
                Agent a = it.next();
                AgentResult res = cmd.getResults().get(a.getUuid());
                if(isZero(res.getExitCode())) {
                    po.addLog("Successfully installed on " + a.getHostname());
                } else {
                    it.remove();
                    po.addLog("Failed to install in " + a.getHostname());
                    po.addLog(res.getStdErr());
                }
            }
            if(config.getNodes().isEmpty()) {
                po.addLogFailed("Installation failed");
            } else {
                // save cluster info
                boolean b = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
                        config.getClusterName(), config);
                if(b) po.addLogDone("Cluster info successfully saved");
                else po.addLogFailed("Failed to save cluster info");
            }
        } else {
            po.addLogFailed(cmd.getAllErrors());
        }
    }

}
