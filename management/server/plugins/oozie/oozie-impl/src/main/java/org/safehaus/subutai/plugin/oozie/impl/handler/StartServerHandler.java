package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class StartServerHandler extends AbstractOperationHandler<OozieImpl>
{
    private final ProductOperation productOperation;


    public StartServerHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Starting server on %s cluster...", clusterName ) );
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
                Command startServiceCommand = Commands.getStartServerCommand( servers );
                manager.getCommandRunner().runCommand( startServiceCommand );

                if ( startServiceCommand.hasCompleted() )
                {
                    productOperation.addLog( "Checking status..." );

                    Command checkCommand = Commands.getStatusServerCommand( servers );
                    manager.getCommandRunner().runCommand( checkCommand );

                    if ( checkCommand.hasCompleted() )
                    {

                        productOperation
                                .addLogDone( checkCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                    }
                    else
                    {
                        productOperation.addLogFailed(
                                String.format( "Failed to check status, %s", checkCommand.getAllErrors() ) );
                    }
                }
                else
                {
                    productOperation
                            .addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
