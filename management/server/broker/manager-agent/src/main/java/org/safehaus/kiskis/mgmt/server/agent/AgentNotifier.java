/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import java.util.Iterator;
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
    private final AgentManagerInterface agentManager;
    protected volatile boolean refresh = true;
    private final Queue<AgentListener> listeners;

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
                    for (Iterator<AgentListener> it = listeners.iterator(); it.hasNext();) {
                        AgentListener listener = it.next();
                        if (listener != null) {
                            listener.onAgent(allFreshAgents);
                        } else {
                            it.remove();
                        }
                    }
                }
                Thread.sleep(500);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in AgentNotifier.run", ex);
            }
        }
    }
}
