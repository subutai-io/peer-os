package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Iterator;
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

    public InstallHandler(HiveImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        if(config != null) {
            po.addLogFailed(String.format("Cluster '%s' already exists.\nInstallation aborted",
                    config.getClusterName()));
            return;
        }

        // check server node
        if(!isNodeConnected(config.getServer().getHostname())) {
            po.addLogFailed(String.format("Server node '%s' is not connected",
                    config.getServer().getHostname()));
            return;
        }
        // check client nodes
        if(checkClientNodes(true) == 0) {
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

        // install
        Task serverTask = TaskFactory.installServer(config.getServer(), skipHive, skipDerby);
        Task clientTask = TaskFactory.installClient(config.getClients());

        po.addLog("Save cluster info");
        if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLog("Cluster info saved");

            po.addLog("Installing server...");
            serverTask = manager.getTaskRunner().executeTaskNWait(serverTask);

            if(serverTask.getTaskStatus() == TaskStatus.SUCCESS) {
                po.addLog("Server successfully installed");
                po.addLog("Installing clients...");
                clientTask = manager.getTaskRunner().executeTaskNWait(clientTask);

                if(clientTask.isCompleted()) {
                    for(Agent a : config.getClients()) {
                        res = clientTask.getResults().get(a.getUuid());
                        if(res.getExitCode() != null && res.getExitCode() == 0)
                            po.addLog("Hive successfully installed on " + a.getHostname());
                        else
                            po.addLog("Failed to install Hive on " + a.getHostname());
                    }
                } else {
                    po.addLogFailed("Failed to install client(s): "
                            + clientTask.getFirstError());
                }
            }
        } else {
            po.addLogFailed("Failed to save cluster info.\nInstallation aborted");
        }
    }

}
