/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
public class AgentHeartBeat implements Runnable {

    private static final Logger LOG = Logger.getLogger(AgentHeartBeat.class.getName());
    private AgentManagerInterface agentManager;
    private CommandTransportInterface commandSender;
    private int timeoutSec;

    public AgentHeartBeat(AgentManagerInterface agentManager, CommandTransportInterface commandSender, int timeoutSec) {
        this.commandSender = commandSender;
        this.agentManager = agentManager;
        this.timeoutSec = timeoutSec;
    }

    public void run() {
        while (true) {
            try {
                //send hearbeats
                if (commandSender != null) {
                    List<Agent> agents = agentManager.getAgentsToHeartbeat();
                    if (!agents.isEmpty()) {
                        System.out.println("Sending heartbeat to agents");
                        for (Agent agent : agents) {
                            commandSender.sendCommand((Command) CommandFactory.createRequest(
                                    RequestType.HEARTBEAT_REQUEST,
                                    agent.getUuid(),
                                    "HEARTBEAT",
                                    null, null, null, null, null, null, null, null, null, null, null));
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
