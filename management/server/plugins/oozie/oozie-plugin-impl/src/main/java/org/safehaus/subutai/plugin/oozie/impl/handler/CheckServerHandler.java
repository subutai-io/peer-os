package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckServerHandler extends AbstractOperationHandler<OozieImpl>
{

    private final Logger logger = Logger.getLogger( CheckServerHandler.class.getName() );
    private ProductOperation po;
    private String clusterName;


    public CheckServerHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Starting server on %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        final ProductOperation po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Checking status of cluster %s", clusterName ) );

        manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                OozieClusterConfig config = null;
                config = manager.getPluginDAO()
                                .getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName, OozieClusterConfig.class );

                Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                if ( serverAgent == null )
                {
                    po.addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command statusServiceCommand = Commands.getStatusServerCommand( servers );
                manager.getCommandRunner().runCommand( statusServiceCommand );

                if ( statusServiceCommand.hasCompleted() )
                {

                    po.addLogDone( statusServiceCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                }
                else
                {
                    po.addLogFailed(
                            String.format( "Failed to check status, %s", statusServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
