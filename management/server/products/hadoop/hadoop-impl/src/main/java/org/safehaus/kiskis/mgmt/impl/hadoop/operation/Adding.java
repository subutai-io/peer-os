package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.operation.common.AddNodeOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Adding {
    private HadoopImpl parent;
    private Config config;
    private String clusterName;

    public Adding(HadoopImpl parent, String clusterName) {
        this.parent = parent;
        this.clusterName = clusterName;
    }


    public UUID execute() {
        final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Adding node to Hadoop");

        parent.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                config = parent.getDbManager().getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null ||
                        Strings.isNullOrEmpty(config.getClusterName()) ||
                        Strings.isNullOrEmpty(config.getDomainName())) {
                    po.addLogFailed("Malformed configuration\nHadoop adding new node aborted");
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc container...", 1));
                    Map<Agent, Set<Agent>> lxcAgentsMap = parent.getLxcManager().createLxcs(1);
                    Agent agent = null;

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        agent = (Agent) entry.getValue();
                    }
                    po.addLog("Lxc containers created successfully\nConfiguring network...");

                    if (parent.getNetworkManager().configHostsOnAgents(config.getAllNodes(), agent, config.getDomainName()) &&
                            parent.getNetworkManager().configSshOnAgents(config.getAllNodes(), agent)) {
                        po.addLog("Cluster network configured");

                        AddNodeOperation addOperation = new AddNodeOperation(config, agent);
                        for (Task task : addOperation.getTaskList()) {
                            po.addLog((String.format("%s started...", task.getDescription())));
                            parent.getTaskRunner().executeTaskNWait(task);

                            if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                                po.addLogDone(String.format("%s succeeded", task.getDescription()));
                            } else {
                                po.addLogFailed(String.format("%s failed, %s", task.getDescription(), task.getFirstError()));
                            }
                        }

                        config.getTaskTrackers().add(agent);
                        config.getDataNodes().add(agent);

                        if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                            po.addLog("Cluster info saved to DB");
                        } else {
                            po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
                                    "Installation aborted");
                        }
                    } else {
                        po.addLogFailed("Could not configure network! Please see logs\nLXC creation aborted");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }
}
