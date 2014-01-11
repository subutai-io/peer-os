/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

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
        allClusterMembers.addAll(config.getDataNodes());

        //UNINSTALL MONGO
        Task uninstallMongoTask = Util.createTask("Uninstall Mongo");
        //uninstall it
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
    }

}
