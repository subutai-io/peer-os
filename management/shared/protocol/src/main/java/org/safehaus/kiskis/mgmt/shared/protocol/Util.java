/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isCollectionEmpty(Collection col) {
        return col == null || col.isEmpty();
    }

    public static Set retainValues(Set col1, Set col2) {
        if (col1 == null || col2 == null) {
            return null;
        } else {
            Set tmp = new HashSet(col1);
            tmp.retainAll(col2);
            return tmp;
        }
    }

    public static Task createTask(String description) {
        try {
            Task task = new Task();
            task.setTaskStatus(TaskStatus.NEW);
            task.setDescription(description);
            ServiceLocator.getService(CommandManagerInterface.class).saveTask(task);
            return task;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in createTask", e);

        }
        return null;
    }

    public static boolean saveTask(Task task, TaskStatus status) {
        try {
            task.setTaskStatus(status);
            ServiceLocator.getService(CommandManagerInterface.class).saveTask(task);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in saveTask", e);
        }
        return false;

    }

    public static String getAgentIpByMask(Agent agent, String mask) {
        if (agent != null) {
            if (agent.getListIP() != null && !agent.getListIP().isEmpty()) {
                for (String ip : agent.getListIP()) {
                    if (ip.matches(mask)) {
                        return ip;
                    }
                }
            }
            return agent.getHostname();
        }
        return null;
    }

    public static Set<Agent> filterLxcAgents(Set<Agent> agents) {
        Set<Agent> filteredAgents = new HashSet<Agent>();
        if (agents != null) {
            for (Agent agent : agents) {
                if (agent.isIsLXC()) {
                    filteredAgents.add(agent);
                }
            }
        }
        return filteredAgents;
    }
}
