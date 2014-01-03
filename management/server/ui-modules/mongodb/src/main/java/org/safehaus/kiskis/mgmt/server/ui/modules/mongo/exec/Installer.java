/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.text.MessageFormat;
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

    public Installer(InstallerConfig config) {
        super("Mongo Installation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(config.getConfigServers());
        allClusterMembers.addAll(config.getRouterServers());
        allClusterMembers.addAll(config.getShards());

        //KILL && UNINSTALL MONGO
        Task uninstallMongoTask = Util.createTask("Uninstall Mongo");
        //uninstall it
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getKillAllCommand();
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

        //STOP MONGODB ON CONFIG SERVERS
        Task stopMongoDBOnConfigServers = Util.createTask("Stop MongoDB on config servers");
        for (Agent agent : config.getConfigServers()) {
            Command cmd = Commands.getStopShardCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(stopMongoDBOnConfigServers.getUuid());
            cmd.getRequest().setRequestSequenceNumber(stopMongoDBOnConfigServers.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            stopMongoDBOnConfigServers.addCommand(cmd);
        }
        stopMongoDBOnConfigServers.setIgnoreExitCode(true);
        addTask(stopMongoDBOnConfigServers);

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
        //============process output of mongod========
        startConfigServersTask.setIgnoreExitCode(true);
        //============================================
        addTask(startConfigServersTask);

        //STOP MONGODB ON ROUTERS
        Task stopMongoDBOnRouters = Util.createTask("Stop MongoDB on routers");
        for (Agent agent : config.getRouterServers()) {
            Command cmd = Commands.getStopShardCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(stopMongoDBOnRouters.getUuid());
            cmd.getRequest().setRequestSequenceNumber(stopMongoDBOnRouters.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            stopMongoDBOnRouters.addCommand(cmd);
        }
        stopMongoDBOnRouters.setIgnoreExitCode(true);
        addTask(stopMongoDBOnRouters);

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
        //============process output of mongod========
        startRoutersTask.setIgnoreExitCode(true);
        //============================================
        addTask(startRoutersTask);

        //ADD REPLICA SET NAME TO CONFIG OF EACH MEMBER
        Task setReplicaSetNameTask = Util.createTask("Set Replica Set name");
        for (Agent agent : config.getShards()) {
            Command cmd = Commands.getSetReplicaSetNameCommand(config.getReplicaSetName());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(setReplicaSetNameTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(setReplicaSetNameTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            setReplicaSetNameTask.addCommand(cmd);
        }
        addTask(setReplicaSetNameTask);

        //ADD HOST NAME OF EACH NODE TO OTHER NODE'S /ETC/HOSTS FILE
        //check if ip-host pair laready exists
        Task addShardHostToOtherShardsTask = Util.createTask("Register nodes's IP-Host with the other nodes");
        for (Agent agent : allClusterMembers) {
            for (Agent otherAgent : allClusterMembers) {
                if (agent != otherAgent) {
                    StringBuilder hosts = new StringBuilder();
                    hosts.append(Util.getAgentIpByMask(otherAgent, Common.IP_MASK))
                            .append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN);
                    Command cmd = Commands.getAddShardHostToOtherShardsCommand(hosts.toString());
                    cmd.getRequest().setUuid(agent.getUuid());
                    cmd.getRequest().setTaskUuid(addShardHostToOtherShardsTask.getUuid());
                    cmd.getRequest().setRequestSequenceNumber(addShardHostToOtherShardsTask.getIncrementedReqSeqNumber());
                    cmd.getRequest().setSource(MongoModule.MODULE_NAME);
                    addShardHostToOtherShardsTask.addCommand(cmd);
                }
            }
        }
        addTask(addShardHostToOtherShardsTask);

        //RESTART NODES
        Task restartShards = Util.createTask("Restart shards");
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getRestartNodeCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(restartShards.getUuid());
            cmd.getRequest().setRequestSequenceNumber(restartShards.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            restartShards.addCommand(cmd);
        }
        addTask(restartShards);

        //Make the first node as primary
        Agent primaryNode = config.getShards().iterator().next();
        //SET PRIMARY NODE CONFIG
        Task setPrimaryNodeConfigTask = Util.createTask("Set replica set's primary node config");
        Command cmd = Commands.getSetPrimaryShardConfigCommand();
        cmd.getRequest().setUuid(primaryNode.getUuid());
        cmd.getRequest().setTaskUuid(setPrimaryNodeConfigTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(setPrimaryNodeConfigTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        setPrimaryNodeConfigTask.addCommand(cmd);
        setPrimaryNodeConfigTask.setIgnoreExitCode(true);
        addTask(setPrimaryNodeConfigTask);

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
        cmd = Commands.getAddSecondaryReplicasToPrimaryCommand2(secondaryStr.toString());
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
        cmd = Commands.getRegisterShardWithRouterCommand(
                router.getHostname(),
                shards.toString());
        cmd.getRequest().setUuid(router.getUuid());
        cmd.getRequest().setTaskUuid(registerShardsWithRouterTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerShardsWithRouterTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        registerShardsWithRouterTask.addCommand(cmd);
        addTask(registerShardsWithRouterTask);
    }

    @Override
    public void onTaskSucceeded(Task task) {
        //PROCESS OUTPUT OF START CONFIG SERVERS HERE
        //IF OK THEN NO-OP
        //ELSE fail the task with custom output appended
//        if (task == startConfigServersTask || task == startRoutersTask) {
//            int j = 0;
//            for (int i = 1; i <= task.getReqSeqNumber(); i++) {
//                Response response = commandManager.getResponse(task.getUuid(), i);
//                if (response != null && response.getStdOut() != null) {
//                    if (response.getStdOut().contains("child process started successfully, parent exiting")) {
//                        j++;
//                    }
//                }
//            }
//            if (j != task.getReqSeqNumber()) {
//                fail();
//                appendOutput(
//                        MessageFormat.format("Could not succesfully execute task {0}.",
//                                task.getDescription()));
//            }
//        }
//        System.out.println("Task succeeded " + task);
    }

    /*
     TODO:join responses in memory instead of polling db
     */
    @Override
    protected void beforeResponseProcessed(Response response) {
        Task task = getCurrentTask();
        if (task == startConfigServersTask || task == startRoutersTask) {
            Response wholeResponse = commandManager.getResponse(task.getUuid(), response.getRequestSequenceNumber());
            if (wholeResponse != null && wholeResponse.getStdOut() != null) {
                if (response.getStdOut().contains("child process started successfully, parent exiting")) {
                    response.setType(ResponseType.EXECUTE_RESPONSE_DONE);
                    response.setExitCode(0);
                }
            }
        }

        super.beforeResponseProcessed(response);
    }

}
