/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mahout;

import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getCheckInstalledTask(Set<Agent> agents) {
        Task task = new Task("Check existence of ksks packages");
        for (Agent agent : agents) {
            task.addRequest(Commands.getCheckInstalledCommand(), agent);
        }
        return task;
    }

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install Mahout");
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getUninstallTask(Set<Agent> agents) {
        Task task = new Task("Uninstall Mahout");
        for (Agent agent : agents) {
            task.addRequest(Commands.getUninstallCommand(), agent);
        }
        return task;
    }
}
