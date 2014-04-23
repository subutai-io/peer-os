/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.oozie;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

/**
 * @author dilshat
 */
public class Tasks {

    public static Task getInstallServerTask(Agent agent) {
        Task task = new Task("Install Oozie server");
        task.setData(TaskType.INSTALL);
        task.addRequest(Commands.getInstallServerCommand(), agent);
        return task;
    }

    public static Task getUninstallServerTask(Agent agent) {
        Task task = new Task("Uninstall Oozie server");
        task.setData(TaskType.INSTALL);
        task.addRequest(Commands.getUninstallServerCommand(), agent);
        return task;
    }

    public static Task getStartServerTask(Agent agent) {
        Task task = new Task("Start Oozie server");
        task.setData(TaskType.START);
        task.addRequest(Commands.getStartServerCommand(), agent);
        return task;
    }

    public static Task getStopServerTask(Agent agent) {
        Task task = new Task("Stop Oozie server");
        task.setData(TaskType.STOP);
        task.addRequest(Commands.getStopServerCommand(), agent);
        return task;
    }

    public static Task getStatusServerTask(Agent agent) {
        Task task = new Task("Status of Oozie server");
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusServerCommand(), agent);
        return task;
    }

    public static Task getInstallClientsTask(Set<Agent> agents) {
        Task task = new Task("Install Oozie client");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallClientCommand(), agent);
        }
        return task;
    }

    public static Task getUninstallClientsTask(Set<Agent> agents) {
        Task task = new Task("Uninstall Oozie client");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getUninstallClientCommand(), agent);
        }
        return task;
    }

    public static Task getConfigureRootHostsTask(Set<Agent> agents, String ip) {
        Task task = new Task("Configure root hosts");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getConfigureRootHostsCommand(ip), agent);
        }
        return task;
    }

    public static Task getConfigureRootGroupsTask(Set<Agent> agents) {
        Task task = new Task("Configure root groups");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getConfigureRootGroupsCommand(), agent);
        }
        return task;
    }
}
