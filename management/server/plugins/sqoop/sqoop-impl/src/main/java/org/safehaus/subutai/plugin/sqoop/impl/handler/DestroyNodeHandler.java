package org.safehaus.subutai.plugin.sqoop.impl.handler;


import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.CommandFactory;
import org.safehaus.subutai.plugin.sqoop.impl.CommandType;
import org.safehaus.subutai.plugin.sqoop.impl.SqoopImpl;


public class DestroyNodeHandler extends AbstractHandler
{

    public DestroyNodeHandler( SqoopImpl manager, String clusterName, TrackerOperation po )
    {
        super( manager, clusterName, po );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        SqoopConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( "Sqoop installation not found: " + clusterName );
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            po.addLogFailed( "Node is not connected" );
            return;
        }
        if ( !config.getNodes().contains( agent ) )
        {
            po.addLogFailed( "Node does not belong to Sqoop installation group" );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            po.addLogFailed(
                    "This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        String s = CommandFactory.build( CommandType.PURGE, null );
        Command cmd = manager.getCommandRunner()
                             .createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( agent ) ) );

        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Sqoop successfully removed from " + hostname );
            config.getNodes().remove( agent );
            po.addLog( "Updating db..." );

            manager.getPluginDao().saveInfo( SqoopConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLogDone( "Cluster info updated in DB\nDone" );
        }
        else
        {
            po.addLog( cmd.getAllErrors() );
            po.addLogFailed( "Failed to remove Sqoop from node" );
        }
    }


    private void destroyNodes( SqoopConfig config )
    {
        if ( config.getHadoopNodes() == null || config.getHadoopNodes().isEmpty() )
        {
            return;
        }

        trackerOperation.addLog( "Destroying nodes..." );
        try
        {
            manager.getContainerManager().clonesDestroy( config.getHadoopNodes() );
            manager.getLogger().info( "Destroyed {} node(s)", config.getHadoopNodes().size() );
            trackerOperation.addLog( "Nodes successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            String m = "Failed to destroy node(s)";
            trackerOperation.addLog( m + ": " + ex.getMessage() );
            manager.getLogger().error( m, ex );
        }
    }
}
