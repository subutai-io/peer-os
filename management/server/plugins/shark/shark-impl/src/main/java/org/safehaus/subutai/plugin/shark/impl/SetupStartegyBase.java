package org.safehaus.subutai.plugin.shark.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class SetupStartegyBase
{
    final SharkImpl manager;
    final SharkClusterConfig config;
    final TrackerOperation po;


    public SetupStartegyBase( SharkImpl manager, SharkClusterConfig config, TrackerOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


    void checkConfig() throws ClusterSetupException
    {
        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( "Malformed configuration. Installation aborted" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists. Installation aborted",
                            config.getClusterName() ) );
        }
    }


    SparkClusterConfig checkAndGetSparkConfig() throws ClusterSetupException
    {
        if ( config.getSparkClusterName() == null || config.getSparkClusterName().isEmpty() )
        {
            throw new ClusterSetupException( "Spark cluster not specified" );
        }
        SparkClusterConfig spark = manager.getSparkManager().getCluster( config.getSparkClusterName() );
        if ( spark == null )
        {
            throw new ClusterSetupException( "Spark cluster not found: " + config.getSparkClusterName() );
        }
        return spark;
    }


    void checkConnected() throws ClusterSetupException
    {
        if ( config.getNodes() == null )
        {
            return;
        }
        for ( Agent a : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException(
                        String.format( "Node %s is not connected. Installation aborted", a.getHostname() ) );
            }
        }
    }


    void setupMasterIp( Agent sparkMaster ) throws ClusterSetupException
    {
        po.addLog( "Setting Master IP..." );

        Command cmd = Commands.getSetMasterIPCommand( config.getNodes(), sparkMaster );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Master IP successfully set." );
        }
        else
        {
            throw new ClusterSetupException( "Failed to set Master IP: " + cmd.getAllErrors() );
        }
    }
}

