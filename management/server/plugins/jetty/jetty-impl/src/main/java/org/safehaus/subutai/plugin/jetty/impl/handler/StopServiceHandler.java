package org.safehaus.subutai.plugin.jetty.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;

import com.google.common.collect.Sets;


public class StopServiceHandler extends AbstractOperationHandler<JettyImpl>
{

    private String lxcHostname;
    private String clusterName;


    public StopServiceHandler( final JettyImpl manager, final String clusterName, final String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Stopping %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        JettyConfig cassandraConfig = manager.getCluster( clusterName );
        if ( cassandraConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !cassandraConfig.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command stopServiceCommand = manager.getCommands().getStopCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( stopServiceCommand );

        if ( stopServiceCommand.hasSucceeded() )
        {
            AgentResult ar = stopServiceCommand.getResults().get( node.getUuid() );
            productOperation.addLog( ar.getStdOut() );
            productOperation.addLogDone( "Stop succeeded" );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
        }
    }
}