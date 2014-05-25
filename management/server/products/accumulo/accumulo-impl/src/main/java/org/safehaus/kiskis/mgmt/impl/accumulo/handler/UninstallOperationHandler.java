package org.safehaus.kiskis.mgmt.impl.accumulo.handler;


import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.accumulo.AccumuloImpl;
import org.safehaus.kiskis.mgmt.impl.accumulo.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;


/**
 * Created by dilshat on 5/6/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;


    public UninstallOperationHandler( AccumuloImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Uninstalling cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        po.addLog( "Uninstalling cluster..." );

        Command uninstallCommand = Commands.getUninstallCommand( config.getAllNodes() );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            if ( uninstallCommand.hasSucceeded() ) {
                po.addLog( "Cluster successfully uninstalled" );
            }
            else {
                po.addLog( String.format( "Uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors() ) );
            }

            po.addLog( "Removing Accumulo from HDFS..." );

            Command removeAccumuloFromHDFSCommand = Commands.getRemoveAccumuloFromHFDSCommand( config.getMasterNode() );
            manager.getCommandRunner().runCommand( removeAccumuloFromHDFSCommand );

            if ( removeAccumuloFromHDFSCommand.hasSucceeded() ) {
                po.addLog( "Accumulo successfully removed from HDFS" );
            }
            else {
                po.addLog( String.format( "Removing Accumulo from HDFS failed, %s, skipping...",
                        removeAccumuloFromHDFSCommand.getAllErrors() ) );
            }

            po.addLog( "Updating db..." );
            if ( manager.getDbManager().deleteInfo( Config.PRODUCT_KEY, config.getClusterName() ) ) {
                po.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            else {
                po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
        }
        else {
            po.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
