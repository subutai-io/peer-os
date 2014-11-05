package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import java.util.Iterator;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class CheckDecommissionStatusOperationHandler extends AbstractOperationHandler<HadoopImpl>
{


    public CheckDecommissionStatusOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking decommissioning status of %s", clusterName ) );
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

        if ( hadoopClusterConfig.getNameNode() == null )
        {
            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return;
        }

        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getAgent().getUuid().equals( hadoopClusterConfig.getNameNode().getAgent().getUuid() ) )
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
            CommandResult result = host.execute( new RequestBuilder( commands.getReportHadoopCommand() ) );
            logStatusResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }
    }


    private void logStatusResults( TrackerOperation po, CommandResult result )
    {
        if ( result.getStdOut() != null && result.getStdOut().contains( "NameNode" ) )
        {
            String[] array = result.getStdOut().split( "\n" );

            for ( String status : array )
            {
                if ( status.contains( "NameNode" ) )
                {
                    trackerOperation.addLogDone( status );
                }
            }
        }
    }
}
