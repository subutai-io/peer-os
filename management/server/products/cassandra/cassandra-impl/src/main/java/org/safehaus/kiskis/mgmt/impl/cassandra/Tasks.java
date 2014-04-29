/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.cassandra;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Iterator;
import java.util.Set;

/**
 * @author dilshat
 */
public class Tasks {

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Cassandra");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getStartAllNodesTask(Set<Agent> agents) {
        Task task = new Task("Start all nodes");
        task.setData(TaskType.START_ALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartCommand(), agent);
        }
        return task;
    }

    public static Task getStartTask(Agent agent) {
        Task task = new Task("Start Cassandra");
        task.setData(TaskType.START);
        task.addRequest(Commands.getStartCommand(), agent);
        return task;
    }

    public static Task getStopTask(Agent agent) {
        Task task = new Task("Stop Cassandra");
        task.setData(TaskType.STOP);
        task.addRequest(Commands.getStopCommand(), agent);
        return task;
    }

    public static Task getStatusTask(Agent agent) {
        Task task = new Task("Status of Cassandra");
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusCommand(), agent);
        return task;
    }

    public static Task getCheckAllNodesTask(Set<Agent> agents) {
        Task task = new Task("Check Cassandra");
        task.setData(TaskType.CHECK_ALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getStatusCommand(), agent);
        }
        return task;
    }

    public static Task getStopAllNodesTask(Set<Agent> agents) {
        Task task = new Task("Stop Cassandra");
        task.setData(TaskType.STOP_ALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopCommand(), agent);
        }
        return task;
    }

    public static Task configureCassandra(Set<Agent> agents, String param) {
        Task task = new Task("Configure Cassandra");
        task.setData(TaskType.CONFIGURE);
        for (Agent agent : agents) {
            task.addRequest(Commands.getConfigureCommand(param), agent);
        }
        return task;
    }

    public static Task getUpdateAptTask(Set<Agent> nodes) {
        Task task = new Task("Update APT");
        task.setData(TaskType.APTUPDATE);
        for (Agent agent : nodes) {
            task.addRequest(Commands.getUpdateAptCommand(), agent);
        }
        return task;
    }
}
