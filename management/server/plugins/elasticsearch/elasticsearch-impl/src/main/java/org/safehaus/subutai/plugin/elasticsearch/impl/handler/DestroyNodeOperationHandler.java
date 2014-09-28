package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.collect.Sets;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    private static final Logger LOG = Logger.getLogger( DestroyNodeOperationHandler.class.getName() );
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ElasticsearchImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !elasticsearchClusterConfiguration.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( elasticsearchClusterConfiguration.getNodes().size() == 1 )
        {
            productOperation.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }
        productOperation.addLog( "Uninstalling Mahout..." );
        Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
            if ( result.getExitCode() != null && result.getExitCode() == 0 )
            {
                if ( result.getStdOut().contains( "Package ksks-elasticsearch is not installed, so not removed" ) )
                {
                    productOperation.addLog( String.format( "Elasticsearch is not installed, so not removed on node %s",
                            agent.getHostname() ) );
                }
                else
                {
                    productOperation
                            .addLog( String.format( "Elasticsearch is removed from node %s", agent.getHostname() ) );
                }
            }
            else
            {
                productOperation
                        .addLog( String.format( "Error %s on node %s", result.getStdErr(), agent.getHostname() ) );
            }

            elasticsearchClusterConfiguration.getNodes().remove( agent );
            productOperation.addLog( "Updating db..." );

            manager.getPluginDAO().saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                    elasticsearchClusterConfiguration.getClusterName(), elasticsearchClusterConfiguration );
            productOperation.addLogDone( "Cluster info update in DB\nDone" );
        }
        else
        {
            productOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
