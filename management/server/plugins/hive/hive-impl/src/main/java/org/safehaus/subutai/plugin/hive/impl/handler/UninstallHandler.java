package org.safehaus.subutai.plugin.hive.impl.handler;


import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.impl.CommandType;
import org.safehaus.subutai.plugin.hive.impl.Commands;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
import org.safehaus.subutai.plugin.hive.impl.Product;


public class UninstallHandler extends AbstractHandler
{

    public UninstallHandler( HiveImpl manager, String clusterName )
    {
        super( manager, clusterName );
        this.productOperation = manager.getTracker().createProductOperation( HiveConfig.PRODUCT_KEY,
                "Uninstalling cluster " + clusterName );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        HiveConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster '%s' does not exist", clusterName ) );
            return;
        }

        // check server node
        if ( !isNodeConnected( config.getServer().getHostname() ) )
        {
            po.addLogFailed( String.format( "Server node '%s' is not connected", config.getServer().getHostname() ) );
            return;
        }
        // check client nodes
        for ( Agent a : config.getClients() )
        {
            if ( !isNodeConnected( a.getHostname() ) )
            {
                po.addLogFailed( "Not all nodes are connected" );
                return;
            }
        }

        boolean ok = false;
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            ok = removeHive( config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            ok = destroyNodes( config );
        }
        else
        {
            po.addLog( "Unsupported setup type: " + config.getSetupType() );
        }

        if ( ok )
        {
            po.addLog( "Updating DB..." );
            manager.getPluginDao().deleteInfo( HiveConfig.PRODUCT_KEY, clusterName );
            po.addLogDone( "Cluster info deleted from DB" );
        }
        else
        {
            po.addLogFailed( null );
        }
    }


    private boolean removeHive( HiveConfig config )
    {
        ProductOperation po = productOperation;
        po.addLog( "Removing Hive client(s)..." );
        String s = Commands.make( CommandType.PURGE, Product.HIVE );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ), config.getClients() );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasCompleted() )
        {
            for ( Agent agent : config.getClients() )
            {
                AgentResult res = cmd.getResults().get( agent.getUuid() );
                if ( isZero( res.getExitCode() ) )
                {
                    po.addLog( "Hive removed from node " + agent.getHostname() );
                }
                else
                {
                    po.addLog( String.format( "Failed to remove Hive on '%s': %s", agent.getHostname(),
                            res.getStdErr() ) );
                    return false;
                }
            }
        }
        else
        {
            po.addLog( "Failed to remove client(s): " + cmd.getAllErrors() );
            return false;
        }

        // remove products from server node
        po.addLog( "Removing Hive from server node..." );
        for ( Product p : new Product[] { Product.HIVE, Product.DERBY } )
        {
            s = Commands.make( CommandType.PURGE, p );
            cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ),
                    new HashSet<>( Arrays.asList( config.getServer() ) ) );
            manager.getCommandRunner().runCommand( cmd );

            if ( cmd.hasSucceeded() )
            {
                po.addLog( p + " removed from server node" );
            }
            else
            {
                po.addLog( "Failed to remove Hive from server" );
                return false;
            }
        }
        return true;
    }


    private boolean destroyNodes( HiveConfig config )
    {
        ProductOperation po = productOperation;
        po.addLog( "Destroying container(s)..." );
        try
        {
            manager.getContainerManager().clonesDestroy( config.getHadoopNodes() );
            po.addLog( "Container(s) successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            String m = "Failed to destroy container(s)";
            po.addLog( m );
        }
        return true;
    }
}
