package org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;

import java.util.Iterator;


public class StatusTaskTrackerOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String lxcHostName;


    public StatusTaskTrackerOperationHandler( HadoopImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostName = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking TaskTracker in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        Commands commands = new Commands( hadoopClusterConfig );

        if ( hadoopClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getAgent().getUuid().equals( hadoopClusterConfig.getJobTracker().getAgent().getUuid() ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", host.getId() ) );
            return;
        }

        try
        {
            CommandResult result = host.execute( new RequestBuilder( commands.getStatusJobTrackerCommand() ) );
            logStatusResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }
    }

    private void logStatusResults( TrackerOperation po, CommandResult result )
    {
        if ( result.getStdOut() != null && result.getStdOut().contains( "TaskTracker" ) )
        {
            String[] array = result.getStdOut().split( "\n" );

            for ( String status : array )
            {
                if ( status.contains( "TaskTracker" ) )
                {
                    trackerOperation.addLogDone( status );
                }
            }
        }
    }
}