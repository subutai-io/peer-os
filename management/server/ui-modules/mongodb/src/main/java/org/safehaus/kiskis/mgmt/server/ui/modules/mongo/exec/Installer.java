/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.HashSet;
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

    public Installer(MongoWizard mongoWizard) {
        CommandManagerInterface commandManager = ServiceLocator.getService(CommandManagerInterface.class);

        Set<Agent> allClusterMembers = new HashSet<Agent>();
        allClusterMembers.addAll(mongoWizard.getConfig().getConfigServers());
        allClusterMembers.addAll(mongoWizard.getConfig().getRouterServers());
        allClusterMembers.addAll(mongoWizard.getConfig().getShards());

        //INSTALL MONGO
        Task installMongoTask = RequestUtil.createTask(commandManager, "Mongo Install");
        for (Agent agent : allClusterMembers) {
            Command cmd = MongoCommands.getInstallCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(installMongoTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(installMongoTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            installMongoTask.addCommand(cmd);
        }

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

        //START ROUTERS
        Task startRoutersTask = RequestUtil.createTask(commandManager, "Start routers");
        StringBuilder configServersArg = new StringBuilder();
        for (Agent agent : mongoWizard.getConfig().getConfigServers()) {
            String ipOrHost = RequestUtil.getAgentIpByMask(agent, Common.IP_MASK);
            if (ipOrHost == null) {
                ipOrHost = agent.getHostname();
            }
            configServersArg.append(ipOrHost).append(":").append(Constants.MONGO_CONFIG_SERVER_PORT).append(",");
        }
        //drop comma
        if (configServersArg.length() > 0) {
            configServersArg.setLength(configServersArg.length() - 1);
        }
        for (Agent agent : mongoWizard.getConfig().getRouterServers()) {
            Command cmd = MongoCommands.getStartRouterCommand();
            cmd.getRequest().setUuid(agent.getUuid());
            cmd.getRequest().setTaskUuid(startRoutersTask.getUuid());
            cmd.getRequest().setRequestSequenceNumber(startRoutersTask.getIncrementedReqSeqNumber());
            cmd.getRequest().setSource(mongoWizard.getSource());
            cmd.getRequest().getArgs().add(configServersArg.toString());
            startRoutersTask.addCommand(cmd);
        }
        
        //

    }

}
