package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClusterForm;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 * Created by daralbaev on 3/18/14.
 */
public class Deletion {

    public static void startDeletion(final String clusterName, final ClusterForm form) {
        form.setVisible(true);

        HadoopClusterInfo cluster = HadoopDAO.getHadoopClusterInfo(clusterName);
        Task task = Tasks.removeClusterTask(cluster);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    HadoopDAO.deleteHadoopClusterInfo(clusterName);
                    form.setVisible(false);
                }

                return null;
            }
        });
    }
}
