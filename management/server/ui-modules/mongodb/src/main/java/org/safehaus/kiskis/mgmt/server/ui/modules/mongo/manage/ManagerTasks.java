/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class ManagerTasks {

    public static Task getCheckStatusTask(Set<Agent> agents, NodeType nodeType) {
        Task task = new Task("Check status");
        for (Agent agent : agents) {
            CommandImpl cmd;
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

    public static Task getStopNodeTask(Set<Agent> agents) {
        Task task = new Task("Stop mongo");
        for (Agent agent : agents) {
            CommandImpl cmd = Commands.getStopNodeCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getStartDataNodeTask(Set<Agent> dataNodes) {
        Task task = new Task("Start data node");
        for (Agent agent : dataNodes) {
            CommandImpl cmd = Commands.getStartNodeCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getStartConfigSrvTask(Set<Agent> configServers) {
        Task task = new Task("Start config server");
        for (Agent agent : configServers) {
            CommandImpl cmd = Commands.getStartConfigServerCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getStartRouterTask(Set<Agent> routers, Set<Agent> configServers) {
        StringBuilder configServersArg = new StringBuilder();
        for (Agent cfgSrvAgent : configServers) {
            configServersArg.append(cfgSrvAgent.getHostname()).append(Constants.DOMAIN).
                    append(":").append(Constants.CONFIG_SRV_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        Task task = new Task("Start router");
        for (Agent agent : routers) {
            CommandImpl cmd = Commands.getStartRouterCommand(configServersArg.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        task.setData(TaskType.START_ROUTERS);
        return task;
    }

    public static Task getKillRunningMongoTask(Set<Agent> agents) {
        Task task = new Task("Kill running mongo");
        for (Agent agent : agents) {
            CommandImpl cmd = Commands.getKillAllCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getUninstallMongoTask(Set<Agent> agents) {
        Task task = new Task("Uninstall existing Mongo");
        for (Agent agent : agents) {
            CommandImpl cmd = Commands.getUninstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getCleanMongoDataTask(Set<Agent> agents) {
        Task task = new Task("Clean previous Mongo data");
        for (Agent agent : agents) {
            CommandImpl cmd = Commands.getCleanCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getFindPrimaryNodeTask(Agent secondaryNode) {
        Task task = new Task("Find primary node");
        CommandImpl cmd = Commands.getFindPrimaryNodeCommand();
        cmd.getRequest().setUuid(secondaryNode.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getUnregisterSecondaryFromPrimaryTask(Agent primaryNode, Agent secondaryNode) {
        Task task = new Task("Unregister secondary node from primary");
        CommandImpl cmd = Commands.getUnregisterSecondaryNodeFromPrimaryCommand(
                String.format("%s%s", secondaryNode.getHostname(), Constants.DOMAIN));
        cmd.getRequest().setUuid(primaryNode.getUuid());
        task.addCommand(cmd);
        task.setIgnoreExitCode(true);
        return task;
    }

}
