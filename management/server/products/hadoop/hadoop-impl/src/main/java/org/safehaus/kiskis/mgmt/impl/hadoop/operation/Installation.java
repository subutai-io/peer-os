package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.operation.common.InstallHadoopOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Installation {
    private HadoopImpl parent;
    private Config config;

    public Installation(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    private void destroyLXC(ProductOperation po, String log) {
        //destroy all lxcs also
        Set<String> lxcHostnames = new HashSet<String>();
        for (Agent lxcAgent : config.getNodes()) {
            lxcHostnames.add(lxcAgent.getHostname());
        }
        try {
            parent.getLxcManager().destroyLxcs(lxcHostnames);
            if (parent.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                po.addLogDone("Cluster info deleted from DB\nDone");
            } else {
                po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
            }
        } catch (LxcDestroyException ex) {
            po.addLogFailed(log + "\nUse LXC module to cleanup");
        }
        po.addLogFailed(log);
    }

    public UUID execute() {
        final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Installation of Hadoop");

        parent.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                config = parent.getDbManager().getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class);

                if (config == null || config.getAllNodes().isEmpty()) {
                    po.addLogFailed("Malformed configuration\nHadoop installation aborted");
                    return;
                }

                Operation installOperation = new InstallHadoopOperation(config);
                while (installOperation.hasNextTask()) {
                    Task task = installOperation.getNextTask();
                    parent.getTaskRunner().executeTask(task);

                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone(String.format("%s succeeded", task.getDescription()));
                    } else {
                        po.addLogFailed(String.format("%s failed, %s", task.getDescription(), task.getFirstError()));
                    }
                }

                if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB");
                } else {
                    destroyLXC(po, "Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }
            }
        });

        return po.getId();
    }
}
