package org.safehaus.subutai.plugin.spark.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterConfigurationInterface;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.collect.Sets;


/**
 * Configures Spark cluster
 */
public class ClusterConfiguration implements ClusterConfigurationInterface<SparkClusterConfig>
{
    private final SparkImpl manager;
    private final TrackerOperation po;


    public ClusterConfiguration( SparkImpl manager, TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
    }


    @Override
    public void configureCluster( final SparkClusterConfig config, final Environment environment )
            throws ClusterConfigurationException
    {
        final ContainerHost master = environment.getContainerHostByUUID( config.getMasterNodeId() );
        final Set<ContainerHost> slaves = environment.getHostsByIds( config.getSlaveIds() );

        //configure master IP
        po.addLog( "Setting master IP..." );

        RequestBuilder setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( master.getHostname() );
        for ( ContainerHost host : slaves )
        {
            executeCommand( host, setMasterIPCommand );
        }
        po.addLog( "Setting master IP succeeded" );

        //register slaves
        po.addLog( "Registering slave(s)..." );

        Set<String> slaveHostnames = Sets.newHashSet();

        for ( ContainerHost host : slaves )
        {
            slaveHostnames.add( host.getHostname() );
        }

        RequestBuilder addSlavesCommand = manager.getCommands().getAddSlavesCommand( slaveHostnames );

        executeCommand( master, addSlavesCommand );

        po.addLog( "Slave(s) successfully registered" );

        //start cluster
        po.addLog( "Starting cluster..." );

        RequestBuilder startAllCommand = manager.getCommands().getStartAllCommand();

        CommandResult result = executeCommand( master, startAllCommand );

        if ( !result.getStdOut().contains( "starting" ) )
        {
            po.addLog( "Failed to start cluster, skipping..." );
        }
        else
        {
            po.addLog( "Cluster successfully started" );
        }
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command )
            throws ClusterConfigurationException
    {

        CommandResult result;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            throw new ClusterConfigurationException( e );
        }
        if ( !result.hasSucceeded() )
        {
            throw new ClusterConfigurationException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
        return result;
    }
}
