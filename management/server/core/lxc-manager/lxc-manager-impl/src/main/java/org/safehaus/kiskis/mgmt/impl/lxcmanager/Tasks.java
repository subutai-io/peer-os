/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getCloneSingleLxcTask(Agent physicalAgent, String lxcHostName) {
        Task task = new Task();
        Request cmd = Commands.getCloneCommand();
        cmd.setUuid(physicalAgent.getUuid());
        cmd.setProgram(cmd.getProgram() + lxcHostName);
        task.addRequest(cmd);
        return task;
    }

    public static Task getLxcListTask(Set<Agent> physicalAgents) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_LIST);
        for (Agent physAgent : physicalAgents) {
            Request cmd = Commands.getLxcListCommand();
            cmd.setUuid(physAgent.getUuid());
            task.addRequest(cmd);
        }

        return task;
    }

    public static Task getLxcStartTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.START_LXC);
        Request cmd = Commands.getLxcStartCommand(lxcHostname);
        cmd.setUuid(physicalAgent.getUuid());
        task.addRequest(cmd);
        return task;
    }

    public static Task getLxcStopTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.STOP_LXC);
        Request cmd = Commands.getLxcStopCommand(lxcHostname);
        cmd.setUuid(physicalAgent.getUuid());
        task.addRequest(cmd);
        return task;
    }

    public static Task getLxcInfoTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        Request cmd = Commands.getLxcInfoCommand(lxcHostname);
        cmd.setUuid(physicalAgent.getUuid());
        task.addRequest(cmd);
        return task;
    }

    public static Task getLxcInfoWithWaitTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        Request cmd = Commands.getLxcInfoWithWaitCommand(lxcHostname);
        cmd.setUuid(physicalAgent.getUuid());
        task.addRequest(cmd);
        return task;
    }

    public static Task getLxcDestroyTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.DESTROY_LXC);
        Request cmd = Commands.getLxcDestroyCommand(lxcHostname);
        cmd.setUuid(physicalAgent.getUuid());
        task.addRequest(cmd);
        return task;
    }

    public static Task getMetricsTask(Set<Agent> physicalAgents) {
        Task task = new Task();
        task.setData(TaskType.GET_METRICS);
        for (Agent physicalAgent : physicalAgents) {
            Request cmd = Commands.getMetricsCommand();
            cmd.setUuid(physicalAgent.getUuid());
            task.addRequest(cmd);
        }
        return task;
    }
}
