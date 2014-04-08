package org.safehaus.kiskis.mgmt.impl.hadoop;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 * Created by daralbaev on 05.04.14.
 */
public class Tasks {

    public static Task getInstallTask(Config cfg) {
        Task task = new Task("Install Hadoop");
        task.setData(TaskType.INSTALL);
        for (Agent agent : cfg.getAllNodes()) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getClearMasterTask(Config cfg) {
        Task task = new Task("Clear SecondaryNameNode");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getClearMastersCommand(), cfg.getNameNode());
        return task;
    }

    public static Task getClearSlaveTask(Config cfg) {
        Task task = new Task("Clear slaves");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getClearSlavesCommand(), cfg.getNameNode());
        task.addRequest(Commands.getClearSlavesCommand(), cfg.getJobTracker());
        return task;
    }
}
