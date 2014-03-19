/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class AddConfigSrvOperation extends Operation {

    public AddConfigSrvOperation(Config config, Agent agent) {
        super("Add New Config Server");

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        clusterMembers.add(agent);

        addTask(Tasks.getKillRunningMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getUninstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getCleanMongoDataTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getAptGetUpdateTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getInstallMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getStopMongoTask(Util.wrapAgentToSet(agent)));

        addTask(Tasks.getRegisterIpsTask(clusterMembers, config));

        addTask(Tasks.getStartConfigServersTask(Util.wrapAgentToSet(agent), config));

        Task stopMongoTask = Tasks.getStopMongoTask(config.getRouterServers());
        stopMongoTask.setIgnoreExitCode(true);
        addTask(stopMongoTask);

        Set<Agent> newConfigServers = new HashSet<Agent>(config.getConfigServers());
        newConfigServers.add(agent);

        Task startRoutersTask = Tasks.getStartRoutersTask(config.getRouterServers(), newConfigServers, config);
        startRoutersTask.setData(TaskType.RESTART_ROUTERS);
        startRoutersTask.setIgnoreExitCode(true);
        addTask(startRoutersTask);

    }

}
