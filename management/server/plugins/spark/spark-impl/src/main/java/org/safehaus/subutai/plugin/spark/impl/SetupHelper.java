package org.safehaus.subutai.plugin.spark.impl;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

class SetupHelper {

    private final SparkImpl manager;
    private final SparkClusterConfig config;
    private final ProductOperation po;

    public SetupHelper(SparkImpl manager, SparkClusterConfig config, ProductOperation po) {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }

    void configureMaster() throws ClusterSetupException {
        po.addLog("Setting master IP...");

        Command cmd = Commands.getSetMasterIPCommand(config.getMasterNode(),
                config.getAllNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(!cmd.hasSucceeded())
            throw new ClusterSetupException("Setting master IP failed:" + cmd.getAllErrors());

        po.addLog("Setting master IP succeeded");
    }

    void registerSlaves() throws ClusterSetupException {
        po.addLog("Registering slaves...");

        Command cmd = Commands.getAddSlavesCommand(config.getSlaveNodes(),
                config.getMasterNode());
        manager.getCommandRunner().runCommand(cmd);

        if(!cmd.hasSucceeded())
            throw new ClusterSetupException("Failed to register slaves with master: "
                    + cmd.getAllErrors());

        po.addLog("Slaves successfully registered");
    }

    void startCluster() throws ClusterSetupException {
        po.addLog("Starting cluster...");

        Command cmd = Commands.getStartAllCommand(config.getMasterNode());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            po.addLog("Cluster started successfully\nDone");
        else
            throw new ClusterSetupException("Failed to start cluster:" + cmd.getAllErrors());

    }
}
