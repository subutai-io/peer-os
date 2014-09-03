package org.safehaus.subutai.plugin.accumulo.impl.handler;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import java.util.UUID;


/**
 * Handles add accumulo config property operation
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final String propertyName;
    private final String propertyValue;


    public AddPropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName,
                                        String propertyValue ) {
        super( manager, clusterName );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyValue ), "Property Value is null or empty" );
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Adding property %s=%s", propertyName, propertyValue ) );

    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );

        if ( accumuloClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Adding property..." );

        Command addPropertyCommand =
                Commands.getAddPropertyCommand( propertyName, propertyValue, accumuloClusterConfig.getAllNodes() );
        manager.getCommandRunner().runCommand( addPropertyCommand );

        if ( addPropertyCommand.hasSucceeded() ) {
            productOperation.addLog( "Property added successfully\nRestarting cluster..." );

            Command restartClusterCommand = Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
            manager.getCommandRunner().runCommand( restartClusterCommand );
            if ( restartClusterCommand.hasSucceeded() ) {
                productOperation.addLogDone( "Cluster restarted successfully" );
            }
            else {
                productOperation.addLogFailed( String.format( "Cluster restart failed, %s", restartClusterCommand.getAllErrors() ) );
            }
        }
        else {
            productOperation.addLogFailed( String.format( "Adding property failed, %s", addPropertyCommand.getAllErrors() ) );
        }
    }
}
