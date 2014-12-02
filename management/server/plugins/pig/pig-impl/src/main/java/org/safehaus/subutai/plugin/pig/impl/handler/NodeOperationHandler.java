package org.safehaus.subutai.plugin.pig.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.plugin.pig.impl.Commands;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;


/**
 * Created by ebru on 06.11.2014.
 */
public class NodeOperationHandler extends AbstractOperationHandler<PigImpl, PigConfig>
{
    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;

    public NodeOperationHandler( final PigImpl manager, String clusterName, final String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( PigConfig.PRODUCT_KEY,
                                               String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        PigConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainerHosts().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }


            switch ( operationType )
            {
                case INSTALL:
                    installProductOnNode( host );
                    break;
                case UNINSTALL:
                    uninstallProductOnNode( host );
                    break;
            }



    }
    private CommandResult installProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.installCommand ).withTimeout( 600 ) );
            if ( result.hasSucceeded() )
            {
                config.getNodes().add( host.getId() );
                manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLogDone(
                        PigConfig.PRODUCT_KEY + " is installed on node " + host.getHostname() + " successfully." );
            }
            else
            {
                trackerOperation.addLogFailed( "Could not install " + PigConfig.PRODUCT_KEY + " to node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }


    private CommandResult uninstallProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.uninstallCommand ).withTimeout( 600 ) );
            if ( result.hasSucceeded() )
            {
                config.getNodes().remove( host.getId() );
                manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLogDone(
                        PigConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname() + " successfully." );
            }
            else
            {
                trackerOperation
                        .addLogFailed( "Could not uninstall " + PigConfig.PRODUCT_KEY + " from node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }

}
