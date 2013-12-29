/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Installer extends Operation {

    public Installer(Wizard wizard) {
        super(wizard, "Mongo Installation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(wizard.getConfig().getConfigServers());
        allClusterMembers.addAll(wizard.getConfig().getRouterServers());
        allClusterMembers.addAll(wizard.getConfig().getShards());

        //UNINSTALL MONGO
        Task uninstallMongoTask = RequestUtil.createTask(commandManager, Constants.MONGO_UNINSTALL_TASK_NAME);
        //uninstall it
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getUninstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            uninstallMongoTask.addCommand(cmd);
        }
        addTask(uninstallMongoTask);
        //INSTALL MONGO
        Task installMongoTask = RequestUtil.createTask(commandManager, "Install Mongo");
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getInstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(installMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(installMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            installMongoTask.addCommand(cmd);
        }
        addTask(installMongoTask);

        //START CONFIG SERVERS
        Task startConfigServersTask = RequestUtil.createTask(commandManager, "Start config servers");
        for (Agent agent : wizard.getConfig().getConfigServers()) {
            Command cmd = Commands.getStartConfigServerCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startConfigServersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startConfigServersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            startConfigServersTask.addCommand(cmd);
        }
        addTask(startConfigServersTask);

        //START ROUTERS
        Task startRoutersTask = RequestUtil.createTask(commandManager, "Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : wizard.getConfig().getConfigServers()) {
            configServersArg.append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                    append(":").append(Constants.MONGO_CONFIG_SERVER_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : wizard.getConfig().getRouterServers()) {
            Command cmd = Commands.getStartRouterCommand(configServersArg.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startRoutersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startRoutersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            startRoutersTask.addCommand(cmd);
        }
        addTask(startRoutersTask);

        //ADD REPLICA TO EACH OTHERS /ETC/HOSTS
        Task setReplicaSetNameTask = RequestUtil.createTask(commandManager, "Set ReplicaSet name");
        for (Agent agent : wizard.getConfig().getShards()) {
            Command cmd = Commands.getSetReplicaSetNameCommand(wizard.getConfig().getReplicaSetName());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(setReplicaSetNameTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(setReplicaSetNameTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            setReplicaSetNameTask.addCommand(cmd);
        }
        addTask(setReplicaSetNameTask);

        //ADD HOST NAME OF EACH SHARD TO OTHER SHARD'S /ETC/HOSTS FILE
        Task addShardHostToOtherShardsTask = RequestUtil.createTask(commandManager, "Add Shard Host To Other Shards");
        for (Agent agent : wizard.getConfig().getShards()) {
            StringBuilder hosts = new StringBuilder();
            for (Agent otherAgent : wizard.getConfig().getShards()) {
                if (agent != otherAgent) {
                    hosts.append("\n").append(RequestUtil.getAgentIpByMask(otherAgent, Common.IP_MASK))
                            .append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN);
                }
            }

            Command cmd = Commands.getAddShardHostToOtherShardsCommand(hosts.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(addShardHostToOtherShardsTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(addShardHostToOtherShardsTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            addShardHostToOtherShardsTask.addCommand(cmd);
        }
        addTask(addShardHostToOtherShardsTask);

        //RESTART SHARDS
        Task restartShards = RequestUtil.createTask(commandManager, "Restart shards");
        for (Agent agent : wizard.getConfig().getShards()) {
            Command cmd = Commands.getRestartShardCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(restartShards.getUuid());
            cmd.getRequest().setRequestSequenceNumber(restartShards.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(wizard.getSource());
            restartShards.addCommand(cmd);
        }
        addTask(restartShards);

        //REGISTER SECONDARY NODES ON PRIMARY
        Task registerSecondaryNodesWithPrimaryTask = RequestUtil.createTask(commandManager, "Register secondary nodes with primary");
        //Make the first node as primary
        Agent primaryNode = wizard.getConfig().getShards().iterator().next();
        StringBuilder secondaryStr = new StringBuilder();
        for (Agent agent : wizard.getConfig().getShards()) {
            if (agent != primaryNode) {
                secondaryStr.append("\n'rs.add(\"").
                        append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                        append("\")'");
            }
        }
        Command cmd = Commands.getAddSecondaryReplicasToPrimaryCommand(secondaryStr.toString());
        cmd.getRequest().setUuid(primaryNode.getUuid());
        cmd.getRequest().setTaskUuid(registerSecondaryNodesWithPrimaryTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerSecondaryNodesWithPrimaryTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(wizard.getSource());
        registerSecondaryNodesWithPrimaryTask.addCommand(cmd);
        addTask(registerSecondaryNodesWithPrimaryTask);

        //REGISTER PRIMARY NODE WITH ONE OF THE ROUTERS
        Task registerPrimaryWithRouterTask = RequestUtil.createTask(commandManager, "Register primary with router");
        Agent router = wizard.getConfig().getRouterServers().iterator().next();
        cmd = Commands.getRegisterPrimaryWithRouterCommand(
                wizard.getConfig().getReplicaSetName(),
                primaryNode.getHostname() + Constants.DOMAIN);//use hostname when fixed
        cmd.getRequest().setUuid(router.getUuid());
        cmd.getRequest().setTaskUuid(registerPrimaryWithRouterTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerPrimaryWithRouterTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(wizard.getSource());
        registerPrimaryWithRouterTask.addCommand(cmd);
        addTask(registerPrimaryWithRouterTask);

    }

}
