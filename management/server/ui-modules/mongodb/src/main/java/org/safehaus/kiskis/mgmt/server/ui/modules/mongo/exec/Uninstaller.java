/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.InstallerConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class Uninstaller extends Operation {

    public Uninstaller(InstallerConfig config) {
        super("Mongo Uninstallation");

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(config.getConfigServers());
        allClusterMembers.addAll(config.getRouterServers());
        allClusterMembers.addAll(config.getShards());

        //UNINSTALL MONGO
        Task uninstallMongoTask = Util.createTask("Uninstall Mongo");
        //uninstall it
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
    }

    @Override
    void onTaskCompleted(Task task) {
        System.out.println("Task completed " + task);
    }

    @Override
    void onTaskSucceeded(Task task) {
        System.out.println("Task succeeded " + task);
    }

    @Override
    void onTaskFailed(Task task) {
        System.out.println("Task failed " + task);
    }

    @Override
    void onOperationEnded() {
        System.out.println("Operation ended");
    }

    @Override
    void onOperationStarted() {
        System.out.println("Operation started");
    }

    @Override
    void onOperationStopped() {
        System.out.println("Operation stopped");
    }

    @Override
    void onBeforeTaskRun(Task task) {
        System.out.println("Before running task" + task);
    }

    @Override
    void onAfterTaskRun(Task task) {
        System.out.println("After running task " + task);
    }

}
