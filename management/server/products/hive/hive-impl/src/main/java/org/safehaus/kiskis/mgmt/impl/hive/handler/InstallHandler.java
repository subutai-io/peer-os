package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.impl.hive.TaskFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class InstallHandler extends BaseHandler {

    private Config config;

    public InstallHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void run() {
        if(getClusterConfig() != null) {
            po.addLogFailed(String.format("Cluster '%s' already exists",
                    clusterName));
            return;
        }

        // check server node
        if(!isNodeConnected(config.getServer().getHostname())) {
            po.addLogFailed(String.format("Server node '%s' is not connected",
                    config.getServer().getHostname()));
            return;
        }
        // check client nodes
        if(checkClientNodes(config, true) == 0) {
            po.addLogFailed("No nodes eligible for installation. Operation aborted");
            return;
        }

        po.addLog("Check installed packages...");
        // server packages
        Task checkTask = TaskFactory.checkPackages(config.getServer());
        manager.getTaskRunner().executeTaskNWait(checkTask);
        if(!checkTask.isCompleted()) {
            po.addLogFailed("Failed to check installed packages for server node");
            return;
        }
        Result res = checkTask.getResults().get(config.getServer().getUuid());
        boolean skipHive = res.getStdOut().contains(Product.HIVE.getPackageName());
        boolean skipDerby = res.getStdOut().contains(Product.DERBY.getPackageName());

        // check clients
        checkTask = TaskFactory.checkPackages(config.getClients());
        checkTask = manager.getTaskRunner().executeTaskNWait(checkTask);
        if(!checkTask.isCompleted()) {
            po.addLogFailed("Failed to check installed packages");
            return;
        }
        Iterator<Agent> it = config.getClients().iterator();
        while(it.hasNext()) {
            Agent a = it.next();
            res = checkTask.getResults().get(a.getUuid());
            if(res.getStdOut().contains(Product.HIVE.getPackageName())) {
                po.addLog(String.format("Node '%s' has already Hive installed.\nOmitting from installation",
                        a.getHostname()));
                it.remove();
            }
        }
        if(config.getClients().isEmpty()) {
            po.addLogFailed("No client nodes eligible for installation. Operation aborted");
            return;
        }

        // save cluster info and install
        po.addLog("Save cluster info");
        if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLog("Cluster info saved");

            po.addLog("Installing server...");
            List<Task> serverTasks = TaskFactory.installServer(config.getServer(),
                    skipHive, skipDerby);
            for(Task t : serverTasks) {
                manager.getTaskRunner().executeTaskNWait(t);
                po.addLog(t.getDescription() + " " + t.getTaskStatus());
                if(t.getTaskStatus() != TaskStatus.SUCCESS) {
                    Result r = t.getResults().get(config.getServer().getUuid());
                    po.addLogFailed(r.getStdErr());
                    return;
                }
            }
            po.addLog("Server successfully installed");

            po.addLog("Installing clients...");
            Task clientTask = TaskFactory.installClient(config.getClients());
            manager.getTaskRunner().executeTaskNWait(clientTask);

            if(clientTask.isCompleted()) {
                List<Agent> readyClients = new ArrayList<Agent>();
                for(Agent a : config.getClients()) {
                    res = clientTask.getResults().get(a.getUuid());
                    if(isZero(res.getExitCode())) {
                        readyClients.add(a);
                        po.addLog("Hive successfully installed on " + a.getHostname());
                    } else {
                        po.addLog("Failed to install Hive on " + a.getHostname());
                    }
                }
                if(readyClients.size() > 0) {
                    Task configTask = TaskFactory.configureClient(readyClients);
                    manager.getTaskRunner().executeTaskNWait(configTask);
                    for(Agent a : readyClients) {
                        res = configTask.getResults().get(a.getUuid());
                        if(isZero(res.getExitCode()))
                            po.addLog(String.format("Client node '%s' successfully configured",
                                    a.getHostname()));
                        else
                            po.addLog(String.format("Failed to configure client node '%s': %s",
                                    a.getHostname(), res.getStdErr()));
                    }
                }
            } else {
                po.addLogFailed("Failed to install client(s): "
                        + clientTask.getFirstError());
            }
        } else {
            po.addLogFailed("Failed to save cluster info.\nInstallation aborted");
        }
    }

}
