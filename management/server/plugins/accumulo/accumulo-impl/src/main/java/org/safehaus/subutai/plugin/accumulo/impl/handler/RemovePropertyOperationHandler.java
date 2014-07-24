package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;

import com.google.common.base.Strings;


/**
 * Created by dilshat on 5/6/14.
 */
public class RemovePropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;
    private final String propertyName;


    public RemovePropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName ) {
        super( manager, clusterName );
        this.propertyName = propertyName;
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Removing property %s", propertyName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( clusterName ) || Strings.isNullOrEmpty( propertyName ) ) {
            po.addLogFailed( "Malformed arguments\nOperation aborted" );
            return;
        }
        final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        po.addLog( "Removing property..." );

        Command removePropertyCommand = Commands.getRemovePropertyCommand( propertyName, accumuloClusterConfig.getAllNodes() );
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
