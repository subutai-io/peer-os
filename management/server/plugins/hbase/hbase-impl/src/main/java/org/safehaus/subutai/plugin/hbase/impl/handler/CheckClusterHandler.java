package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class CheckClusterHandler extends AbstractOperationHandler<HBaseImpl>
{
    private String clusterName;


    public CheckClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
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
            productOperation.addLogFailed( e.getMessage() );

            return;
        }
        if ( allNodes == null || allNodes.isEmpty() )
        {
            productOperation.addLogFailed( "Nodes not connected" );
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
            productOperation.addLogDone( status.toString() );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
        }
    }


    private Set<Agent> getAllNodes( HBaseClusterConfig config )
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
    }
}
