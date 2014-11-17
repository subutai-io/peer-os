package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class UninstallHandler extends AbstractOperationHandler<HBaseImpl, HBaseClusterConfig>
{
    private String clusterName;


    public UninstallHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
       /* HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Set<Agent> allNodes;
        try
        {
            allNodes = getAllNodes( config );
        }
        catch ( Exception e )
        {
            trackerOperation.addLogFailed( e.getMessage() );
            return;
        }

        trackerOperation.addLog( "Uninstalling..." );

        Command installCommand = manager.getCommands().getUninstallCommand( allNodes );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            trackerOperation.addLog( "Uninstallation success.." );
        }
        else
        {
            trackerOperation
                    .addLogFailed( String.format( "Uninstallation failed, %s", installCommand.getAllErrors() ) );
            return;
        }

        trackerOperation.addLog( "Updating db..." );
        manager.getPluginDAO().deleteInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName() );
        trackerOperation.addLogDone( "Cluster info deleted from DB\nDone" );*/
    }


    /*private Set<Agent> getAllNodes( HBaseClusterConfig config )
    {
        final Set<Agent> allNodes = new HashSet<>();
        allNodes.add( config.getHbaseMaster() );
        for ( Agent agent : config.getRegionServers() )
        {
            allNodes.add( agent );
        }
        for ( Agent agent : config.getQuorumPeers() )
        {
            allNodes.add( agent );
        }
        for ( Agent agent : config.getBackupMasters() )
        {
            allNodes.add( agent );
        }
        return allNodes;
    }*/
}