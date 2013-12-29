/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.commands.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class Uninstaller extends Operation {

    public Uninstaller(final Wizard wizard) {
        super(wizard, "Mongo Uninstallation");

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

    }

}
