package org.safehaus.subutai.impl.presto.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.presto.Config;
import org.safehaus.subutai.impl.presto.Commands;
import org.safehaus.subutai.impl.presto.PrestoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.UUID;


/**
 * Created by dilshat on 5/7/14.
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<PrestoImpl>
{
    private final ProductOperation po;
    private final String lxcHostname;


    public StopNodeOperationHandler( PrestoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Stopping node %s in %s", lxcHostname, clusterName ) );

    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        Config config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !config.getAllNodes().contains( node ) )
        {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster", lxcHostname ) );
            return;
        }

        po.addLog( String.format( "Stopping node %s...", node.getHostname() ) );

        Command stopNodeCommand = Commands.getStopCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( stopNodeCommand );

        if ( stopNodeCommand.hasSucceeded() )
        {
            po.addLogDone( String.format( "Node %s stopped", node.getHostname() ) );
        }
        else
        {
            po.addLogFailed(
                String.format( "Stopping %s failed, %s", node.getHostname(), stopNodeCommand.getAllErrors() ) );
        }
    }
}
