/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * s
 *
 * @author dilshat
 */
public class AgentHeartBeat implements Runnable {

    private static final Logger LOG = Logger.getLogger(AgentHeartBeat.class.getName());
    private final AgentManagerInterface agentManager;
    private final CommandTransportInterface commandSender;
    private final int timeoutSec;

    public AgentHeartBeat(AgentManagerInterface agentManager, CommandTransportInterface commandSender, int timeoutSec) {
        this.commandSender = commandSender;
        this.agentManager = agentManager;
        this.timeoutSec = timeoutSec;
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                //send hearbeats
                if (commandSender != null) {
                    List<Agent> agents = agentManager.getAgentsToHeartbeat();
                    if (!agents.isEmpty()) {
                        LOG.log(Level.INFO, "Sending heartbeat to agents");
                        for (Agent agent : agents) {
                            commandSender.sendCommand((Command) CommandFactory.createRequest(
                                    RequestType.HEARTBEAT_REQUEST,
                                    agent.getUuid(),
                                    "HEARTBEAT",
                                    null, null, null, null, null, null, null, null, null, null, null, null));
                        }
                    }
                }
                Thread.sleep(timeoutSec * 1000);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in AgentHeartBeat.run", ex);
            }
        }
    }
}
