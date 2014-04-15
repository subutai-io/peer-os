/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.zookeeper;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task();
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getStartTask(Set<Agent> agents) {
        Task task = new Task();
        task.setData(TaskType.START);
        for (Agent agent : agents) {
            task.addRequest(Commands.getStartCommand(), agent);
        }
        return task;
    }

    public static Task getRestartTask(Set<Agent> agents) {
        Task task = new Task();
        task.setData(TaskType.RESTART);
        for (Agent agent : agents) {
            task.addRequest(Commands.getRestartCommand(), agent);
        }
        return task;
    }

    public static Task getStopTask(Set<Agent> agents) {
        Task task = new Task();
        task.setData(TaskType.STOP);
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopCommand(), agent);
        }
        return task;
    }

    public static Task getStatusTask(Agent agent) {
        Task task = new Task();
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusCommand(), agent);
        return task;
    }

    public static Task getReadSettingsTask(Agent agent) {
        Task task = new Task();
        task.setData(TaskType.GET_SETTINGS);
        task.addRequest(Commands.getReadSettingsCommand(), agent);
        return task;
    }

//    public static Task getSetZkIdTask(Set<Agent> agents) {
//        Task task = new Task();
//        task.setData(TaskType.SET_ZK_ID);
//        int id = 0;
//        for (Agent agent : agents) {
//            task.addRequest(Commands.getSetZkIdCommand(++id), agent);
//        }
//        return task;
//    }
//
//    public static Task getUpdateCfgFileTask(Set<Agent> agents) {
//        Task task = new Task();
//        task.setData(TaskType.UPDATE_CFG_FILE);
//        for (Agent agent : agents) {
//            task.addRequest(Commands.getUpdateCfgFileCommand(agents), agent);
//        }
//        return task;
//    }
    public static Task getUpdateSettingsTask(Set<Agent> agents) {
        Task task = new Task();
        task.setData(TaskType.UPDATE_SETTINGS);
        int id = 0;
        for (Agent agent : agents) {
            task.addRequest(Commands.getUpdateSettingsCommand(agents, ++id), agent);
        }
        return task;
    }
}
