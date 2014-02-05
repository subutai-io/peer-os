/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getCloneTask(Set<Agent> physicalAgents, String productName, double lxcCount) {
        Task task = new Task();
        task.setData(TaskType.CLONE_LXC);
        for (Agent physAgent : physicalAgents) {
            StringBuilder lxcHost = new StringBuilder(physAgent.getHostname());
            lxcHost.append(Common.PARENT_CHILD_LXC_SEPARATOR).append(productName);
            for (int i = 1; i <= lxcCount; i++) {
                Request cmd = Commands.getCloneCommand();
                cmd.setUuid(physAgent.getUuid());
                String lxcHostFull = lxcHost.toString() + i;
                cmd.getArgs().set(cmd.getArgs().size() - 1, lxcHostFull);
                task.addRequest(cmd);
            }
        }
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
}
