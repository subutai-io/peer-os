package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by bahadyr on 8/25/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<HBaseImpl>
{

    private ProductOperation po;
    //    private HBaseConfig config;
    private String clusterName;


    public UninstallOperationHandler( final HBaseImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                HBaseConfig config =
                        manager.getDbManager().getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Set<Agent> allNodes;
                try {
                    allNodes = getAllNodes( config );
                }
                catch ( Exception e ) {
                    po.addLogFailed( e.getMessage() );
                    return;
                }

                po.addLog( "Uninstalling..." );

                Command installCommand = Commands.getUninstallCommand( allNodes );
                manager.getCommandRunner().runCommand( installCommand );

                if ( installCommand.hasSucceeded() ) {
                    po.addLog( "Uninstallation success.." );
                }
                else {
                    po.addLogFailed( String.format( "Uninstallation failed, %s", installCommand.getAllErrors() ) );
                    return;
                }

                po.addLog( "Updating db..." );
                if ( manager.getDbManager().deleteInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                    po.addLogDone( "Cluster info deleted from DB\nDone" );
                }
                else {
                    po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
                }
            }
        } );
    }


    private Set<Agent> getAllNodes( HBaseConfig config ) throws Exception {
        final Set<Agent> allNodes = new HashSet<>();

        if ( manager.getAgentManager().getAgentByHostname( config.getMaster() ) == null ) {
            throw new Exception( String.format( "Master node %s not connected", config.getMaster() ) );
        }
        allNodes.add( manager.getAgentManager().getAgentByHostname( config.getMaster() ) );
        if ( manager.getAgentManager().getAgentByHostname( config.getBackupMasters() ) == null ) {
            throw new Exception( String.format( "Backup master node %s not connected", config.getBackupMasters() ) );
        }
        allNodes.add( manager.getAgentManager().getAgentByHostname( config.getBackupMasters() ) );

        for ( String hostname : config.getRegion() ) {
            if ( manager.getAgentManager().getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Region server node %s not connected", hostname ) );
            }
            allNodes.add( manager.getAgentManager().getAgentByHostname( hostname ) );
        }

        for ( String hostname : config.getQuorum() ) {
            if ( manager.getAgentManager().getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Quorum node %s not connected", hostname ) );
            }
            allNodes.add( manager.getAgentManager().getAgentByHostname( hostname ) );
        }

        return allNodes;
    }
}