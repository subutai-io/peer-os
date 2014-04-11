/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.presto;

import java.util.Set;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 * @author dilshat
 */
public class Tasks {

    public static Task getCheckInstalledTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getCheckInstalledCommand(), agent);
        }
        return task;
    }

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getUninstallTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getUninstallCommand(), agent);
        }
        return task;
    }

    public static Task getStartTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartCommand(), agent);
        }
        return task;
    }

    public static Task getStopTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopCommand(), agent);
        }
        return task;
    }

    public static Task getStatusTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStatusCommand(), agent);
        }
        return task;
    }

    public static Task getRestartTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getRestartCommand(), agent);
        }
        return task;
    }

    public static Task getSetCoordinatorTask(Agent coordinatorNode) {
        Task task = new Task();
        task.addRequest(Commands.getSetCoordinatorCommand(coordinatorNode), coordinatorNode);
        return task;
    }

    public static Task getSetWorkerTask(Agent coordinatorNode, Set<Agent> workers) {
        Task task = new Task();
        for (Agent agent : workers) {
            task.addRequest(Commands.getSetWorkerCommand(coordinatorNode), agent);
        }
        return task;
    }
}
