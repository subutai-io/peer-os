/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
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

    public static Task getKillTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getKillCommand(), agent);
        }
        return task;
    }

    public static Task getSetMasterIPTask(Set<Agent> agents, String masterHostname) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getSetMasterIPCommand(masterHostname), agent);
        }
        return task;
    }

    public static Task getClearMasterIPTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getClearMasterIPCommand(), agent);
        }
        return task;
    }

    public static Task getAddSlaveTask(Agent masterNode, String slaveHostname) {
        Task task = new Task();
        task.addRequest(Commands.getAddSlaveCommand(slaveHostname), masterNode);
        return task;
    }

    public static Task getRemoveSlaveTask(Agent masterNode, String slaveHostname) {
        Task task = new Task();
        task.addRequest(Commands.getRemoveSlaveCommand(slaveHostname), masterNode);
        return task;
    }

    public static Task getClearSlavesTask(Agent masterNode) {
        Task task = new Task();
        task.addRequest(Commands.getClearSlavesCommand(), masterNode);
        return task;
    }
}
