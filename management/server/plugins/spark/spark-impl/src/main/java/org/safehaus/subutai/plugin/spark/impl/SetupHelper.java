package org.safehaus.subutai.plugin.spark.impl;


import java.util.Set;

import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class SetupHelper
{

    private final SparkImpl manager;
    private final SparkClusterConfig config;
    private final ProductOperation po;


    public SetupHelper( SparkImpl manager, SparkClusterConfig config, ProductOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


    public void configureMasterIP( Set<Agent> agents ) throws ClusterSetupException
    {
        po.addLog( "Setting master IP..." );

        Command cmd = Commands.getSetMasterIPCommand( config.getMasterNode(), agents );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Setting master IP failed:" + cmd.getAllErrors() );
        }

        po.addLog( "Setting master IP succeeded" );
    }


    public void registerSlaves() throws ClusterSetupException
    {
        po.addLog( "Registering slave(s)..." );

        Command cmd = Commands.getAddSlavesCommand( config.getSlaveNodes(), config.getMasterNode() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Failed to register slave(s) with master: " + cmd.getAllErrors() );
        }

        po.addLog( "Slave(s) successfully registered" );
    }


    public void startCluster() throws ClusterSetupException
    {
        po.addLog( "Starting cluster..." );

        Command cmd = Commands.getStartAllCommand( config.getMasterNode() );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Cluster started successfully\nDone" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to start cluster:" + cmd.getAllErrors() );
        }
    }
}
