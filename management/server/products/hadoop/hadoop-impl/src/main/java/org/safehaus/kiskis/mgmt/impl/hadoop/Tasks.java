package org.safehaus.kiskis.mgmt.impl.hadoop;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;

/**
 * Created by daralbaev on 05.04.14.
 */
public class Tasks {

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

    public static Task getSetMastersTask(Config cfg, Agent agent) {
        Task task = new Task("Set Master Nodes");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getSetMastersCommand(cfg.getNameNode(), cfg.getJobTracker(), cfg.getReplicationFactor()), agent);
        return task;
    }

    public static Task getExcludeNameNodeCommand(Config cfg, Agent agent) {
        Task task = new Task("Exclude NameNode from blacklist");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getExcludeDataNodeCommand(agent), cfg.getNameNode());
        return task;
    }

    public static Task getExcludeTaskTrackerCommand(Config cfg, Agent agent) {
        Task task = new Task("Exclude TaskTracker from blacklist");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getExcludeTaskTrackerCommand(agent), cfg.getJobTracker());
        return task;
    }

    public static Task getIncludeNameNodeCommand(Config cfg, Agent agent) {
        Task task = new Task("Include NameNode to blacklist");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getIncludeDataNodeCommand(agent), cfg.getNameNode());
        return task;
    }

    public static Task getIncludeTaskTrackerCommand(Config cfg, Agent agent) {
        Task task = new Task("Include TaskTracker to blacklist");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getIncludeTaskTrackerCommand(agent), cfg.getJobTracker());
        return task;
    }

    public static Task getSetDataNodeTask(Config cfg, Agent agent) {
        Task task = new Task("Set Data Nodes");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getSetDataNodeCommand(agent), cfg.getNameNode());

        return task;
    }

    public static Task getSetTaskTrackerTask(Config cfg, Agent agent) {
        Task task = new Task("Set Task Trackers");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getSetDataNodeCommand(agent), cfg.getJobTracker());

        return task;
    }

    public static Task getClearDataNodeTask(Config cfg, Agent agent) {
        Task task = new Task("Clear Data Nodes");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getRemoveSlaveCommand(agent), cfg.getNameNode());

        return task;
    }

    public static Task getClearTaskTrackerTask(Config cfg, Agent agent) {
        Task task = new Task("Clear Task Trackers");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getRemoveSlaveCommand(agent), cfg.getJobTracker());

        return task;
    }

    public static Task getRefreshNameNodeTask(Config cfg) {
        Task task = new Task("Refresh NameNode");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getRefreshNameNodeCommand(), cfg.getNameNode());
        return task;
    }

    public static Task getRefreshTaskTrackerTask(Config cfg) {
        Task task = new Task("Refresh TaskTracker");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getRefreshJobTrackerCommand(), cfg.getJobTracker());
        return task;
    }

    public static Task getStartNameNodeTask(Agent agent) {
        Task task = new Task("Start NameNode");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getStartNameNodeCommand(), agent);
        return task;
    }

    public static Task getStartTaskTrackerTask(Agent agent) {
        Task task = new Task("Start TaskTracker");
        task.setData(TaskType.CONFIGURE);
        task.addRequest(Commands.getStartTaskTrackerCommand(), agent);
        return task;
    }

    public static Task getInstallTask(Agent agent) {
        Task task = new Task("Install Hadoop");
        task.setData(TaskType.INSTALL);
        task.addRequest(Commands.getInstallCommand(), agent);
        return task;
    }
}
