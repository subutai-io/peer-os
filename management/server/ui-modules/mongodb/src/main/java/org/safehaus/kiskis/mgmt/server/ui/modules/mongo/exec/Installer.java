/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.InstallerConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
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
    private final Task startShardsTask;
    private final StringBuilder startConfigServersTaskOutput = new StringBuilder();
    private final StringBuilder startRoutersTaskOutput = new StringBuilder();
    private final StringBuilder startShardsTaskOutput = new StringBuilder();

    public Installer(InstallerConfig config) {
        super("Mongo Installation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(config.getConfigServers());
        allClusterMembers.addAll(config.getRouterServers());
        allClusterMembers.addAll(config.getShards());

        //KILL && UNINSTALL MONGO
        Task uninstallMongoTask = Util.createTask("Uninstall Mongo");
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getKillNCleanCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            uninstallMongoTask.addCommand(cmd);
        }
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getUninstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            uninstallMongoTask.addCommand(cmd);
        }
        uninstallMongoTask.setIgnoreExitCode(true);
        addTask(uninstallMongoTask);

        //INSTALL MONGO
        Task installMongoTask = Util.createTask("Install Mongo");
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getInstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(installMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(installMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            installMongoTask.addCommand(cmd);
        }
        addTask(installMongoTask);

        //STOP MONGODB ON ALL NODES
        Task stopMongoOnAllNodes = Util.createTask("Stop Mongo on all nodes");
        for (Agent agent : allClusterMembers) {
//            Command cmd = Commands.getStopNodeCommand();
            Command cmd = Commands.getKillAllCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(stopMongoOnAllNodes.getUuid());
            cmd.getRequest().setRequestSequenceNumber(stopMongoOnAllNodes.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            stopMongoOnAllNodes.addCommand(cmd);
        }
        stopMongoOnAllNodes.setIgnoreExitCode(true);
        addTask(stopMongoOnAllNodes);

        //ADD HOST NAME OF EACH NODE TO OTHER NODE'S /ETC/HOSTS FILE
        Task addNodesIpHostToOtherNodesTask = Util.createTask("Register nodes's IP-Host with other nodes");
        for (Agent agent : allClusterMembers) {
            StringBuilder hosts = new StringBuilder();
            for (Agent otherAgent : allClusterMembers) {
                if (agent != otherAgent) {
                    hosts.append("if ! /bin/grep -q '").
                            append(Util.getAgentIpByMask(otherAgent, Common.IP_MASK)).
                            append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN).
                            append("' '/etc/hosts'; then /bin/echo '").
                            append(Util.getAgentIpByMask(otherAgent, Common.IP_MASK)).
                            append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN).
                            append("' >> '/etc/hosts'; fi ;");
                }
            }
            Command cmd = Commands.getAddNodesIpHostToOtherNodesCommand(hosts.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(addNodesIpHostToOtherNodesTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(addNodesIpHostToOtherNodesTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            addNodesIpHostToOtherNodesTask.addCommand(cmd);
        }
        addTask(addNodesIpHostToOtherNodesTask);

        //ADD REPLICA SET NAME TO CONFIG OF EACH SHARD
        Task setReplicaSetNameTask = Util.createTask(String.format("Set Replica Set name to %s", config.getReplicaSetName()));
        for (Agent agent : config.getShards()) {
            Command cmd = Commands.getSetReplicaSetNameCommand(config.getReplicaSetName());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(setReplicaSetNameTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(setReplicaSetNameTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            setReplicaSetNameTask.addCommand(cmd);
        }
        addTask(setReplicaSetNameTask);

        //START CONFIG SERVERS
        startConfigServersTask = Util.createTask("Start config servers");
        for (Agent agent : config.getConfigServers()) {
            Command cmd = Commands.getStartConfigServerCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startConfigServersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startConfigServersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            startConfigServersTask.addCommand(cmd);
        }
        addTask(startConfigServersTask);

        //START ROUTERS
        startRoutersTask = Util.createTask("Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : config.getConfigServers()) {
            configServersArg.append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                    append(":").append(Constants.MONGO_CONFIG_SERVER_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : config.getRouterServers()) {
            Command cmd = Commands.getStartRouterCommand(configServersArg.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startRoutersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startRoutersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            startRoutersTask.addCommand(cmd);
        }
        addTask(startRoutersTask);

        //START SHARDS
        startShardsTask = Util.createTask("Start shards");
        for (Agent agent : config.getShards()) {
            Command cmd = Commands.getStartNodeCommand2();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startShardsTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startShardsTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            startShardsTask.addCommand(cmd);
        }
        addTask(startShardsTask);

        //choose the first shard as primary node
        Agent primaryNode = config.getShards().iterator().next();
        Command cmd;

        //REGISTER SECONDARY NODES WITH PRIMARY
        Task registerSecondaryNodesWithPrimaryTask = Util.createTask("Register secondary nodes with primary");
        StringBuilder secondaryStr = new StringBuilder();
        for (Agent agent : config.getShards()) {
            if (agent != primaryNode) {
                secondaryStr.append("rs.add('").
                        append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                        append("');");
            }
        }
        cmd = Commands.getRegisterSecondaryNodesWithPrimaryCommand(secondaryStr.toString());
        cmd.getRequest().setUuid(primaryNode.getUuid());
        cmd.getRequest().setTaskUuid(registerSecondaryNodesWithPrimaryTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerSecondaryNodesWithPrimaryTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        registerSecondaryNodesWithPrimaryTask.addCommand(cmd);
        addTask(registerSecondaryNodesWithPrimaryTask);

        //REGISTER SHARDS WITH ONE OF THE ROUTERS
        Task registerShardsWithRouterTask = Util.createTask("Register shards with router");
        Agent router = config.getRouterServers().iterator().next();
        StringBuilder shards = new StringBuilder();
        for (Agent shard : config.getShards()) {
            shards.append("sh.addShard('").append(config.getReplicaSetName()).
                    append("/").append(shard.getHostname()).append(Constants.DOMAIN).
                    append(":").append(Constants.MONGO_SHARD_PORT).append("');");
        }
        cmd = Commands.getRegisterShardsWithRouterCommand(
                shards.toString());
        cmd.getRequest().setUuid(router.getUuid());
        cmd.getRequest().setTaskUuid(registerShardsWithRouterTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerShardsWithRouterTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        registerShardsWithRouterTask.addCommand(cmd);
        addTask(registerShardsWithRouterTask);

    }

    @Override
    protected void beforeResponseProcessed(Response response) {
        Task task = getCurrentTask();
        if ((task == startConfigServersTask || task == startRoutersTask || task == startShardsTask)
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
            if (task == startShardsTask) {
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
