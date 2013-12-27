/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands.MongoCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.MongoWizard;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Installer {

    private final List<Task> tasks = new ArrayList<Task>();
    private final Iterator<Task> tasksIterator;

    public Installer(MongoWizard mongoWizard) {
        CommandManagerInterface commandManager = ServiceLocator.getService(CommandManagerInterface.class);

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(mongoWizard.getConfig().getConfigServers());
        allClusterMembers.addAll(mongoWizard.getConfig().getRouterServers());
        allClusterMembers.addAll(mongoWizard.getConfig().getShards());

        //UNINSTALL MONGO
        Task uninstallMongoTask = RequestUtil.createTask(commandManager, Constants.MONGO_UNINSTALL_TASK_NAME);
        //kill mongod
        for (Agent agent : allClusterMembers) {
            Command cmd = MongoCommands.getForceKillMongodCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            uninstallMongoTask.addCommand(cmd);
        }
        //remove mongo data
        for (Agent agent : allClusterMembers) {
            Command cmd = MongoCommands.getRemoveDataDirCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            uninstallMongoTask.addCommand(cmd);
        }
        //uninstall it
        for (Agent agent : allClusterMembers) {
            Command cmd = MongoCommands.getUninstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            uninstallMongoTask.addCommand(cmd);
        }
        tasks.add(uninstallMongoTask);
        //INSTALL MONGO
        Task installMongoTask = RequestUtil.createTask(commandManager, "Install Mongo");
        for (Agent agent : allClusterMembers) {
            Command cmd = MongoCommands.getInstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(installMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(installMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            installMongoTask.addCommand(cmd);
        }
        tasks.add(installMongoTask);

        //START CONFIG SERVERS
        Task startConfigServersTask = RequestUtil.createTask(commandManager, "Start config servers");
        for (Agent agent : mongoWizard.getConfig().getConfigServers()) {
            Command cmd = MongoCommands.getStartConfigServerCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startConfigServersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startConfigServersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            startConfigServersTask.addCommand(cmd);
        }
        tasks.add(startConfigServersTask);

        //START ROUTERS
        Task startRoutersTask = RequestUtil.createTask(commandManager, "Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : mongoWizard.getConfig().getConfigServers()) {
            configServersArg.append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                    append(":").append(Constants.MONGO_CONFIG_SERVER_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : mongoWizard.getConfig().getRouterServers()) {
            Command cmd = MongoCommands.getStartRouterCommand(configServersArg.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startRoutersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startRoutersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            startRoutersTask.addCommand(cmd);
        }
        tasks.add(startRoutersTask);

        //ADD REPLICA TO EACH OTHERS /ETC/HOSTS
        Task setReplicaSetNameTask = RequestUtil.createTask(commandManager, "Set ReplicaSet name");
        for (Agent agent : mongoWizard.getConfig().getShards()) {
            Command cmd = MongoCommands.getSetReplicaSetNameCommand(mongoWizard.getConfig().getReplicaSetName());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(setReplicaSetNameTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(setReplicaSetNameTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            setReplicaSetNameTask.addCommand(cmd);
        }
        tasks.add(setReplicaSetNameTask);

        //ADD HOST NAME OF EACH SHARD TO OTHER SHARD'S /ETC/HOSTS FILE
        Task addShardHostToOtherShardsTask = RequestUtil.createTask(commandManager, "Add Shard Host To Other Shards");
        for (Agent agent : mongoWizard.getConfig().getShards()) {
            StringBuilder hosts = new StringBuilder();
            for (Agent otherAgent : mongoWizard.getConfig().getShards()) {
                if (agent != otherAgent) {
                    hosts.append("\n").append(RequestUtil.getAgentIpByMask(otherAgent, Common.IP_MASK))
                            .append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN);
                }
            }

            Command cmd = MongoCommands.getAddShardHostToOtherShardsCommand(hosts.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(addShardHostToOtherShardsTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(addShardHostToOtherShardsTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            addShardHostToOtherShardsTask.addCommand(cmd);
        }
        tasks.add(addShardHostToOtherShardsTask);

        //RESTART SHARDS
        Task restartShards = RequestUtil.createTask(commandManager, "Restart shards");
        for (Agent agent : mongoWizard.getConfig().getShards()) {
            Command cmd = MongoCommands.getRestartShardCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(restartShards.getUuid());
            cmd.getRequest().setRequestSequenceNumber(restartShards.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            restartShards.addCommand(cmd);
        }
        tasks.add(restartShards);

        //REGISTER SECONDARY NODES ON PRIMARY
        Task registerSecondaryNodesWithPrimaryTask = RequestUtil.createTask(commandManager, "Register secondary nodes with primary");
        //Make the first node as primary
        Agent primaryNode = mongoWizard.getConfig().getShards().iterator().next();
        StringBuilder secondaryStr = new StringBuilder();
        for (Agent agent : mongoWizard.getConfig().getShards()) {
            if (agent != primaryNode) {
                secondaryStr.append("\n'rs.add(\"").
                        append(agent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                        append("\")'");
            }
        }
        Command cmd = MongoCommands.getAddSecondaryReplicasToPrimaryCommand(secondaryStr.toString());
        cmd.getRequest().setUuid(primaryNode.getUuid());
        cmd.getRequest().setTaskUuid(registerSecondaryNodesWithPrimaryTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerSecondaryNodesWithPrimaryTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(mongoWizard.getSource());
        registerSecondaryNodesWithPrimaryTask.addCommand(cmd);
        tasks.add(registerSecondaryNodesWithPrimaryTask);

        //REGISTER PRIMARY NODE WITH ONE OF THE ROUTERS
        Task registerPrimaryWithRouterTask = RequestUtil.createTask(commandManager, "Register primary with router");
        Agent router = mongoWizard.getConfig().getRouterServers().iterator().next();
        cmd = MongoCommands.getRegisterPrimaryWithRouterCommand(
                mongoWizard.getConfig().getReplicaSetName(),
                primaryNode.getHostname() + Constants.DOMAIN);//use hostname when fixed
        cmd.getRequest().setUuid(router.getUuid());
        cmd.getRequest().setTaskUuid(registerPrimaryWithRouterTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerPrimaryWithRouterTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(mongoWizard.getSource());
        registerPrimaryWithRouterTask.addCommand(cmd);
        tasks.add(registerPrimaryWithRouterTask);

        //
        tasksIterator = tasks.iterator();
    }

    public Task start() {
        return executeNextTask();
    }

    public Task executeNextTask() {
        Task currentTask = null;
        if (tasksIterator.hasNext()) {
            currentTask = tasksIterator.next();
        }
        if (currentTask != null && currentTask.getCommands() != null
                && !currentTask.getCommands().isEmpty()) {
            for (Command cmd : currentTask.getCommands()) {
                ServiceLocator.getService(CommandManagerInterface.class).executeCommand(cmd);
            }
        }
        return currentTask;
    }

}
