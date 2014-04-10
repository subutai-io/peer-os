package org.safehaus.kiskis.mgmt.impl.hadoop.operation.common;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 10:08 PM
 */
public class InstallHadoopOperation {
    private final Config config;
    private List<Task> taskList;

    public InstallHadoopOperation(Config config) {

        System.out.println("InstallHadoopOperation started");
        this.config = config;
        taskList = new ArrayList<Task>();

        taskList.add(Tasks.getInstallTask(config));
        taskList.add(Tasks.getClearMasterTask(config));
        taskList.add(Tasks.getClearSlaveTask(config));
        taskList.add(Tasks.getSetMastersTask(config));
        taskList.add(Tasks.getSecondaryNameNodeTask(config));
        List<Task> tasks = Tasks.getSetDataNodeTask(config);
        for (Task task : tasks) {
            taskList.add(task);
        }
        tasks = Tasks.getSetTaskTrackerTask(config);
        for (Task task : tasks) {
            taskList.add(task);
        }
        taskList.add(Tasks.getFormatNameNodeTask(config));
        System.out.println("InstallHadoopOperation finished");
    }

    public List<Task> getTaskList() {
        return taskList;
    }
}
