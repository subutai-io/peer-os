package org.safehaus.subutai.impl.shark.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.shark.Commands;
import org.safehaus.subutai.impl.shark.SharkImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;


/**
 * Created by dilshat on 5/7/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final ProductOperation po;
    private final String lxcHostname;


    public DestroyNodeOperationHandler( SharkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
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
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !config.getNodes().contains( agent ) )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            po.addLogFailed(
                "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }
        po.addLog( "Uninstalling Shark..." );
        Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
            if ( result.getExitCode() != null && result.getExitCode() == 0 )
            {
                if ( result.getStdOut().contains( "Package ksks-shark is not installed, so not removed" ) )
                {
                    po.addLog( String.format( "Shark is not installed, so not removed on node %s",
                        agent.getHostname() ) );
                }
                else
                {
                    po.addLog( String.format( "Shark is removed from node %s",
                        agent.getHostname() ) );
                }
            }
            else
            {
                po.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                    agent.getHostname() ) );
            }

            config.getNodes().remove( agent );
            po.addLog( "Updating db..." );

            if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) )
            {
                po.addLogDone( "Cluster info update in DB\nDone" );
            }
            else
            {
                po.addLogFailed( "Error while updating cluster info in DB. Check logs.\nFailed" );
            }
        }
        else
        {
            po.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
