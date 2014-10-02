package org.safehaus.subutai.plugin.pig.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;


public class DestroyClusterOperationHandler extends AbstractOperationHandler<PigImpl>
{

    public DestroyClusterOperationHandler( PigImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( PigConfig.PRODUCT_KEY,
                String.format( "Destroying %s ", clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        PigConfig config = manager.getCluster( clusterName );
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
            manager.getPluginDao().deleteInfo( PigConfig.PRODUCT_KEY, config.getClusterName() );
            po.addLogDone( "Cluster info deleted from DB\nDone" );
        }
        else
        {
            po.addLogFailed( "Failed to destroy cluster" );
        }
    }


    private boolean uninstall( PigConfig config )
    {
        ProductOperation po = productOperation;
        po.addLog( "Uninstalling Pig..." );

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


    private boolean destroyNodes( PigConfig config )
    {

        productOperation.addLog( "Destroying node(s)..." );
        try
        {
            manager.getContainerManager().clonesDestroy( config.getNodes() );
            productOperation.addLog( "Destroying node(s) completed" );
            return true;
        }
        catch ( LxcDestroyException ex )
        {
            productOperation.addLog( "Failed to destroy node(s): " + ex.getMessage() );
            return false;
        }
    }
}

