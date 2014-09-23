package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class UninstallHandler extends AbstractOperationHandler<HBaseImpl>
{
    private String clusterName;


    public UninstallHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
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
            productOperation.addLogFailed( e.getMessage() );
            return;
        }

        productOperation.addLog( "Uninstalling..." );

        Command installCommand = Commands.getUninstallCommand( allNodes );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            productOperation.addLog( "Uninstallation success.." );
        }
        else
        {
            productOperation
                    .addLogFailed( String.format( "Uninstallation failed, %s", installCommand.getAllErrors() ) );
            return;
        }

        productOperation.addLog( "Updating db..." );
        manager.getPluginDAO().deleteInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName() );
        productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
    }


    private Set<Agent> getAllNodes( HBaseClusterConfig config ) throws Exception
    {
        final Set<Agent> allNodes = new HashSet<>();

        if ( config.getHbaseMaster() == null )
        {
            throw new Exception( String.format( "Master node %s not connected", config.getHbaseMaster() ) );
        }
        allNodes.add( config.getHbaseMaster() );

        for ( Agent agent : config.getRegionServers() )
        {
            if ( agent == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent );
        }

        for ( Agent agent : config.getQuorumPeers() )
        {
            if ( agent == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent );
        }

        for ( Agent agent : config.getBackupMasters() )
        {
            if ( agent == null )
            {
                throw new Exception( String.format( "Region server node %s not connected", agent ) );
            }
            allNodes.add( agent );
        }

        return allNodes;
    }
}