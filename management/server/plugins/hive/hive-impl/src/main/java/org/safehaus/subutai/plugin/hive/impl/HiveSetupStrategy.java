package org.safehaus.subutai.plugin.hive.impl;


import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;


abstract class HiveSetupStrategy implements ClusterSetupStrategy
{

    final HiveImpl manager;
    final HiveConfig config;
    final ProductOperation po;


    public HiveSetupStrategy( HiveImpl manager, HiveConfig config, ProductOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


    public void checkConfig() throws ClusterSetupException
    {

        String m = "Invalid configuration: ";

        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "name is not specified" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "Sqoop installation already exists: %s", config.getClusterName() ) );
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            if ( config.getServer() == null )
            {
                throw new ClusterSetupException( m + "Server node not specified" );
            }
            if ( config.getClients() == null || config.getClients().isEmpty() )
            {
                throw new ClusterSetupException( m + "Target nodes not specified" );
            }
        }
    }


    void configureServer() throws ClusterSetupException
    {
        po.addLog( "Configuring server..." );
        String s = Commands.configureHiveServer( config.getServer().getListIP().get( 0 ) );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ),
                new HashSet<>( Arrays.asList( config.getServer() ) ) );
        manager.getCommandRunner().runCommand( cmd );
        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Server successfully configured" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to configure Hive server: " + cmd.getAllErrors() );
        }
    }


    void configureClients() throws ClusterSetupException
    {
        po.addLog( "Configuring clients..." );
        String s = Commands.configureClient( config.getServer() );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ), config.getClients() );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Clients successfully configured" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to configure clients: " + cmd.getAllErrors() );
        }
    }
}
