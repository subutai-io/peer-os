package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.operation.common.InstallHadoopOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.*;

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
        for (Agent lxcAgent : config.getAllNodes()) {
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

    private void setNodes(HashSet<Agent> agents) {
        if (!agents.isEmpty() && agents.size() > 3) {
            Iterator<Agent> it = agents.iterator();
            int index = 0;
            while (it.hasNext()) {
                Agent agent = it.next();
                if (index == 0) {
                    config.setNameNode(agent);
                } else if (index == 1) {
                    config.setJobTracker(agent);
                } else if (index == 2) {
                    config.setSecondaryNameNode(agent);
                } else {
                    config.getDataNodes().add(agent);
                    config.getTaskTrackers().add(agent);
                }
            }
        }
    }

    public UUID execute() {
        final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Installation of Hadoop");

        parent.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Executor started to run");
                System.out.println(config);

                if (config == null ||
                        Strings.isNullOrEmpty(config.getClusterName()) ||
                        Strings.isNullOrEmpty(config.getDomainName())) {
                    po.addLogFailed("Malformed configuration\nHadoop installation aborted");
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc containers...", config.getCountOfSlaveNodes() + 3));
                    Map<Agent, Set<Agent>> lxcAgentsMap = parent.getLxcManager().createLxcs(config.getCountOfSlaveNodes() + 3);
                    HashSet<Agent> agents = new HashSet<Agent>();

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        agents.addAll(entry.getValue());
                    }
                    setNodes(agents);
                    po.addLog("Lxc containers created successfully\nConfiguring network...");

                    if (parent.getNetworkManager().configHostsOnAgents(config.getAllNodes(), config.getDomainName()) &&
                            parent.getNetworkManager().configSshOnAgents(config.getAllNodes())) {
                        po.addLog("Cluster network configured");

                        Operation installOperation = new InstallHadoopOperation(config);
                        while (installOperation.hasNextTask()) {
                            Task task = installOperation.getNextTask();
                            po.addLog((String.format("%s started...", task.getDescription())));
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
                    } else {
                        destroyLXC(po, "Could not configure network! Please see logs\nLXC creation aborted");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }
}
