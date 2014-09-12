package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.HadoopSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.common.AddNodeOperation;


public class AddOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private int nodeCount;


    public AddOperationHandler( HadoopImpl manager, String clusterName, int nodeCount ) {
//    public AddOperationHandler( HadoopImpl manager, String clusterName ) {
        super( manager, clusterName );
        this.nodeCount = nodeCount;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Adding %d node to cluster %s", nodeCount, clusterName ) );
//                String.format( "Adding node to cluster %s", clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed(
                    String.format( "Malformed configuration\nAdding new node to %s aborted", clusterName ) );
            return;
        }

        productOperation.addLog( String.format( "Creating %d lxc container(s)...", nodeCount ) );
//        productOperation.addLog( String.format( "Creating " + nodeCount + " lxc containers..." ) );
        try {
            Set<Agent> agents = manager.getContainerManager().clone( hadoopClusterConfig.getTemplateName(),
                    nodeCount, manager.getAgentManager().getPhysicalAgents(),
                    HadoopSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.SLAVE_NODE ) );

            productOperation.addLog( "Lxc containers created successfully\nConfiguring network..." );
            for ( Agent agent : agents ) {
                if ( manager.getNetworkManager().configHostsOnAgents( hadoopClusterConfig.getAllNodes(), agent,
                        hadoopClusterConfig.getDomainName() ) && manager.getNetworkManager().configSshOnAgents(
                        hadoopClusterConfig.getAllNodes(), agent ) ) {
                    productOperation.addLog( "Cluster network configured for " + agent.getHostname() );

                    AddNodeOperation addOperation = new AddNodeOperation( hadoopClusterConfig, agent );
                    for ( Command command : addOperation.getCommandList() ) {
                        productOperation.addLog( ( String.format( "%s started...", command.getDescription() ) ) );
                        manager.getCommandRunner().runCommand( command );

                        if ( command.hasSucceeded() ) {
                            productOperation.addLogDone( String.format( "%s succeeded", command.getDescription() ) );
                        }
                        else {
                            productOperation.addLogFailed(
                                    String.format( "%s failed, %s", command.getDescription(), command.getAllErrors() ) );
                        }
                    }

                    hadoopClusterConfig.getTaskTrackers().add( agent );
                    hadoopClusterConfig.getDataNodes().add( agent );

                    try {
                        manager.getPluginDAO()
                               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                       hadoopClusterConfig );
                        productOperation.addLog( "Cluster info saved to DB" );
                    }
                    catch ( DBException e ) {
                        productOperation.addLogFailed(
                                "Could not save cluster info to DB! Please see logs\n" + "Adding new node aborted" );
                    }
                }
                else {
                    productOperation.addLogFailed( "Could not configure network! Please see logs\nLXC creation aborted" );
                }
            }
        }
        catch ( LxcCreateException e ) {
            productOperation.addLogFailed( e.getMessage() );
        }
    }


//    public int getNodeCount() {
//        return nodeCount;
//    }
}
