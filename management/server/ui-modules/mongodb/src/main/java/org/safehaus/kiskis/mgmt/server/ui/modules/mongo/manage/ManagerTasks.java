/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class ManagerTasks {

    public static Task getCheckStatusTask(Set<Agent> agents, NodeType nodeType) {
        Task task = new Task("Check status");
        for (Agent agent : agents) {
            Command cmd;
            if (nodeType == NodeType.CONFIG_NODE) {
                cmd = Commands.getCheckConfigSrvStatusCommand(
                        String.format("%s%s", agent.getHostname(), Constants.DOMAIN));
            } else if (nodeType == NodeType.ROUTER_NODE) {
                cmd = Commands.getCheckRouterStatusCommand(
                        String.format("%s%s", agent.getHostname(), Constants.DOMAIN));
            } else {
                cmd = Commands.getCheckDataNodeStatusCommand(
                        String.format("%s%s", agent.getHostname(), Constants.DOMAIN));
            }
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }
}
