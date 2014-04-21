/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getCloneSingleLxcTask(Agent physicalAgent, String lxcHostName) {
        Task task = new Task();
        task.setData(TaskType.CLONE_LXC);
        task.addRequest(Commands.getCloneCommand(lxcHostName), physicalAgent);
        return task;
    }

    public static Task getLxcListTask(Set<Agent> physicalAgents) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_LIST);
        for (Agent physicalAgent : physicalAgents) {
            task.addRequest(Commands.getLxcListCommand(), physicalAgent);
        }

        return task;
    }

    public static Task getLxcStartTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.START_LXC);
        task.addRequest(Commands.getLxcStartCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getLxcCloneNStartTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.CLONE_N_START);
        task.addRequest(Commands.getCloneNStartCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getLxcStopTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.STOP_LXC);
        task.addRequest(Commands.getLxcStopCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getLxcInfoTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        task.addRequest(Commands.getLxcInfoCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getLxcInfoWithWaitTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        task.addRequest(Commands.getLxcInfoWithWaitCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getLxcDestroyTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.DESTROY_LXC);
        task.addRequest(Commands.getLxcDestroyCommand(lxcHostname), physicalAgent);
        return task;
    }

    public static Task getMetricsTask(Set<Agent> physicalAgents) {
        Task task = new Task();
        task.setData(TaskType.GET_METRICS);
        for (Agent physicalAgent : physicalAgents) {
            task.addRequest(Commands.getMetricsCommand(), physicalAgent);
        }
        return task;
    }
}
