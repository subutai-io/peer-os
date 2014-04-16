package org.safehaus.kiskis.mgmt.impl.hadoop;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;

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

    public static Task getSecondaryNameNodeTask(Config cfg) {
        Task task = new Task("Set Secondary NameNode slaves");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getAddSecondaryNamenodeCommand(cfg.getSecondaryNameNode()));
        return task;
    }

    public static Task getSetMastersTask(Config cfg) {
        Task task = new Task("Set Master Nodes");
        task.setData(TaskType.CONFIGURE);
        for (Agent agent : cfg.getAllNodes()) {
            task.addRequest(Commands.getSetMastersCommand(cfg.getNameNode(), cfg.getJobTracker(), cfg.getReplicationFactor()), agent);
        }
        return task;
    }

    public static ArrayList<Task> getSetDataNodeTask(Config cfg) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (Agent agent : cfg.getDataNodes()) {
            Task task = new Task("Set Data Nodes");
            task.setData(TaskType.CONFIGURE);
            task.addRequest(Commands.getAddSlaveCommand(agent), cfg.getNameNode());
            tasks.add(task);
        }
        return tasks;
    }

    public static ArrayList<Task> getSetTaskTrackerTask(Config cfg) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (Agent agent : cfg.getTaskTrackers()) {
            Task task = new Task("Set Task Trackers");
            task.setData(TaskType.CONFIGURE);
            task.addRequest(Commands.getAddSlaveCommand(agent), cfg.getJobTracker());
            tasks.add(task);
        }
        return tasks;
    }

    public static Task getFormatNameNodeTask(Config cfg) {
        Task task = new Task("Set format NameNode");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getFormatNameNodeCommand(), cfg.getNameNode());
        return task;
    }

    public static Task getNameNodeCommandTask(Agent agent, String command) {
        Task task = new Task("Run command on NameNode or DataNode");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getNameNodeCommand(command), agent);
        return task;
    }

    public static Task getJobTrackerCommand(Agent agent, String command) {
        Task task = new Task("Run command on JobTracker or TaskTracker");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getJobTrackerCommand(command), agent);
        return task;
    }
}
