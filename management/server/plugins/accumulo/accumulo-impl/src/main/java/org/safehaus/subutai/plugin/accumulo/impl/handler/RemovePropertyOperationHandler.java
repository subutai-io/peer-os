package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles remove accumulo config property operation
 */
public class RemovePropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;
    private final String propertyName;


    public RemovePropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName ) {
        super( manager, clusterName );
        this.propertyName = propertyName;
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Removing property %s", propertyName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {

        final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        po.addLog( "Removing property..." );

        Command removePropertyCommand =
                Commands.getRemovePropertyCommand( propertyName, accumuloClusterConfig.getAllNodes() );
        manager.getCommandRunner().runCommand( removePropertyCommand );

        if ( removePropertyCommand.hasSucceeded() ) {
            po.addLog( "Property removed successfully\nRestarting cluster..." );

            Command restartClusterCommand = Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
            manager.getCommandRunner().runCommand( restartClusterCommand );
            if ( restartClusterCommand.hasSucceeded() ) {
                po.addLogDone( "Cluster restarted successfully" );
            }
            else {
                po.addLogFailed( String.format( "Cluster restart failed, %s", restartClusterCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed( String.format( "Removing property failed, %s", removePropertyCommand.getAllErrors() ) );
        }
    }
}
