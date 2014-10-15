package org.safehaus.subutai.plugin.jetty.impl.handler;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;


public class StartClusterHandler extends AbstractOperationHandler<JettyImpl>
{

    private String clusterName;


    public StartClusterHandler( final JettyImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        JettyConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }
        Command startServiceCommand = manager.getCommands().getStartCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( startServiceCommand );

        if ( startServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Start succeeded" );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
        }
    }
}
