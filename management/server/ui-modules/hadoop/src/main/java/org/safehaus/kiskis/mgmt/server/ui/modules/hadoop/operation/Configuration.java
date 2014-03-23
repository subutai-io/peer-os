package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.datanode.DataNodesWindow;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * Created by daralbaev on 23.03.14.
 */
public class Configuration {
    public static void removeNode(final DataNodesWindow form, final String clusterName, final Agent agent, final boolean restart) {
        final HadoopClusterInfo cluster = HadoopDAO.getHadoopClusterInfo(clusterName);

        Task task = Tasks.getRemoveNodeCommand(cluster, agent);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    cluster.getDataNodes().remove(agent);

                    HadoopDAO.deleteHadoopClusterInfo(cluster.getClusterName());
                    HadoopDAO.saveHadoopClusterInfo(cluster);

                    if (restart) {
                        form.restartCluster();
                    } else {
                        form.getStatus();
                    }
                    uninstallDeb(agent);
                }

                return null;
            }
        });
    }

    public static void uninstallDeb(final Agent agent) {

        Task task = Tasks.getUninstallTask(agent);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });
    }
}
