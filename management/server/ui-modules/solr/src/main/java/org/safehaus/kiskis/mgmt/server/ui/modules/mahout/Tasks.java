/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mahout;

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
        Task task = new Task("Check existence of Mahout");
        for (Agent agent : agents) {
            Request req = Commands.getCheckCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Mahout");
        for (Agent agent : agents) {
            Request req = Commands.getInstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getUninstallTask(Set<Agent> agents) {
        Task task = new Task("Uninstall Mahout");
        for (Agent agent : agents) {
            Request req = Commands.getUninstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }
}
