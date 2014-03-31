/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.solr;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;

/**
 *
 * @author dilshat
 */
public class Tasks {

//    public static Task getCheckTask(Set<Agent> agents) {
//        Task task = new Task("Check existence of Solr");
//        task.setData(TaskType.CHECK);
//        for (Agent agent : agents) {
//            Request req = Commands.getCheckCommand();
//            req.setUuid(agent.getUuid());
//            task.addRequest(req);
//        }
//        return task;
//    }
    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Solr");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

//    public static Task getUninstallTask(Set<Agent> agents) {
//        Task task = new Task("Uninstall Solr");
//        task.setData(TaskType.UNINSTALL);
//        for (Agent agent : agents) {
//            Request req = Commands.getUninstallCommand();
//            req.setUuid(agent.getUuid());
//            task.addRequest(req);
//        }
//        return task;
//    }
    public static Task getStartTask(Agent agent) {
        Task task = new Task("Start Solr");
        task.setData(TaskType.START);
        task.addRequest(Commands.getStartCommand(), agent);
        return task;
    }

    public static Task getStopTask(Agent agent) {
        Task task = new Task("Stop Solr");
        task.setData(TaskType.STOP);
        task.addRequest(Commands.getStopCommand(), agent);
        return task;
    }

    public static Task getStatusTask(Agent agent) {
        Task task = new Task("Status of Solr");
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusCommand(), agent);
        return task;
    }
}
