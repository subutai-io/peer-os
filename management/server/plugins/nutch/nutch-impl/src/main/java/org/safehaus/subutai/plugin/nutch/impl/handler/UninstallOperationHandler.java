package org.safehaus.subutai.plugin.nutch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.safehaus.subutai.plugin.nutch.api.SetupType;
import org.safehaus.subutai.plugin.nutch.impl.NutchImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<NutchImpl>
{


    public UninstallOperationHandler( NutchImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( NutchConfig.PRODUCT_KEY,
                String.format( "Destroying %s ", clusterName ) );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        NutchConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        boolean ok = false;
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            ok = uninstall( config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            ok = destroyNodes( config );
        }
        else
        {
            po.addLog( "Undefined setup type" );
        }

        if ( ok )
        {
            po.addLog( "Updating db..." );
            manager.getPluginDao().deleteInfo( NutchConfig.PRODUCT_KEY, config.getClusterName() );
            po.addLogDone( "Cluster info deleted from DB\nDone" );
        }
        else
        {
            po.addLogFailed( "Failed to destroy cluster" );
        }
    }


    private boolean uninstall( NutchConfig config )
    {
        TrackerOperation po = trackerOperation;
        po.addLog( "Uninstalling " + NutchConfig.PRODUCT_KEY );

        Command cmd = manager.getCommands().getUninstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            return true;
        }

        po.addLog( cmd.getAllErrors() );
        po.addLogFailed( "Uninstallation failed" );
        return false;
    }


    private boolean destroyNodes( NutchConfig config )
    {

        trackerOperation.addLog( "Destroying node(s)..." );
        try
        {
            manager.getContainerManager().clonesDestroy( config.getNodes() );
            trackerOperation.addLog( "Destroying node(s) completed" );
            return true;
        }
        catch ( LxcDestroyException ex )
        {
            trackerOperation.addLog( "Failed to destroy node(s): " + ex.getMessage() );
            return false;
        }
    }
}
