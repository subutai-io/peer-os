/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb.common;

import java.util.Iterator;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getInstallMongoTask(Set<Agent> agents) {
        Task task = new Task("Install mongo");
        for (Agent agent : agents) {
            task.addRequest(Commands.getInstallCommand(), agent);
        }
        return task;
    }

    public static Task getStopMongoTask(Set<Agent> agents) {
        Task task = new Task("Stop mongo");
        for (Agent agent : agents) {
            task.addRequest(Commands.getStopNodeCommand(), agent);
        }
        //ignore exit code
//        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getRegisterIpsTask(Set<Agent> agents, Config cfg) {
        Task task = new Task("Register nodes's IP-Host with other nodes");
        for (Agent agent : agents) {
            StringBuilder cleanHosts = new StringBuilder("localhost|127.0.0.1|");
            StringBuilder appendHosts = new StringBuilder();
            for (Agent otherAgent : agents) {
                if (agent != otherAgent) {
                    String ip = Util.getAgentIpByMask(otherAgent, Common.IP_MASK);
                    String hostname = otherAgent.getHostname();
                    cleanHosts.append(ip).append("|").append(hostname).append("|");
                    appendHosts.append("/bin/echo '").
                            append(ip).append(" ").
                            append(hostname).append(".").append(cfg.getDomainName()).
                            append(" ").append(hostname).
                            append("' >> '/etc/hosts'; ");
                }
            }
            if (cleanHosts.length() > 0) {
                //drop pipe | symbol
                cleanHosts.setLength(cleanHosts.length() - 1);
                cleanHosts.insert(0, "egrep -v '");
                cleanHosts.append("' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;");
                appendHosts.insert(0, cleanHosts);
            }
            appendHosts.append("/bin/echo '127.0.0.1 localhost ").append(agent.getHostname()).append("' >> '/etc/hosts';");
            task.addRequest(
                    Commands.getAddNodesIpHostToOtherNodesCommand(appendHosts.toString()), agent);
        }
        return task;
    }

    public static Task getSetReplicaSetNameTask(String replicaSetName, Set<Agent> dataNodes) {
        Task task = new Task(String.format("Set replica set name to %s", replicaSetName));
        for (Agent agent : dataNodes) {
            task.addRequest(Commands.getSetReplicaSetNameCommand(replicaSetName), agent);
        }
        return task;
    }

    public static Task getStartConfigServersTask(Set<Agent> configServers, Config cfg) {
        Task task = new Task("Start config servers");
        for (Agent agent : configServers) {
            task.addRequest(Commands.getStartConfigServerCommand(cfg), agent);
        }
        task.setData(TaskType.START_CONFIG_SERVERS);
        return task;
    }

    public static Task getStartRoutersTask(Set<Agent> routers, Set<Agent> configServers, Config cfg) {
        Task task = new Task("Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : configServers) {
            configServersArg.append(agent.getHostname()).append(".").append(cfg.getDomainName()).
                    append(":").append(cfg.getCfgSrvPort()).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : routers) {
            task.addRequest(
                    Commands.getStartRouterCommand(configServersArg.toString(), cfg), agent);
        }
        task.setData(TaskType.START_ROUTERS);
        return task;
    }

    public static Task getStartReplicaSetTask(Set<Agent> dataNodes, Config cfg) {
        Task task = new Task("Start replica set");
        for (Agent agent : dataNodes) {
            task.addRequest(Commands.getStartNodeCommand(cfg), agent);
        }
        task.setData(TaskType.START_REPLICA_SET);
        return task;
    }

    public static Task getRegisterSecondaryNodesWithPrimaryTask(Config cfg) {
        Task task = new Task("Register secondary nodes with primary");
        StringBuilder secondaryStr = new StringBuilder();
        Iterator<Agent> it = cfg.getDataNodes().iterator();
        Agent primaryNodeAgent = it.next();
        while (it.hasNext()) {
            Agent secondaryNodeAgent = it.next();
            secondaryStr.append("rs.add('").
                    append(secondaryNodeAgent.getHostname()).append(".").append(cfg.getDomainName()).
                    append(":").append(cfg.getDataNodePort()).append("');");
        }
        task.addRequest(
                Commands.getRegisterSecondaryNodesWithPrimaryCommand(
                        secondaryStr.toString(), cfg), primaryNodeAgent);
        return task;
    }

    public static Task getRegisterSecondaryNodeWithPrimaryTask(Agent primaryNodeAgent, Agent secondaryNodeAgent, Config cfg) {
        Task task = new Task("Register secondary node with primary");
        StringBuilder secondaryStr = new StringBuilder();
        secondaryStr.append("rs.add('").
                append(secondaryNodeAgent.getHostname()).append(".").append(cfg.getDomainName()).
                append(":").append(cfg.getDataNodePort()).append("');");
        task.addRequest(
                Commands.getRegisterSecondaryNodesWithPrimaryCommand(
                        secondaryStr.toString(), cfg), primaryNodeAgent);
        return task;
    }

    public static Task getRegisterReplicaSetAsShardWithRouter(Config cfg) {
        Task task = new Task("Register replica set as shard with router");
        StringBuilder shard = new StringBuilder();
        for (Agent agent : cfg.getDataNodes()) {
            shard.append("sh.addShard('").append(cfg.getReplicaSetName()).
                    append("/").append(agent.getHostname()).append(".").append(cfg.getDomainName()).
                    append(":").append(cfg.getDataNodePort()).append("');");
        }
        task.addRequest(
                Commands.getRegisterShardsWithRouterCommand(
                        shard.toString(), cfg), cfg.getRouterServers().iterator().next());
        return task;
    }

    public static Task getUnregisterSecondaryFromPrimaryTask(Agent primaryNode, Agent secondaryNode, Config cfg) {
        Task task = new Task("Unregister secondary node from primary");
        Request req = Commands.getUnregisterSecondaryNodeFromPrimaryCommand(
                String.format("%s.%s", secondaryNode.getHostname(), cfg.getDomainName()), cfg);
        task.addRequest(req, primaryNode);
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getCheckStatusTask(Set<Agent> agents, NodeType nodeType, Config cfg) {
        Task task = new Task("Check status");
        for (Agent agent : agents) {
            Request req;
            if (nodeType == NodeType.CONFIG_NODE) {
                req = Commands.getCheckConfigSrvStatusCommand(
                        String.format("%s.%s", agent.getHostname(), cfg.getDomainName()), cfg);
            } else if (nodeType == NodeType.ROUTER_NODE) {
                req = Commands.getCheckRouterStatusCommand(
                        String.format("%s.%s", agent.getHostname(), cfg.getDomainName()), cfg);
            } else {
                req = Commands.getCheckDataNodeStatusCommand(
                        String.format("%s.%s", agent.getHostname(), cfg.getDomainName()), cfg);
            }
            task.addRequest(req, agent);
        }
        return task;
    }

    public static Task getFindPrimaryNodeTask(Agent secondaryNode, Config cfg) {
        Task task = new Task("Find primary node");
        task.addRequest(Commands.getFindPrimaryNodeCommand(cfg), secondaryNode);
        task.setData(TaskType.FIND_PRIMARY_NODE);
        return task;
    }
}
