package org.safehaus.kiskis.mgmt.impl.flume;

import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class Tasks {

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Flume");
        task.setData(TaskType.INSTALL);
        for(Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getStartTask(Agent agent) {
        Task task = new Task("Start Flume");
        task.setData(TaskType.START);
        task.addRequest(Commands.getStartCommand(), agent);
        return task;
    }

    public static Task getStopTask(Agent agent) {
        Task task = new Task("Stop Flume");
        task.setData(TaskType.STOP);
        task.addRequest(Commands.getStopCommand(), agent);
        return task;
    }

    public static Task getStatusTask(Agent agent) {
        Task task = new Task("Status of Flume");
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusCommand(), agent);
        return task;
    }
}
