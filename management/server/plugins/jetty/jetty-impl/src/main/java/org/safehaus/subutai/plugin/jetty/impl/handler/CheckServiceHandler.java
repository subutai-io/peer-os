package org.safehaus.subutai.plugin.jetty.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;

import com.google.common.collect.Sets;


public class CheckServiceHandler extends AbstractOperationHandler<JettyImpl>
{

    private String lxcHostname;
    private String clusterName;


    public CheckServiceHandler( final JettyImpl manager, final String clusterName, final String agentUUID )
    {
        super( manager, clusterName );
        this.lxcHostname = agentUUID;
        this.clusterName = clusterName;
        this.trackerOperation = manager.getTracker().createTrackerOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        JettyConfig jettyConfig = manager.getCluster( clusterName );
        if ( jettyConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            trackerOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !jettyConfig.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        Command statusServiceCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );
        if ( statusServiceCommand.hasSucceeded() )
        {
            AgentResult ar = statusServiceCommand.getResults().get( agent.getUuid() );
            if ( ar.getStdOut().contains( "running" ) )
            {
                trackerOperation.addLogDone( "Jetty is running" );
            }
            else
            {
                trackerOperation.addLogFailed( "Jetty is not running" );
            }
        }
        else
        {
            trackerOperation.addLogFailed( "Jetty is not running" );
        }
    }
}