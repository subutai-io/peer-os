//package org.safehaus.subutai.plugin.accumulo.impl.handler;
//
//
//import java.util.UUID;
//
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
//import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
//
//
///**
// * Handles stop cluster operation
// */
//public class StopClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl>
//{
//
//    public StopClusterOperationHandler( AccumuloImpl manager, String clusterName )
//    {
//        super( manager, clusterName );
//
//        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
//                String.format( "Stopping cluster %s", clusterName ) );
//    }
//
//
//    @Override
//    public UUID getTrackerId()
//    {
//        return trackerOperation.getId();
//    }
//
//
//    @Override
//    public void run()
//    {
//        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
//        if ( accumuloClusterConfig == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
//            return;
//        }
//
//        if ( manager.getAgentManager().getAgentByHostname( accumuloClusterConfig.getMasterNode().getHostname() )
//                == null )
//        {
//            trackerOperation.addLogFailed( String.format( "Master node '%s' is not connected",
//                    accumuloClusterConfig.getMasterNode().getHostname() ) );
//            return;
//        }
//
//        trackerOperation.addLog( "Stopping cluster..." );
//
//        Command stopCommand = manager.getCommands().getStopCommand( accumuloClusterConfig.getMasterNode() );
//        manager.getCommandRunner().runCommand( stopCommand );
//
//        if ( stopCommand.hasSucceeded() )
//        {
//            trackerOperation.addLogDone( "Cluster stopped successfully" );
//        }
//        else
//        {
//            trackerOperation.addLogFailed(
//                    String.format( "Failed to stop cluster %s, %s", clusterName, stopCommand.getAllErrors() ) );
//        }
//    }
//}
