/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class DestroyNodeOperation extends Operation {

    public DestroyNodeOperation(Agent nodeAgent, ClusterConfig config, NodeType nodeType) {
        super(null);

        if (nodeType == NodeType.CONFIG_NODE) {
            addTask(ManagerTasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getStopNodeTask(config.getRouterServers()));
            Set<Agent> otherConfigServers = new HashSet<Agent>(config.getConfigServers());
            otherConfigServers.remove(nodeAgent);
            Task startRoutersTask = ManagerTasks.getStartRouterTask(config.getRouterServers(), otherConfigServers);
            startRoutersTask.setIgnoreExitCode(true);
            addTask(startRoutersTask);
        } else if (nodeType == NodeType.DATA_NODE) {
            Task findPrimaryNodeTask = ManagerTasks.getFindPrimaryNodeTask(nodeAgent);
            addTask(findPrimaryNodeTask);
            addTask(ManagerTasks.getUnregisterSecondaryFromPrimaryTask(nodeAgent, nodeAgent));
            addTask(ManagerTasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
        } else if (nodeType == NodeType.ROUTER_NODE) {
            addTask(ManagerTasks.getKillRunningMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getUninstallMongoTask(Util.wrapAgentToSet(nodeAgent)));
            addTask(ManagerTasks.getCleanMongoDataTask(Util.wrapAgentToSet(nodeAgent)));
        }
    }

}
