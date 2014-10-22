package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( agent == null )
        {
            trackerOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !solrClusterConfig.getNodes().contains( agent ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to installation %s", lxcHostname,
                            clusterName ) );
            return;
        }

        if ( solrClusterConfig.getNodes().size() == 1 )
        {
            trackerOperation.addLogFailed( "This is the last node, destroy installation instead" );
            return;
        }

        solrClusterConfig.getNodes().remove( agent );
        solrClusterConfig.setNumberOfNodes( solrClusterConfig.getNumberOfNodes() - 1 );

        // Destroy lxc
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
            }
            catch ( LxcDestroyException e )
            {
                trackerOperation.addLog(
                        String.format( "Could not destroy lxc container, %s. Use LXC module to cleanup, skipping...",
                                e.getMessage() ) );
            }
        }

        // Update db
        trackerOperation.addLog( "Saving information to database..." );

        manager.getPluginDAO()
               .saveInfo( SolrClusterConfig.PRODUCT_KEY, solrClusterConfig.getClusterName(), solrClusterConfig );
        trackerOperation.addLogDone( "Saved information to database" );
    }
}
