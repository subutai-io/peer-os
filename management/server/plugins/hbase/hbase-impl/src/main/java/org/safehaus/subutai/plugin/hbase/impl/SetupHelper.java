package org.safehaus.subutai.plugin.hbase.impl;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;

import com.google.common.collect.Sets;


public class SetupHelper
{

    private final HBaseImpl manager;
    private final HBaseClusterConfig config;
    private final TrackerOperation po;


    public SetupHelper( HBaseImpl manager, HBaseClusterConfig config, TrackerOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


    public void configureHMaster() throws ClusterSetupException
    {
        /*po.addLog( "Setting master" );
        UUID hmaster = config.getHbaseMaster();
        Command cmd = manager.getCommands().getConfigMasterCommand( config.getAllNodes(), config.getHadoopNameNode(),
                hmaster.getHostname() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Setting regions servers:" + cmd.getAllErrors() );
        }

        po.addLog( "Setting regions servers succeeded" );*/
    }


    public void configureRegionServers() throws ClusterSetupException
    {
       /* po.addLog( "Setting regions servers" );
        StringBuilder sb = new StringBuilder();
        for ( UUID agent : config.getRegionServers() )
        {
            sb.append( agent.getHostname() );
            sb.append( " " );
        }

        Command cmd = manager.getCommands().getConfigRegionCommand( config.getAllNodes(), sb.toString().trim() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Setting regions servers:" + cmd.getAllErrors() );
        }

        po.addLog( "Setting regions servers succeeded" );*/
    }


    public void configureQuorumPeers() throws ClusterSetupException
    {
       /* po.addLog( "Setting quorum peers" );
        StringBuilder sb = new StringBuilder();
        for ( UUID agent : config.getQuorumPeers() )
        {
            sb.append( agent.getHostname() );
            sb.append( " " );
        }

        Command cmd = manager.getCommands().getConfigQuorumCommand( config.getAllNodes(), sb.toString().trim() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Setting quorum peers:" + cmd.getAllErrors() );
        }

        po.addLog( "Setting quorum peers succeeded" );*/
    }


    public void configureBackUpMasters() throws ClusterSetupException
    {
        /*po.addLog( "Setting backup masters" );
        StringBuilder sb = new StringBuilder();
        for ( Agent agent : config.getBackupMasters() )
        {
            sb.append( agent.getHostname() );
            sb.append( " " );
        }

        Command cmd = manager.getCommands().getConfigBackupMastersCommand( config.getAllNodes(), sb.toString().trim() );
        manager.getCommandRunner().runCommand( cmd );

        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Setting backup masters:" + cmd.getAllErrors() );
        }

        po.addLog( "Setting backup masters succeeded" );*/
    }


    public void startCluster() throws ClusterSetupException
    {
       /* po.addLog( "Starting cluster..." );

        Command cmd = manager.getCommands().getStartCommand( Sets.newHashSet( config.getHbaseMaster() ) );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Cluster started successfully\nDone" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to start cluster:" + cmd.getAllErrors() );
        }*/
    }
}


