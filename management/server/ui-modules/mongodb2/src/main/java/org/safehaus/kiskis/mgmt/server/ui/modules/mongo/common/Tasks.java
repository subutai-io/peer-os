/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common;

import java.util.Iterator;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Tasks {

    public static Task getKillRunningMongoTask(Set<Agent> agents) {
        Task task = new Task("Kill running mongo");
        for (Agent agent : agents) {
            Request req = Commands.getKillAllCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getUninstallMongoTask(Set<Agent> agents) {
        Task task = new Task("Uninstall existing Mongo");
        for (Agent agent : agents) {
            Request req = Commands.getUninstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getCleanMongoDataTask(Set<Agent> agents) {
        Task task = new Task("Clean previous Mongo data");
        for (Agent agent : agents) {
            Request req = Commands.getCleanCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        //ignore exit code
        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getAptGetUpdateTask(Set<Agent> agents) {
        Task task = new Task("Apt-get update");
        for (Agent agent : agents) {
            Request req = Commands.getAptGetUpdateCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getInstallMongoTask(Set<Agent> agents) {
        Task task = new Task("Install mongo");
        for (Agent agent : agents) {
            Request req = Commands.getInstallCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getStopMongoTask(Set<Agent> agents) {
        Task task = new Task("Stop mongo");
        for (Agent agent : agents) {
            Request req = Commands.getStopNodeCommand();
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        //ignore exit code
//        task.setIgnoreExitCode(true);
        return task;
    }

    public static Task getRegisterIpsTask(Set<Agent> agents, Config cfg) {
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
                            append(" ").append(hostname).append(".").append(cfg.getDomainName()).
                            append("' '/etc/hosts'; then /bin/echo '").
                            append(ip).
                            append(" ").append(hostname).append(".").append(cfg.getDomainName()).
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
            Request req = Commands.getAddNodesIpHostToOtherNodesCommand(appendHosts.toString());
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getSetReplicaSetNameTask(String replicaSetName, Set<Agent> dataNodes) {
        Task task = new Task(String.format("Set replica set name to %s", replicaSetName));
        for (Agent agent : dataNodes) {
            Request req = Commands.getSetReplicaSetNameCommand(replicaSetName);
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getStartConfigServersTask(Set<Agent> configServers, Config cfg) {
        Task task = new Task("Start config servers");
        for (Agent agent : configServers) {
            Request req = Commands.getStartConfigServerCommand(cfg);
            req.setUuid(agent.getUuid());
            task.addRequest(req);
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
            Request req = Commands.getStartRouterCommand(configServersArg.toString(), cfg);
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        task.setData(TaskType.START_ROUTERS);
        return task;
    }

    public static Task getStartRoutersTask2(Set<Agent> routers, Set<Agent> configServers, Config cfg) {
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
            Request req = Commands.getStartRouterCommand2(configServersArg.toString(), cfg);
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        task.setData(TaskType.START_ROUTERS);
        return task;
    }

    public static Task getStartReplicaSetTask(Set<Agent> dataNodes, Config cfg) {
        Task task = new Task("Start replica set");
        for (Agent agent : dataNodes) {
            Request req = Commands.getStartNodeCommand(cfg);
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        task.setData(TaskType.START_REPLICA_SET);
        return task;
    }

    public static Task getRegisterSecondaryNodesWithPrimaryTask(Set<Agent> dataNodes, Config cfg) {
        Task task = new Task("Register secondary nodes with primary");
        StringBuilder secondaryStr = new StringBuilder();
        Iterator<Agent> it = dataNodes.iterator();
        Agent primaryNodeAgent = it.next();
        while (it.hasNext()) {
            Agent secondaryNodeAgent = it.next();
            secondaryStr.append("rs.add('").
                    append(secondaryNodeAgent.getHostname()).append(".").append(cfg.getDomainName()).
                    append(":").append(cfg.getDataNodePort()).append("');");
        }
        Request req = Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString(), cfg);
        req.setUuid(primaryNodeAgent.getUuid());
        task.addRequest(req);
        return task;
    }

    public static Task getRegisterSecondaryNodeWithPrimaryTask(Agent primaryNodeAgent, Agent secondaryNodeAgent, Config cfg) {
        Task task = new Task("Register secondary node with primary");
        StringBuilder secondaryStr = new StringBuilder();
        secondaryStr.append("rs.add('").
                append(secondaryNodeAgent.getHostname()).append(".").append(cfg.getDomainName()).
                append(":").append(cfg.getDataNodePort()).append("');");
        Request req = Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString(), cfg);
        req.setUuid(primaryNodeAgent.getUuid());
        task.addRequest(req);
        return task;
    }

    public static Task getRegisterReplicaSetAsShardWithRouter(String replicaSetName, Agent router, Set<Agent> dataNodes, Config cfg) {
        Task task = new Task("Register replica set as shard with router");
        StringBuilder shard = new StringBuilder();
        for (Agent agent : dataNodes) {
            shard.append("sh.addShard('").append(replicaSetName).
                    append("/").append(agent.getHostname()).append(".").append(cfg.getDomainName()).
                    append(":").append(cfg.getDataNodePort()).append("');");
        }
        Request req = Commands.getRegisterShardsWithRouterCommand(
                shard.toString(), cfg);
        req.setUuid(router.getUuid());
        task.addRequest(req);
        return task;
    }

    public static Task getUnregisterSecondaryFromPrimaryTask(Agent primaryNode, Agent secondaryNode, Config cfg) {
        Task task = new Task("Unregister secondary node from primary");
        Request req = Commands.getUnregisterSecondaryNodeFromPrimaryCommand(
                String.format("%s.%s", secondaryNode.getHostname(), cfg.getDomainName()), cfg);
        req.setUuid(primaryNode.getUuid());
        task.addRequest(req);
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
            req.setUuid(agent.getUuid());
            task.addRequest(req);
        }
        return task;
    }

    public static Task getFindPrimaryNodeTask(Agent secondaryNode, Config cfg) {
        Task task = new Task("Find primary node");
        Request req = Commands.getFindPrimaryNodeCommand(cfg);
        req.setUuid(secondaryNode.getUuid());
        task.addRequest(req);
        task.setData(TaskType.FIND_PRIMARY_NODE);
        return task;
    }
}
