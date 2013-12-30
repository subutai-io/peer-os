/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static Label createImage(String imageName, int imageWidth, int imageHeight) {
        Label image = new Label(
                String.format("<img src='http://%s:%s/%s' />", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT, imageName));
        image.setContentMode(Label.CONTENT_XHTML);
        image.setHeight(imageWidth, Sizeable.UNITS_PIXELS);
        image.setWidth(imageHeight, Sizeable.UNITS_PIXELS);
        return image;
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
