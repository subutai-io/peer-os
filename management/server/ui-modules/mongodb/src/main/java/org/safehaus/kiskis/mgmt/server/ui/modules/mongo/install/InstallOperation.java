/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install;

import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class InstallOperation extends Operation {

    private final InstallerConfig config;

    public InstallerConfig getConfig() {
        return config;
    }

    public InstallOperation(InstallerConfig config) {
        super("Install Mongo cluster");
        this.config = config;
        
        Set<Agent> clusterMembers = new HashSet<Agent>();
        clusterMembers.addAll(config.getConfigServers());
        clusterMembers.addAll(config.getRouterServers());
        clusterMembers.addAll(config.getDataNodes());

        addTask(InstallTasks.getKillRunningMongoTask(clusterMembers));

        addTask(InstallTasks.getUninstallMongoTask(clusterMembers));

        addTask(InstallTasks.getCleanMongoDataTask(clusterMembers));

        addTask(InstallTasks.getAptGetUpdateTask(clusterMembers));

        addTask(InstallTasks.getInstallMongoTask(clusterMembers));

        addTask(InstallTasks.getStopMongoTask(clusterMembers));

        addTask(InstallTasks.getRegisterIpsTask(clusterMembers));

        addTask(InstallTasks.getSetReplicaSetNameTask(
                config.getReplicaSetName(),
                config.getDataNodes()));

        addTask(InstallTasks.getStartConfigServersTask(
                config.getConfigServers()));

        addTask(InstallTasks.getStartRoutersTask(
                config.getRouterServers(),
                config.getConfigServers()));

        addTask(InstallTasks.getStartReplicaSetTask(
                config.getDataNodes()));

        addTask(InstallTasks.getRegisterSecondaryNodesWithPrimaryTask(
                config.getDataNodes()));

        addTask(InstallTasks.getRegisterReplicaSetAsShardWithRouter(
                config.getReplicaSetName(),
                config.getRouterServers().iterator().next(),
                config.getDataNodes()));

    }

}
