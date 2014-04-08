package org.safehaus.kiskis.mgmt.impl.hadoop.operation.common;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 10:08 PM
 */
public class InstallHadoopOperation extends Operation {
    private final Config config;


    public InstallHadoopOperation(Config config) {
        super("Install Hadoop cluster");

        this.config = config;

        addTask(Tasks.getInstallTask(config));
        addTask(Tasks.getClearMasterTask(config));
        addTask(Tasks.getClearSlaveTask(config));
        addTask(Tasks.getSetMastersTask(config));
        addTask(Tasks.getSecondaryNameNodeTask(config));
        for (Task task : Tasks.getSetDataNodeTask(config)) {
            addTask(task);
        }
        for (Task task : Tasks.getSetTaskTrackerTask(config)) {
            addTask(task);
        }
        addTask(Tasks.getFormatNameNodeTask(config));
    }
}
