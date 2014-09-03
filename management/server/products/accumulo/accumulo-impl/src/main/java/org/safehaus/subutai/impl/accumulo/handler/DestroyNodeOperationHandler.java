package org.safehaus.subutai.impl.accumulo.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.accumulo.NodeType;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.impl.accumulo.AccumuloImpl;
import org.safehaus.subutai.impl.accumulo.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{
    private final ProductOperation po;
    private final String lxcHostname;
    private final NodeType nodeType;


    public DestroyNodeOperationHandler( AccumuloImpl manager, String clusterName, String lxcHostname,
        NodeType nodeType )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.nodeType = nodeType;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Destroying %s on %s", nodeType, lxcHostname ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        if ( !( nodeType == NodeType.TRACER || nodeType.isSlave() ) )
        {
            po.addLogFailed( "Only tracer or slave node can be destroyed" );
            return;
        }

        final Config config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getAllNodes().contains( agent ) )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }
        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            po.addLogFailed( String.format( "Master node %s is not connected", config.getMasterNode().getHostname() ) );
            return;
        }

        if ( nodeType == NodeType.TRACER )
        {
            if ( config.getTracers().size() == 1 )
            {
                po.addLogFailed( "This is the last tracer in the cluster, destroy cluster instead" );
                return;
            }
            config.getTracers().remove( agent );
        }
        else
        {
            if ( config.getSlaves().size() == 1 )
            {
                po.addLogFailed( "This is the last slave in the cluster, destroy cluster instead" );
                return;
            }
            config.getSlaves().remove( agent );
        }

        boolean uninstall = !config.getAllNodes().contains( agent );

        if ( uninstall )
        {
            po.addLog( "Uninstalling Accumulo..." );

            Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasSucceeded() )
            {
                po.addLog( "Accumulo uninstallation succeeded" );
            }
            else
            {
                po.addLog( String.format( "Accumulo uninstallation failed, %s, skipping...",
                    uninstallCommand.getAllErrors() ) );
            }
        }

        Command unregisterNodeCommand;
        if ( nodeType == NodeType.TRACER )
        {
            unregisterNodeCommand = Commands.getClearTracerCommand( config.getAllNodes(), agent );
        }
        else
        {
            unregisterNodeCommand = Commands.getClearSlaveCommand( config.getAllNodes(), agent );
        }

        po.addLog( "Unregistering node from cluster..." );
        manager.getCommandRunner().runCommand( unregisterNodeCommand );

        if ( unregisterNodeCommand.hasSucceeded() )
        {
            po.addLog( "Node unregistered successfully\nRestarting cluster..." );

            Command restartClusterCommand = Commands.getRestartCommand( config.getMasterNode() );
            manager.getCommandRunner().runCommand( restartClusterCommand );

            //  temporarily turning off until exit code ir fixed:  if ( restartClusterCommand.hasSucceeded() ) {
            if ( restartClusterCommand.hasCompleted() )
            {
                po.addLog( "Cluster restarted successfully" );
            }
            else
            {
                po.addLog( String.format( "Cluster restart failed, %s, skipping...",
                    restartClusterCommand.getAllErrors() ) );
            }

            po.addLog( "Updating db..." );

            try
            {
                manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, config.getClusterName(), config );

                po.addLogDone( "Database information updated" );
            }
            catch ( DBException e )
            {
                po.addLogFailed( String.format( "Failed to update database information, %s", e.getMessage() ) );
            }
        }
        else
        {
            po.addLogFailed( String.format( "Unregistering node failed, %s", unregisterNodeCommand.getAllErrors() ) );
        }
    }
}
