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

    public static Task getStartAllTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartAllCommand(), agent);
        }
        return task;
    }

    public static Task getStopAllTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopAllCommand(), agent);
        }
        return task;
    }

    public static Task getStatusAllTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStatusAllCommand(), agent);
        }
        return task;
    }

    public static Task getKillAllTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getKillAllCommand(), agent);
        }
        return task;
    }

    public static Task getStartMasterTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartMasterCommand(), agent);
        }
        return task;
    }

    public static Task getRestartMasterTask(Agent masterNode) {
        Task task = new Task();
        task.addRequest(Commands.getRestartMasterCommand(), masterNode);
        return task;
    }

    public static Task getStopMasterTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopMasterCommand(), agent);
        }
        return task;
    }

    public static Task getStatusMasterTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStatusMasterCommand(), agent);
        }
        return task;
    }

    public static Task getKillMasterTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getKillMasterCommand(), agent);
        }
        return task;
    }

    public static Task getStartSlaveTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartSlaveCommand(), agent);
        }
        return task;
    }

    public static Task getStopSlaveTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopSlaveCommand(), agent);
        }
        return task;
    }

    public static Task getStatusSlaveTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getStatusSlaveCommand(), agent);
        }
        return task;
    }

    public static Task getKillSlaveTask(Set<Agent> agents) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getKillSlaveCommand(), agent);
        }
        return task;
    }

    public static Task getSetMasterIPTask(Set<Agent> agents, Agent masterNode) {
        Task task = new Task();
        for (Agent agent : agents) {
            task.addRequest(Commands.getSetMasterIPCommand(masterNode), agent);
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

    public static Task getAddSlaveTask(Agent masterNode, Agent slaveNode) {
        Task task = new Task();
        task.addRequest(Commands.getAddSlaveCommand(slaveNode), masterNode);
        return task;
    }

    public static Task getAddSlavesTask(Agent masterNode, Set<Agent> slaveNodes) {
        Task task = new Task();
        task.addRequest(Commands.getAddSlavesCommand(slaveNodes), masterNode);
        return task;
    }

    public static Task getRemoveSlaveTask(Agent masterNode, Agent slaveNode) {
        Task task = new Task();
        task.addRequest(Commands.getRemoveSlaveCommand(slaveNode), masterNode);
        return task;
    }

    public static Task getClearSlavesTask(Agent masterNode) {
        Task task = new Task();
        task.addRequest(Commands.getClearSlavesCommand(), masterNode);
        return task;
    }
}
