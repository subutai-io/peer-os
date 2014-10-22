package org.safehaus.subutai.plugin.jetty.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;

import com.google.common.collect.Sets;


public class CheckNodeHandler extends AbstractOperationHandler<JettyImpl>
{

    private String clusterName;
    private String lxcHostname;


    public CheckNodeHandler( final JettyImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Checking jetty on %s of %s cluster...", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        JettyConfig JettyConfig = manager.getCluster( clusterName );
        if ( JettyConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            trackerOperation.addLogFailed( "Agent is not connected !" );
            return;
        }
        if ( !JettyConfig.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command statusServiceCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );

        if ( statusServiceCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Jetty is running" );
        }
        else
        {
            logStatusResults( trackerOperation, statusServiceCommand );
        }
    }


    private void logStatusResults( TrackerOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "Jetty is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Jetty is not running";
            }

            log.append( String.format( "%s", status ) );
        }
        po.addLogDone( log.toString() );
    }
}
