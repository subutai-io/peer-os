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
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( agent == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !solrClusterConfig.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to installation %s", lxcHostname,
                            clusterName ) );
            return;
        }

        if ( solrClusterConfig.getNodes().size() == 1 )
        {
            productOperation.addLogFailed( "This is the last node, destroy installation instead" );
            return;
        }

        solrClusterConfig.getNodes().remove( agent );
        solrClusterConfig.setNumberOfNodes( solrClusterConfig.getNumberOfNodes() - 1 );

        // Destroy lxc
        productOperation.addLog( "Destroying lxc container..." );
        Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );

        if ( physicalAgent == null )
        {
            productOperation.addLog(
                    String.format( "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                            agent.getHostname() ) );
        }
        else
        {
            try
            {
                manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                productOperation.addLog( "Lxc container destroyed successfully" );
            }
            catch ( LxcDestroyException e )
            {
                productOperation.addLog(
                        String.format( "Could not destroy lxc container, %s. Use LXC module to cleanup, skipping...",
                                e.getMessage() ) );
            }
        }

        // Update db
        productOperation.addLog( "Saving information to database..." );

        manager.getPluginDAO()
               .saveInfo( SolrClusterConfig.PRODUCT_KEY, solrClusterConfig.getClusterName(), solrClusterConfig );
        productOperation.addLogDone( "Saved information to database" );
    }
}
