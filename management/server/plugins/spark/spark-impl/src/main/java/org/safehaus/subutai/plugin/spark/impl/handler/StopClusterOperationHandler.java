package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class StopClusterOperationHandler extends AbstractOperationHandler<SparkImpl> {

    public StopClusterOperationHandler(SparkImpl manager, String clusterName ) {
        super(manager, clusterName);

        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format("Stopping %s cluster", clusterName ) );
    }

    @Override
    public void run() {

        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Command stopCommand = Commands.getStopAllCommand( config.getMasterNode() );
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand(stopCommand, new CommandCallback() {
            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                if ( agentResult.getStdOut().contains( "stopping" ) ) {
                    ok.set( true );
                    stop();
                }
            }
        });

        if ( ok.get() ){
            productOperation.addLogDone( "All nodes are stopped successfully." );
        } else{
            productOperation.addLogFailed( "Could not stop all nodes !");
        }
    }
}
