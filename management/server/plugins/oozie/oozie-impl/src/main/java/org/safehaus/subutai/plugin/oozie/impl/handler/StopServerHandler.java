package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class StopServerHandler extends AbstractOperationHandler<OozieImpl>
{

    private final ProductOperation productOperation;


    public StopServerHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s...", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                OozieClusterConfig config = manager.getPluginDAO().getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName,
                        OozieClusterConfig.class );
                if ( config == null )
                {
                    productOperation.addLogFailed(
                            String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
                    return;
                }
                Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                if ( serverAgent == null )
                {
                    productOperation
                            .addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command stopServiceCommand = manager.getCommands().getStopServerCommand( servers );
                manager.getCommandRunner().runCommand( stopServiceCommand );

                if ( stopServiceCommand.hasSucceeded() )
                {
                    productOperation.addLogDone( "Stop succeeded" );
                }
                else
                {
                    productOperation
                            .addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}