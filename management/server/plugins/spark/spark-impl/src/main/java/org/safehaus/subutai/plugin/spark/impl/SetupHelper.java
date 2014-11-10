package org.safehaus.subutai.plugin.spark.impl;


import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.collect.Sets;


public class SetupHelper
{

    private final SparkImpl manager;

    private final TrackerOperation po;
    private ContainerHost master;
    private Set<ContainerHost> slaves;


    public SetupHelper( SparkImpl manager, SparkClusterConfig config, Environment environment, TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
        master = environment.getContainerHostByUUID( config.getMasterNodeId() );
        slaves = environment.getHostsByIds( config.getSlaveIds() );
    }


    public void configureMasterIP() throws ClusterSetupException
    {
        po.addLog( "Setting master IP..." );

        RequestBuilder request = manager.getCommands().getSetMasterIPCommand( master.getHostname() );
        for ( ContainerHost host : slaves )
        {

            try
            {
                processResult( host, host.execute( request ) );
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException( e );
            }
        }

        po.addLog( "Setting master IP succeeded" );
    }


    public void registerSlaves() throws ClusterSetupException
    {
        po.addLog( "Registering slave(s)..." );

        Set<String> slaveHostnames = Sets.newHashSet();

        for ( ContainerHost host : slaves )
        {
            slaveHostnames.add( host.getHostname() );
        }

        RequestBuilder request = manager.getCommands().getAddSlavesCommand( slaveHostnames );

        try
        {
            processResult( master, master.execute( request ) );
        }
        catch ( CommandException e )
        {
            throw new ClusterSetupException( e );
        }

        po.addLog( "Slave(s) successfully registered" );
    }


    public void startCluster() throws ClusterSetupException
    {
        po.addLog( "Starting cluster..." );

        RequestBuilder request = manager.getCommands().getStartAllCommand();

        try
        {
            processResult( master, master.execute( request ) );
        }
        catch ( CommandException e )
        {
            throw new ClusterSetupException( e );
        }

        po.addLog( "Cluster successfully started" );
    }


    public void processResult( ContainerHost host, CommandResult result ) throws ClusterSetupException
    {

        if ( !result.hasSucceeded() )
        {
            throw new ClusterSetupException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
    }
}
