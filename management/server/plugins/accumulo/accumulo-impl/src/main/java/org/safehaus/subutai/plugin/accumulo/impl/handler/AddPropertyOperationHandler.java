package org.safehaus.subutai.plugin.accumulo.impl.handler;//package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles add accumulo config property operation
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl, AccumuloClusterConfig>
{
    private final String propertyName;
    private final String propertyValue;


    public AddPropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName,
                                        String propertyValue )
    {
        super( manager, manager.getCluster( clusterName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyValue ), "Property Value is null or empty" );
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Adding property %s=%s", propertyName, propertyValue ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );

        if ( accumuloClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        CommandResult result = null;
        boolean allSuccess = true;
        for ( ContainerHost containerHost : environment.getContainerHostsByIds( accumuloClusterConfig.getAllNodes() ) )
        {
            try
            {
                result = containerHost
                        .execute( new RequestBuilder( Commands.getAddPropertyCommand( propertyName, propertyValue ) ) );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( "Property added successfully to node " + containerHost.getHostname() );
                }
                else
                {
                    allSuccess = false;
                }
            }
            catch ( CommandException e )
            {
                allSuccess = false;
                trackerOperation.addLogFailed( String.format( "Adding property failed, %s", result.getStdErr() ) );
                e.printStackTrace();
            }
        }
        if ( allSuccess )
        {
            trackerOperation.addLog( "Restarting cluster... " );
            ContainerHost master = environment.getContainerHostById( accumuloClusterConfig.getMasterNode() );
            try
            {
                master.execute( new RequestBuilder( Commands.stopCommand ) );
                master.execute( new RequestBuilder( Commands.startCommand ) );
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
                trackerOperation.addLogFailed( String.format( "Accumulo cluster restart operation failed !!!" ) );
            }
        }
        trackerOperation.addLogDone( "Done" );
    }
}
