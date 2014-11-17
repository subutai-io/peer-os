package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class CheckClusterHandler extends AbstractOperationHandler<HBaseImpl, HBaseClusterConfig>
{
    private String clusterName;


    public CheckClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
       /* HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
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
        if ( allNodes == null || allNodes.isEmpty() )
        {
            trackerOperation.addLogFailed( "Nodes not connected" );
            return;
        }

        Command checkCommand = manager.getCommands().getStatusCommand( allNodes );
        manager.getCommandRunner().runCommand( checkCommand );

        if ( checkCommand.hasSucceeded() )
        {
            StringBuilder status = new StringBuilder();
            for ( Agent agent : allNodes )
            {
                status.append( agent.getHostname() ).append( ":\n" )
                      .append( checkCommand.getResults().get( agent.getUuid() ).getStdOut() ).append( "\n\n" );
            }
            trackerOperation.addLogDone( status.toString() );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
        }*/
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
