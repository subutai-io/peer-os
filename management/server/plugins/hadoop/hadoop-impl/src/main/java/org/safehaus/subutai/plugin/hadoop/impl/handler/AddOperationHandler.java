package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Set;

import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.AddNodeOperation;
import org.safehaus.subutai.plugin.hadoop.impl.common.HadoopSetupStrategy;


public class AddOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public AddOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null )
        {
            productOperation.addLogFailed(
                    String.format( "Malformed configuration\nAdding new node to %s aborted", clusterName ) );
            return;
        }

        productOperation.addLog( "Creating lxc container..." );
        try
        {
            Set<Agent> agents = manager.getContainerManager().clone( hadoopClusterConfig.getTemplateName(), 1,
                    manager.getAgentManager().getPhysicalAgents(),
                    HadoopSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.SLAVE_NODE ) );

            Agent agent = agents.iterator().next();
            productOperation.addLog( "Lxc containers created successfully\nConfiguring network..." );

            if ( manager.getNetworkManager().configHostsOnAgents( hadoopClusterConfig.getAllNodes(), agent,
                    hadoopClusterConfig.getDomainName() ) && manager.getNetworkManager().configSshOnAgents(
                    hadoopClusterConfig.getAllNodes(), agent ) )
            {
                productOperation.addLog( "Cluster network configured" );

                AddNodeOperation addOperation = new AddNodeOperation( hadoopClusterConfig, agent );
                for ( Command command : addOperation.getCommandList() )
                {
                    productOperation.addLog( ( String.format( "%s started...", command.getDescription() ) ) );
                    manager.getCommandRunner().runCommand( command );

                    if ( command.hasSucceeded() )
                    {
                        productOperation.addLogDone( String.format( "%s succeeded", command.getDescription() ) );
                    }
                    else
                    {
                        productOperation.addLogFailed(
                                String.format( "%s failed, %s", command.getDescription(), command.getAllErrors() ) );
                    }
                }

                hadoopClusterConfig.getTaskTrackers().add( agent );
                hadoopClusterConfig.getDataNodes().add( agent );

                manager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                        hadoopClusterConfig );
                productOperation.addLog( "Cluster info saved to DB" );
            }
            else
            {
                productOperation.addLogFailed( "Could not configure network! Please see logs\nLXC creation aborted" );
            }
        }
        catch ( LxcCreateException e )
        {
            productOperation.addLogFailed( e.getMessage() );
        }
    }
}
