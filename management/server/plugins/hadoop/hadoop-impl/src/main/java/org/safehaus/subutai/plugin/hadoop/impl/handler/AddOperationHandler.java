package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class AddOperationHandler extends AbstractOperationHandler<HadoopImpl, HadoopClusterConfig>
{

    private int nodeCount;


    public AddOperationHandler( HadoopImpl manager, String clusterName, int nodeCount )
    {
        //    public AddOperationHandler( HadoopImpl manager, String clusterName ) {
        super( manager, clusterName );
        this.nodeCount = nodeCount;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Adding %d node to cluster %s", nodeCount, clusterName ) );
        //                String.format( "Adding node to cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        trackerOperation.addLogFailed(
                String.format( "This functionality currently is not supported by EnvironmentManager! Aborting",
                        clusterName ) );
        return;
        //        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        //
        //        if ( hadoopClusterConfig == null )
        //        {
        //            trackerOperation.addLogFailed(
        //                    String.format( "Malformed configuration\nAdding new node to %s aborted", clusterName ) );
        //            return;
        //        }
        //
        //        trackerOperation.addLog( String.format( "Creating %d lxc container(s)...", nodeCount ) );
        //        //        productOperation.addLog( String.format( "Creating " + nodeCount + " lxc containers..." ) );
        //        try
        //        {
        //            Set<Agent> agents = manager.getContainerManager().clone( hadoopClusterConfig.getTemplateName(),
        // nodeCount,
        //                    manager.getAgentManager().getPhysicalAgents(),
        //                    HadoopSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.SLAVE_NODE ) );
        //
        //            trackerOperation.addLog( "Lxc containers created successfully\nConfiguring network..." );
        //            for ( Agent agent : agents )
        //            {
        //                if ( manager.getNetworkManager().configHostsOnAgents( hadoopClusterConfig.getAllNodes(),
        // agent,
        //                        hadoopClusterConfig.getDomainName() ) && manager.getNetworkManager()
        // .configSshOnAgents(
        //                        hadoopClusterConfig.getAllNodes(), agent ) )
        //                {
        //                    trackerOperation.addLog( "Cluster network configured for " + agent.getHostname() );
        //
        //                    AddNodeOperation addOperation =
        //                            new AddNodeOperation( manager.getCommands(), hadoopClusterConfig, agent );
        //                    for ( Command command : addOperation.getCommandList() )
        //                    {
        //                        trackerOperation.addLog( ( String.format( "%s started...",
        // command.getDescription() ) ) );
        //                        manager.getCommandRunner().runCommand( command );
        //
        //                        if ( command.hasSucceeded() )
        //                        {
        //                            trackerOperation.addLog( String.format( "%s succeeded",
        // command.getDescription() ) );
        //                        }
        //                        else
        //                        {
        //                            trackerOperation.addLogFailed( String.format( "%s failed, %s",
        // command.getDescription(),
        //                                    command.getAllErrors() ) );
        //                        }
        //                    }
        //
        //                    hadoopClusterConfig.getTaskTrackers().add( agent );
        //                    hadoopClusterConfig.getDataNodes().add( agent );
        //
        //                    manager.getPluginDAO()
        //                           .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
        //                                   hadoopClusterConfig );
        //                    trackerOperation.addLogDone( "Cluster info saved to DB" );
        //                }
        //                else
        //                {
        //                    trackerOperation
        //                            .addLogFailed( "Could not configure network! Please see logs\nLXC creation
        // aborted" );
        //                }
        //            }
        //        }
        //        catch ( LxcCreateException e )
        //        {
        //            trackerOperation.addLogFailed( e.getMessage() );
        //        }
    }
}
