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
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
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
        Task uninstallMongoTask = RequestUtil.createTask(commandManager, Constants.MONGO_UNINSTALL_TASK_NAME);
        //uninstall it
        for (Agent agent : allClusterMembers) {
            Command cmd = Commands.getUninstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(uninstallMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(uninstallMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(MongoModule.MODULE_NAME);
            uninstallMongoTask.addCommand(cmd);
        }
        addTask(uninstallMongoTask);
    }

//    @Override
//    public void onResponse(Response response) {
//
//    }
//    @Override
//    public String getOutput() {
//        return output;
//    }
}
