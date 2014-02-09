/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
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
                Command cmd = Commands.getCloneCommand();
                cmd.getRequest().setUuid(physAgent.getUuid());
                String lxcHostFull = lxcHost.toString() + i;
                cmd.getRequest().getArgs().set(cmd.getRequest().getArgs().size() - 1, lxcHostFull);
                task.addCommand(cmd);
            }
        }
        return task;
    }

    public static Task getLxcListTask(Set<Agent> physicalAgents) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_LIST);
        for (Agent physAgent : physicalAgents) {
            Command cmd = Commands.getLxcListCommand();
            cmd.getRequest().setUuid(physAgent.getUuid());
            task.addCommand(cmd);
        }

        return task;
    }

    public static Task getLxcStartTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.START_LXC);
        Command cmd = Commands.getLxcStartCommand(lxcHostname);
        cmd.getRequest().setUuid(physicalAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getLxcStopTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.STOP_LXC);
        Command cmd = Commands.getLxcStopCommand(lxcHostname);
        cmd.getRequest().setUuid(physicalAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getLxcInfoTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        Command cmd = Commands.getLxcInfoCommand(lxcHostname);
        cmd.getRequest().setUuid(physicalAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getLxcInfoWithWaitTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.GET_LXC_INFO);
        Command cmd = Commands.getLxcInfoWithWaitCommand(lxcHostname);
        cmd.getRequest().setUuid(physicalAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getLxcDestroyTask(Agent physicalAgent, String lxcHostname) {
        Task task = new Task();
        task.setData(TaskType.DESTROY_LXC);
        Command cmd = Commands.getLxcDestroyCommand(lxcHostname);
        cmd.getRequest().setUuid(physicalAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }
}
