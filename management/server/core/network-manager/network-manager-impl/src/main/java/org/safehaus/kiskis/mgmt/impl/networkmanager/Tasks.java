package org.safehaus.kiskis.mgmt.impl.networkmanager;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;

/**
 * Created by daralbaev on 03.04.14.
 */
public class Tasks {
    public static Task getCreateSshTask(List<Agent> agentList) {
        Task task = new Task();
        task.setData(TaskType.CREATE_SSH);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getCreateSSHCommand(), agent);
        }

        return task;
    }

    public static Task getReadSshTask(List<Agent> agentList) {
        Task task = new Task();
        task.setData(TaskType.READ_SSH);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getReadSSHCommand(), agent);
        }
        return task;
    }

    public static Task getWriteSshTask(List<Agent> agentList, String key) {
        Task task = new Task();
        task.setData(TaskType.WRITE_SSH);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getWriteSSHCommand(key), agent);
        }
        return task;
    }

    public static Task getConfigSshTask(List<Agent> agentList) {
        Task task = new Task();
        task.setData(TaskType.CONFIG_SSH);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getConfigSSHCommand(), agent);
        }
        return task;
    }

    public static Task getReadHostTask(List<Agent> agentList) {
        Task task = new Task();
        task.setData(TaskType.READ_HOST);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getReadHostsCommand(), agent);
        }
        return task;
    }

    public static Task getWriteHostTask(List<Agent> agentList, String hosts) {
        Task task = new Task();
        task.setData(TaskType.WRITE_HOST);
        for (Agent agent : agentList) {
            task.addRequest(Commands.getWriteHostsCommand(hosts), agent);
        }
        return task;
    }
}
