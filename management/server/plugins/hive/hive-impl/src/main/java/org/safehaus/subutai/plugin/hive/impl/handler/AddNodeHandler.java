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
//public class AddNodeHandler extends AbstractHandler
//{
//
//    private final String hostname;
//
//
//    public AddNodeHandler( HiveImpl manager, String clusterName, String hostname )
//    {
//        super( manager, clusterName );
//        this.hostname = hostname;
//        this.trackerOperation = manager.getTracker().createTrackerOperation( HiveConfig.PRODUCT_KEY,
//                "Add node to cluster: " + hostname );
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
//        String s = Commands.make( CommandType.LIST, null );
//        Command cmd = manager.getCommandRunner()
//                             .createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( agent ) ) );
//        manager.getCommandRunner().runCommand( cmd );
//
//        if ( !cmd.hasSucceeded() )
//        {
//            po.addLogFailed( "Failed to check installed packages" );
//            return;
//        }
//        AgentResult res = cmd.getResults().get( agent.getUuid() );
//        if ( res.getStdOut().contains( Product.HIVE.getPackageName() ) )
//        {
//            po.addLog( "Hive already installed on " + hostname );
//        }
//        else
//        {
//            s = Commands.make( CommandType.INSTALL, Product.HIVE );
//            cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ).withTimeout( 120 ),
//                    new HashSet<>( Arrays.asList( agent ) ) );
//            manager.getCommandRunner().runCommand( cmd );
//            if ( cmd.hasSucceeded() )
//            {
//                po.addLog( String.format( "Hive successfully installed on '%s'", hostname ) );
//            }
//            else
//            {
//                po.addLogFailed( "Installation failed: " + cmd.getAllErrors() );
//                return;
//            }
//        }
//
//        config.getClients().add( agent );
//
//        // configure client
//        s = Commands.configureClient( config.getServer() );
//        cmd = manager.getCommandRunner()
//                     .createCommand( new RequestBuilder( s ), new HashSet<>( Arrays.asList( agent ) ) );
//        manager.getCommandRunner().runCommand( cmd );
//
//        res = cmd.getResults().get( agent.getUuid() );
//        if ( cmd.hasSucceeded() )
//        {
//            po.addLog( "Hive client successfully configured" );
//        }
//        else
//        {
//            po.addLog( res.getStdOut() );
//            po.addLog( res.getStdErr() );
//            po.addLogFailed( "Configuration failed" );
//            return;
//        }
//
//        po.addLog( "Update cluster info..." );
//        manager.getPluginDAO().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
//        po.addLogDone( "Cluster info updated" );
//    }
//}
