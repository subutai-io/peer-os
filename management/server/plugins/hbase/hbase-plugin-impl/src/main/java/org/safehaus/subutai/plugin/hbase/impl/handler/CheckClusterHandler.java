package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import java.util.HashSet;
import java.util.Set;


public class CheckClusterHandler extends AbstractOperationHandler<HBaseImpl>
{

    private ProductOperation po;
    private String clusterName;


    public CheckClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        final ProductOperation po = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Checking cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                HBaseClusterConfig config = manager.getDbManager().getInfo( HBaseClusterConfig.PRODUCT_KEY, clusterName,
                        HBaseClusterConfig.class );
                if ( config == null )
                {
                    po.addLogFailed(
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
                    po.addLogFailed( e.getMessage() );
                    return;
                }
                if ( allNodes == null || allNodes.isEmpty() )
                {
                    po.addLogFailed( "Nodes not connected" );
                    return;
                }

                Command checkCommand = Commands.getStatusCommand( allNodes );
                manager.getCommandRunner().runCommand( checkCommand );

                if ( checkCommand.hasSucceeded() )
                {
                    StringBuilder status = new StringBuilder();
                    for ( Agent agent : allNodes )
                    {
                        status.append( agent.getHostname() ).append( ":\n" )
                              .append( checkCommand.getResults().get( agent.getUuid() ).getStdOut() ).append( "\n\n" );
                    }
                    po.addLogDone( status.toString() );
                }
                else
                {
                    po.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
                }
            }
        } );
    }


    private Set<Agent> getAllNodes( HBaseClusterConfig config ) throws Exception
    {
        final Set<Agent> allNodes = new HashSet<>();

        if ( config.getHbaseMaster() == null )
        {
            throw new Exception( String.format( "Master node %s not connected", config.getHbaseMaster() ) );
        }
        allNodes.add(  config.getHbaseMaster()  );

        for ( Agent agent : config.getRegionServers() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        for ( Agent agent : config.getQuorumPeers() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        for ( Agent agent : config.getBackupMasters() )
        {
            if (  agent  == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent  );
        }

        return allNodes;
    }
}
