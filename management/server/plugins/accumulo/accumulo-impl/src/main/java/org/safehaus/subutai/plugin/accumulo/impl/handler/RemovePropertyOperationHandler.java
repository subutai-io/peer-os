package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles remove accumulo config property operation
 */
public class RemovePropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{
    private final String propertyName;


    public RemovePropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName )
    {
        super( manager, clusterName );
        this.propertyName = propertyName;
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Removing property %s", propertyName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {

        final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Removing property..." );

        Command removePropertyCommand =
                Commands.getRemovePropertyCommand( propertyName, accumuloClusterConfig.getAllNodes() );
        manager.getCommandRunner().runCommand( removePropertyCommand );

        if ( removePropertyCommand.hasSucceeded() )
        {
            productOperation.addLog( "Property removed successfully\nRestarting cluster..." );

            Command restartClusterCommand = Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
            manager.getCommandRunner().runCommand( restartClusterCommand );
            if ( restartClusterCommand.hasSucceeded() )
            {
                productOperation.addLogDone( "Cluster restarted successfully" );
            }
            else
            {
                productOperation.addLogFailed(
                        String.format( "Cluster restart failed, %s", restartClusterCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Removing property failed, %s", removePropertyCommand.getAllErrors() ) );
        }
    }
}
