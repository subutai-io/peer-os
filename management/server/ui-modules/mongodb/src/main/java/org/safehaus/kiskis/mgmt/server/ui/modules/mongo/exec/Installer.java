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
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Installer extends Operation {

    private final Task startConfigServersTask;

    public Installer(InstallerConfig config) {
        super("Mongo Installation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(config.getConfigServers());
        allClusterMembers.addAll(config.getRouterServers());
        allClusterMembers.addAll(config.getShards());

        //KILL MONGO
        //add here separate task ignore exit code
        //UNINSTALL MONGO
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

        //START ROUTERS
        Task startRoutersTask = Util.createTask("Start routers");
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

        //ADD REPLICA TO EACH OTHERS /ETC/HOSTS
        Task setReplicaSetNameTask = Util.createTask("Set ReplicaSet name");
        for (Agent agent : config.getShards()) {
            Command cmd = Commands.getSetReplicaSetNameCommand(config.getReplicaSetName());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(setReplicaSetNameTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(setReplicaSetNameTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            setReplicaSetNameTask.addCommand(cmd);
        }
        addTask(setReplicaSetNameTask);

        //ADD HOST NAME OF EACH SHARD TO OTHER SHARD'S /ETC/HOSTS FILE
        Task addShardHostToOtherShardsTask = Util.createTask("Add Shard Host To Other Shards");
        for (Agent agent : config.getShards()) {
            StringBuilder hosts = new StringBuilder();
            for (Agent otherAgent : config.getShards()) {
                if (agent != otherAgent) {
                    hosts.append("\n").append(Util.getAgentIpByMask(otherAgent, Common.IP_MASK))
                            .append(" ").append(otherAgent.getHostname()).append(Constants.DOMAIN);
                }
            }

            Command cmd = Commands.getAddShardHostToOtherShardsCommand(hosts.toString());
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(addShardHostToOtherShardsTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(addShardHostToOtherShardsTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            addShardHostToOtherShardsTask.addCommand(cmd);
        }
        addTask(addShardHostToOtherShardsTask);

        //RESTART SHARDS
        Task restartShards = Util.createTask("Restart shards");
        for (Agent agent : config.getShards()) {
            Command cmd = Commands.getRestartShardCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(restartShards.getUuid());
            cmd.getRequest().setRequestSequenceNumber(restartShards.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            restartShards.addCommand(cmd);
        }
        addTask(restartShards);

        //REGISTER SECONDARY NODES ON PRIMARY
        Task registerSecondaryNodesWithPrimaryTask = Util.createTask("Register secondary nodes with primary");
        //Make the first node as primary
        Agent primaryNode = config.getShards().iterator().next();
        StringBuilder secondaryStr = new StringBuilder();
        for (Agent agent : config.getShards()) {
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
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        registerSecondaryNodesWithPrimaryTask.addCommand(cmd);
        addTask(registerSecondaryNodesWithPrimaryTask);

        //REGISTER PRIMARY NODE WITH ONE OF THE ROUTERS
        Task registerPrimaryWithRouterTask = Util.createTask("Register primary with router");
        Agent router = config.getRouterServers().iterator().next();
        cmd = Commands.getRegisterPrimaryWithRouterCommand(
                config.getReplicaSetName(),
                primaryNode.getHostname() + Constants.DOMAIN);//use hostname when fixed
        cmd.getRequest().setUuid(router.getUuid());
        cmd.getRequest().setTaskUuid(registerPrimaryWithRouterTask.getUuid());
        cmd.getRequest().setRequestSequenceNumber(registerPrimaryWithRouterTask.getIncrementedReqSeqNumber());
        cmd.getRequest().setSource(MongoModule.MODULE_NAME);
        registerPrimaryWithRouterTask.addCommand(cmd);
        addTask(registerPrimaryWithRouterTask);

    }

    @Override
    public void onTaskCompleted(Task task) {
        System.out.println("Task completed " + task);
    }

    @Override
    public void onTaskSucceeded(Task task) {
        //PROCESS OUTPUT OF START CONFIG SERVERS HERE
        //IF OK THEN NO-OP
        //ELSE fail the task with custom output appended
        if (task == startConfigServersTask) {
            int j = 0;
            for (int i = 1; i <= task.getReqSeqNumber(); i++) {
                Response response = commandManager.getResponse(task.getUuid(), i);
                if (response != null && response.getStdOut() != null) {
                    if (response.getStdOut().contains("child process started successfully, parent exiting")) {
                        j++;
                    }
                }
            }
            if (j != task.getReqSeqNumber()) {
                fail();
                appendOutput("Could not succesfully start config servers on all nodes.");
            }
        }
        System.out.println("Task succeeded " + task);
    }

    @Override
    public void onTaskFailed(Task task) {
        System.out.println("Task failed " + task);
    }

    @Override
    public void onOperationEnded() {
        System.out.println("Operation ended");
    }

    @Override
    public void onOperationStarted() {
        System.out.println("Operation started");
    }

    @Override
    public void onOperationStopped() {
        System.out.println("Operation stopped");
    }

    @Override
    public void onBeforeTaskRun(Task task) {
        System.out.println("Before running task" + task);
    }

    @Override
    public void onAfterTaskRun(Task task) {
        System.out.println("After running task " + task);
    }

}
