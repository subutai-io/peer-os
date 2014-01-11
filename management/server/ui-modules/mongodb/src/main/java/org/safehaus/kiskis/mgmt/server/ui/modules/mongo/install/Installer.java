/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Installer extends Operation {

    private final Task startConfigServersTask;
    private final Task startRoutersTask;
    private final Task startReplicaSet;
    private final StringBuilder startConfigServersTaskOutput = new StringBuilder();
    private final StringBuilder startRoutersTaskOutput = new StringBuilder();
    private final StringBuilder startShardsTaskOutput = new StringBuilder();

    public Installer(InstallerConfig config) {
        super("Mongo Installation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(config.getConfigServers());
        allClusterMembers.addAll(config.getRouterServers());
        allClusterMembers.addAll(config.getDataNodes());

        //KILL && UNINSTALL MONGO
        Task uninstallMongoTask = Util.createTask("Uninstall Mongo");
        for (Agent agent : allClusterMembers) {
            bindCmdToAgentNTask(Commands.getKillAllCommand(), agent, uninstallMongoTask);
        }
        for (Agent agent : allClusterMembers) {
            bindCmdToAgentNTask(Commands.getCleanCommand(), agent, uninstallMongoTask);
        }
        for (Agent agent : allClusterMembers) {
            bindCmdToAgentNTask(Commands.getUninstallCommand(), agent, uninstallMongoTask);
        }
        uninstallMongoTask.setIgnoreExitCode(true);
        addTask(uninstallMongoTask);

        //INSTALL MONGO
        Task installMongoTask = Util.createTask("Install Mongo");
        for (Agent agent : allClusterMembers) {
            bindCmdToAgentNTask(Commands.getInstallCommand(), agent, installMongoTask);
        }
        addTask(installMongoTask);

        //STOP MONGODB ON ALL NODES
        Task stopMongoOnAllNodes = Util.createTask("Stop Mongo on all nodes");
        for (Agent agent : allClusterMembers) {
            bindCmdToAgentNTask(Commands.getKillAllCommand(), agent, stopMongoOnAllNodes);
        }
        stopMongoOnAllNodes.setIgnoreExitCode(true);
        addTask(stopMongoOnAllNodes);

        //ADD HOST NAME OF EACH NODE TO OTHER NODE'S /ETC/HOSTS FILE
        Task addNodesIpHostToOtherNodesTask = Util.createTask("Register nodes's IP-Host with other nodes");
        for (Agent agent : allClusterMembers) {
            StringBuilder cleanHosts = new StringBuilder();
            StringBuilder appendHosts = new StringBuilder();
            for (Agent otherAgent : allClusterMembers) {
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
            bindCmdToAgentNTask(
                    Commands.getAddNodesIpHostToOtherNodesCommand(appendHosts.toString()),
                    agent, addNodesIpHostToOtherNodesTask);
        }
        addTask(addNodesIpHostToOtherNodesTask);

        //ADD REPLICA SET NAME TO CONFIG OF EACH RS
        Task setReplicaSetNameTask = Util.createTask(String.format("Set replica set name to %s", config.getReplicaSetName()));
        for (Agent agent : config.getDataNodes()) {
            bindCmdToAgentNTask(
                    Commands.getSetReplicaSetNameCommand(config.getReplicaSetName()),
                    agent, setReplicaSetNameTask);
        }
        addTask(setReplicaSetNameTask);

        //START CONFIG SERVERS
        startConfigServersTask = Util.createTask("Start config servers");
        for (Agent agent : config.getConfigServers()) {
            bindCmdToAgentNTask(Commands.getStartConfigServerCommand(), agent, startConfigServersTask);
        }
        addTask(startConfigServersTask);

        //START ROUTERS
        startRoutersTask = Util.createTask("Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : config.getConfigServers()) {
            configServersArg.append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                    append(":").append(Constants.CONFIG_SRV_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : config.getRouterServers()) {
            bindCmdToAgentNTask(Commands.getStartRouterCommand(configServersArg.toString()),
                    agent, startRoutersTask);
        }
        addTask(startRoutersTask);

        //START RS
        startReplicaSet = Util.createTask("Start replica set");
        for (Agent agent : config.getDataNodes()) {
            bindCmdToAgentNTask(Commands.getStartNodeCommand(), agent, startReplicaSet);
        }
        addTask(startReplicaSet);

        //choose the first shard as primary node
        Agent primaryNode = config.getDataNodes().iterator().next();

        //REGISTER SECONDARY NODES WITH PRIMARY
        Task registerSecondaryNodesWithPrimaryTask = Util.createTask("Register secondary nodes with primary");
        StringBuilder secondaryStr = new StringBuilder();
        for (Agent agent : config.getDataNodes()) {
            if (agent != primaryNode) {
                secondaryStr.append("rs.add('").
                        append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                        append(":").append(Constants.DATA_NODE_PORT).append("');");
            }
        }
        {
            bindCmdToAgentNTask(
                    Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString()),
                    primaryNode, registerSecondaryNodesWithPrimaryTask);
        }
        addTask(registerSecondaryNodesWithPrimaryTask);

        //REGISTER NODES OF REPLICA SET AS SHARD WITH ONE OF THE ROUTERS
        Task registerShardsWithRouterTask = Util.createTask("Register shard with router");
        Agent router = config.getRouterServers().iterator().next();
        StringBuilder shards = new StringBuilder();
        for (Agent rs : config.getDataNodes()) {
            shards.append("sh.addShard('").append(config.getReplicaSetName()).
                    append("/").append(rs.getHostname()).append(Constants.DOMAIN).
                    append(":").append(Constants.DATA_NODE_PORT).append("');");
        }
        {
            bindCmdToAgentNTask(Commands.getRegisterShardsWithRouterCommand(
                    shards.toString()), router, registerShardsWithRouterTask);
        }
        addTask(registerShardsWithRouterTask);

    }

    @Override
    protected void beforeResponseProcessed(Response response) {
        Task task = getCurrentTask();
        if ((task == startConfigServersTask || task == startRoutersTask || task == startReplicaSet)
                && response.getStdOut() != null) {
            boolean isOk = false;
            if (task == startConfigServersTask) {
                startConfigServersTaskOutput.append(response.getStdOut());
                isOk = startConfigServersTaskOutput.toString().contains("child process started successfully, parent exiting");
            }
            if (task == startRoutersTask) {
                startRoutersTaskOutput.append(response.getStdOut());
                isOk = startRoutersTaskOutput.toString().contains("child process started successfully, parent exiting");
            }
            if (task == startReplicaSet) {
                startShardsTaskOutput.append(response.getStdOut());
                isOk = startShardsTaskOutput.toString().contains("child process started successfully, parent exiting");
            }
            if (isOk) {
                response.setType(ResponseType.EXECUTE_RESPONSE_DONE);
                response.setExitCode(0);
            }
        }

        super.beforeResponseProcessed(response);
    }

}
