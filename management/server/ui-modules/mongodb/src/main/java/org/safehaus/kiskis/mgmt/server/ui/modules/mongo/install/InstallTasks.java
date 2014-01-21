/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import java.util.Iterator;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class InstallTasks {

    public static Task getKillRunningMongoTask(Set<Agent> agents) {
        Task task = new Task("Kill running mongo");
        for (Agent agent : agents) {
            Command cmd = Commands.getKillAllCommand();
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
            Command cmd = Commands.getUninstallCommand();
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
            Command cmd = Commands.getCleanCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getAptGetUpdateTask(Set<Agent> agents) {
        Task task = new Task("Apt-get update");
        for (Agent agent : agents) {
            Command cmd = Commands.getAptGetUpdateCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getInstallMongoTask(Set<Agent> agents) {
        Task task = new Task("Install mongo");
        for (Agent agent : agents) {
            Command cmd = Commands.getInstallCommand2();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getStopMongoTask(Set<Agent> agents) {
        Task task = new Task("Stop mongo");
        for (Agent agent : agents) {
            Command cmd = Commands.getStopNodeCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        //ignore exit code
//        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getRegisterIpsTask(Set<Agent> agents) {
        Task task = new Task("Register nodes's IP-Host with other nodes");
        for (Agent agent : agents) {
            StringBuilder cleanHosts = new StringBuilder();
            StringBuilder appendHosts = new StringBuilder();
            for (Agent otherAgent : agents) {
                if (agent != otherAgent) {
                    String ip = Util.getAgentIpByMask(otherAgent, Common.IP_MASK);
                    String hostname = otherAgent.getHostname();
                    cleanHosts.append(ip).append("|").append(hostname).append("|");
                    appendHosts.append("if ! /bin/grep -q '").
                            append(ip).
                            append(" ").append(hostname).append(Constants.DOMAIN).
                            append("' '/etc/hosts'; then /bin/echo '").
                            append(ip).
                            append(" ").append(hostname).append(Constants.DOMAIN).
                            append("' >> '/etc/hosts'; fi ;");
                }
            }
            if (cleanHosts.length() > 0) {
                //drop pipe | symbol
                cleanHosts.setLength(cleanHosts.length() - 1);
                cleanHosts.insert(0, "egrep -v '");
                cleanHosts.append("' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;");
                appendHosts.insert(0, cleanHosts);
            }
            Command cmd = Commands.getAddNodesIpHostToOtherNodesCommand(appendHosts.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getSetReplicaSetNameTask(String replicaSetName, Set<Agent> dataNodes) {
        Task task = new Task(String.format("Set replica set name to %s", replicaSetName));
        for (Agent agent : dataNodes) {
            Command cmd = Commands.getSetReplicaSetNameCommand(replicaSetName);
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        return task;
    }

    public static Task getStartConfigServersTask(Set<Agent> configServers) {
        Task task = new Task("Start config servers");
        for (Agent agent : configServers) {
            Command cmd = Commands.getStartConfigServerCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        task.setData(TaskType.START_CONFIG_SERVERS);
        return task;
    }

    public static Task getStartRoutersTask(Set<Agent> routers, Set<Agent> configServers) {
        Task task = new Task("Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : configServers) {
            configServersArg.append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                    append(":").append(Constants.CONFIG_SRV_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : routers) {
            Command cmd = Commands.getStartRouterCommand(configServersArg.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        task.setData(TaskType.START_ROUTERS);
        return task;
    }

    public static Task getStartReplicaSetTask(Set<Agent> dataNodes) {
        Task task = new Task("Start replica set");
        for (Agent agent : dataNodes) {
            Command cmd = Commands.getStartNodeCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            task.addCommand(cmd);
        }
        task.setData(TaskType.START_REPLICA_SET);
        return task;
    }

    public static Task getRegisterSecondaryNodesWithPrimaryTask(Set<Agent> dataNodes) {
        Task task = new Task("Register secondary nodes with primary");
        StringBuilder secondaryStr = new StringBuilder();
        Iterator<Agent> it = dataNodes.iterator();
        Agent primaryNodeAgent = it.next();
        while (it.hasNext()) {
            Agent secondaryNodeAgent = it.next();
            secondaryStr.append("rs.add('").
                    append(secondaryNodeAgent.getHostname()).append(Constants.DOMAIN).
                    append(":").append(Constants.DATA_NODE_PORT).append("');");
        }
        Command cmd = Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString());
        cmd.getRequest().setUuid(primaryNodeAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getRegisterSecondaryNodeWithPrimaryTask(Agent primaryNodeAgent, Agent secondaryNodeAgent) {
        Task task = new Task("Register secondary node with primary");
        StringBuilder secondaryStr = new StringBuilder();
        secondaryStr.append("rs.add('").
                append(secondaryNodeAgent.getHostname()).append(Constants.DOMAIN).
                append(":").append(Constants.DATA_NODE_PORT).append("');");
        Command cmd = Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString());
        cmd.getRequest().setUuid(primaryNodeAgent.getUuid());
        task.addCommand(cmd);
        return task;
    }

    public static Task getRegisterReplicaSetAsShardWithRouter(String replicaSetName, Agent router, Set<Agent> dataNodes) {
        Task task = new Task("Register replica set as shard with router");
        StringBuilder shard = new StringBuilder();
        for (Agent agent : dataNodes) {
            shard.append("sh.addShard('").append(replicaSetName).
                    append("/").append(agent.getHostname()).append(Constants.DOMAIN).
                    append(":").append(Constants.DATA_NODE_PORT).append("');");
        }
        Command cmd = Commands.getRegisterShardsWithRouterCommand(
                shard.toString());
        cmd.getRequest().setUuid(router.getUuid());
        task.addCommand(cmd);
        return task;
    }
}
