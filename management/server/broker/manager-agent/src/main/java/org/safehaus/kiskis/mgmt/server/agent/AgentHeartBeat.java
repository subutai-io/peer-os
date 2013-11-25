/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AgentHeartBeat implements Runnable {

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
                    Set<Agent> agents = agentManager.getRegisteredAgents();
                    for (Agent agent : agents) {
                        commandSender.sendCommand((Command) CommandFactory.createRequest(
                                RequestType.HEARTBEAT_REQUEST,
                                agent.getUuid(),
                                "HEARTBEAT",
                                null, null, null, null, null, null, null, null, null, null, null));
                    }
                    System.out.println("Sending heartbeat to agents");
                }
                Thread.sleep(timeoutSec * 1000);
            } catch (Exception ex) {
                Logger.getLogger(AgentHeartBeat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
