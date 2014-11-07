//package org.safehaus.subutai.plugin.hive.impl.handler;
//
//
//import java.util.Arrays;
//import java.util.HashSet;
//
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.command.api.command.AgentResult;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.common.protocol.RequestBuilder;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//import org.safehaus.subutai.plugin.hive.impl.CommandType;
//import org.safehaus.subutai.plugin.hive.impl.Commands;
//import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
//import org.safehaus.subutai.plugin.hive.impl.Product;
//
//
//public class StopHandler extends AbstractHandler
//{
//
//    private final String hostname;
//
//
//    public StopHandler( HiveImpl manager, String clusterName, String hostname )
//    {
//        super( manager, clusterName );
//        this.hostname = hostname;
//        this.trackerOperation =
//                manager.getTracker().createTrackerOperation( HiveConfig.PRODUCT_KEY, "Stop node " + hostname );
//    }
//
//
//    @Override
//    public void run()
//    {
//        TrackerOperation po = trackerOperation;
//        HiveConfig config = manager.getCluster( clusterName );
//        if ( config == null )
//        {
//            po.addLogFailed( String.format( "Cluster '%s' does not exist", clusterName ) );
//            return;
//        }
//
//        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
//        if ( agent == null )
//        {
//            po.addLogFailed( String.format( "Node '%s' is not connected", hostname ) );
//            return;
//        }
//
//        String s = Commands.make( CommandType.STOP, Product.HIVE );
//        Command cmd = manager.getCommandRunner()
//                             .createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( agent ) ) );
//        manager.getCommandRunner().runCommand( cmd );
//
//        AgentResult res = cmd.getResults().get( agent.getUuid() );
//        po.addLog( res.getStdOut() );
//        po.addLog( res.getStdErr() );
//
//        boolean ok = cmd.hasSucceeded();
//
//        // if server node, stop Derby
//        if ( ok && agent.equals( config.getServer() ) )
//        {
//
//            s = Commands.make( CommandType.STOP, Product.DERBY );
//            cmd = manager.getCommandRunner()
//                         .createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( agent ) ) );
//            manager.getCommandRunner().runCommand( cmd );
//
//            res = cmd.getResults().get( agent.getUuid() );
//            po.addLog( res.getStdOut() );
//            po.addLog( res.getStdErr() );
//
//            ok = cmd.hasSucceeded();
//        }
//
//        if ( ok )
//        {
//            po.addLogDone( "Done" );
//        }
//        else
//        {
//            po.addLogFailed( null );
//        }
//    }
//}
