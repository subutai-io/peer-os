/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class InstallClusterOperation extends Operation {

    private final ClusterConfig config;

    public ClusterConfig getConfig() {
        return config;
    }

    public InstallClusterOperation(ClusterConfig config) {
        super("Install Mongo cluster");
        this.config = config;

        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        addTask(Tasks.getKillRunningMongoTask(clusterMembers));

        addTask(Tasks.getUninstallMongoTask(clusterMembers));

        addTask(Tasks.getCleanMongoDataTask(clusterMembers));

        addTask(Tasks.getAptGetUpdateTask(clusterMembers));

        addTask(Tasks.getInstallMongoTask(clusterMembers));

        addTask(Tasks.getStopMongoTask(clusterMembers));

        addTask(Tasks.getRegisterIpsTask(clusterMembers));

        addTask(Tasks.getSetReplicaSetNameTask(
                config.getReplicaSetName(),
                config.getDataNodes()));

        addTask(Tasks.getStartConfigServersTask(
                config.getConfigServers()));

        addTask(Tasks.getStartRoutersTask(
                config.getRouterServers(),
                config.getConfigServers()));

        addTask(Tasks.getStartReplicaSetTask(
                config.getDataNodes()));

        addTask(Tasks.getRegisterSecondaryNodesWithPrimaryTask(
                config.getDataNodes()));

        addTask(Tasks.getRegisterReplicaSetAsShardWithRouter(
                config.getReplicaSetName(),
                config.getRouterServers().iterator().next(),
                config.getDataNodes()));

    }

}
