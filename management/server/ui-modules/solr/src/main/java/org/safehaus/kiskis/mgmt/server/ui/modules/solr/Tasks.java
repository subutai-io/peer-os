/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.solr;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getCheckTask(Set<Agent> agents) {
        Task task = new Task("Check existence of Solr");
        for (Agent agent : agents) {
            Request req = Commands.getCheckCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Solr");
        for (Agent agent : agents) {
            Request req = Commands.getInstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getUninstallTask(Set<Agent> agents) {
        Task task = new Task("Uninstall Solr");
        for (Agent agent : agents) {
            Request req = Commands.getUninstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getStartTask(Set<Agent> agents) {
        Task task = new Task("Start Solr");
        for (Agent agent : agents) {
            Request req = Commands.getStartCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getStopTask(Set<Agent> agents) {
        Task task = new Task("Stop Solr");
        for (Agent agent : agents) {
            Request req = Commands.getStopCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getStatusTask(Set<Agent> agents) {
        Task task = new Task("Status of Solr");
        for (Agent agent : agents) {
            Request req = Commands.getStatusCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }
}
