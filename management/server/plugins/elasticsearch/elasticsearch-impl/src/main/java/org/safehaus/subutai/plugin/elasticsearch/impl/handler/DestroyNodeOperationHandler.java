package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    private static final Logger LOG = Logger.getLogger( DestroyNodeOperationHandler.class.getName() );
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ElasticsearchImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !elasticsearchClusterConfiguration.getNodes().contains( agent ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( elasticsearchClusterConfiguration.getNodes().size() == 1 )
        {
            trackerOperation.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        trackerOperation.addLog( "Destroying lxc container..." );
        Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
        if ( physicalAgent == null )
        {
            trackerOperation.addLog(
                    String.format( "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                            agent.getHostname() ) );
        }
        else
        {
            try
            {
                manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                trackerOperation.addLog( "Lxc container destroyed successfully" );
                elasticsearchClusterConfiguration.getNodes().remove( agent );
                trackerOperation.addLog( "Updating db..." );

                manager.getPluginDAO().saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                        elasticsearchClusterConfiguration.getClusterName(), elasticsearchClusterConfiguration );
                trackerOperation.addLogDone( "Cluster info updated in DB\nDone" );
            }
            catch ( LxcDestroyException e )
            {
                trackerOperation.addLog(
                        String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                                e.getMessage() ) );
            }
        }
    }
}
