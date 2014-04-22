/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.hbase;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Iterator;
import java.util.Set;

/**
 * @author dilshat
 */
public class Tasks {

    public static Task getInstallTask(Set<Agent> agents) {
        Task task = new Task("Install HBase");
        task.setData(TaskType.INSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getUninstallTask(Set<Agent> agents) {
        Task task = new Task("Uninstall HBase");
        task.setData(TaskType.UNINSTALL);
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getConfigMasterTask(Set<Agent> agents, Agent hadoopNameNode, Agent master) {
        Task task = new Task("Setting master");
        task.setData(TaskType.SET_MASTER);
        for (Agent agent : agents) {
            task.addRequest(Commands.setMasterCommand(hadoopNameNode.getHostname(), master.getHostname()), agent);
        }
        return task;
    }

    public static Task getConfigRegionTask(Set<Agent> agents, Set<Agent> regions) {

        StringBuilder sb = new StringBuilder();
        for (Iterator<Agent> iterator = regions.iterator(); iterator.hasNext(); ) {
            Agent next = iterator.next();
            sb.append(next.getHostname());
            sb.append(" ");
        }

        Task task = new Task("Setting region");
        task.setData(TaskType.SET_REGION);
        for (Agent agent : agents) {
            task.addRequest(Commands.setRegionCommand(sb.toString()), agent);
        }
        return task;
    }

    public static Task getConfigQuorumTask(Set<Agent> agents, Set<Agent> quorums) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Agent> iterator = agents.iterator(); iterator.hasNext(); ) {
            Agent next = iterator.next();
            sb.append(next.getHostname());
            sb.append(" ");
        }

        Task task = new Task("Setting quorum");
        task.setData(TaskType.SET_QUORUM);
        for (Agent agent : agents) {
            task.addRequest(Commands.setQuorumCommand(sb.toString()), agent);
        }
        return task;
    }

    public static Task getConfigBackupMastersTask(Set<Agent> agents, Agent backuMaster) {
        Task task = new Task("Setting backup masters");
        task.setData(TaskType.SET_BACKUP_MASTER);
        for (Agent agent : agents) {
            task.addRequest(Commands.setBackupMasterCommand(backuMaster.getHostname()), agent);
        }
        return task;
    }

    public static Task getStartTask(Agent agent) {
        Task task = new Task("Start HBase");
        task.setData(TaskType.START);
        task.addRequest(Commands.getStartCommand(), agent);
        return task;
    }

    public static Task getStopTask(Agent agent) {
        Task task = new Task("Stop HBase");
        task.setData(TaskType.STOP);
        task.addRequest(Commands.getStopCommand(), agent);
        return task;
    }

    public static Task getStatusTask(Agent agent) {
        Task task = new Task("Status of HBase");
        task.setData(TaskType.STATUS);
        task.addRequest(Commands.getStatusCommand(), agent);
        return task;
    }
}
