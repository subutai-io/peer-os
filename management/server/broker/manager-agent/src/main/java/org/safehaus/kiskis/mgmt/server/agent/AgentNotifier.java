/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

/**
 *
 * @author dilshat
 */
public class AgentNotifier implements Runnable {

    private static final Logger LOG = Logger.getLogger(AgentNotifier.class.getName());
    private AgentManagerInterface agentManager;
    protected volatile boolean refresh = true;
    private Queue<AgentListener> listeners;

    public AgentNotifier(AgentManagerInterface agentManager, Queue<AgentListener> listeners) {
        this.agentManager = agentManager;
        this.listeners = listeners;
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                if (refresh) {
                    refresh = false;
                    List<Agent> allFreshAgents = agentManager.getRegisteredAgents();
                    for (AgentListener listener : listeners) {
                        listener.onAgent(allFreshAgents);
                    }
                }
                Thread.sleep(500);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in AgentNotifier.run", ex);
            }
        }
    }
}
