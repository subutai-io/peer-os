package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles add accumulo config property operation
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{
    private final String propertyName;
    private final String propertyValue;


    public AddPropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName,
                                        String propertyValue )
    {
        super( manager, clusterName );
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

        trackerOperation.addLog( "Adding property..." );

        Command addPropertyCommand = manager.getCommands().getAddPropertyCommand( propertyName, propertyValue,
                accumuloClusterConfig.getAllNodes() );
        manager.getCommandRunner().runCommand( addPropertyCommand );

        if ( addPropertyCommand.hasSucceeded() )
        {
            trackerOperation.addLog( "Property added successfully\nRestarting cluster..." );

            Command restartClusterCommand =
                    manager.getCommands().getRestartCommand( accumuloClusterConfig.getMasterNode() );
            manager.getCommandRunner().runCommand( restartClusterCommand );
            if ( restartClusterCommand.hasSucceeded() )
            {
                trackerOperation.addLogDone( "Cluster restarted successfully" );
            }
            else
            {
                trackerOperation.addLogFailed(
                        String.format( "Cluster restart failed, %s", restartClusterCommand.getAllErrors() ) );
            }
        }
        else
        {
            trackerOperation
                    .addLogFailed( String.format( "Adding property failed, %s", addPropertyCommand.getAllErrors() ) );
        }
    }
}
