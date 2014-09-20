package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.SolrSetupStrategy;


public class AddNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{

    public AddNodeOperationHandler( SolrImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
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

        try
        {
            productOperation.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager().clone( solrClusterConfig.getTemplateName(), 1, null,
                    SolrSetupStrategy.getPlacementStrategy() );

            Agent agent = agents.iterator().next();

            solrClusterConfig.getNodes().add( agent );
            solrClusterConfig.setNumberOfNodes( solrClusterConfig.getNumberOfNodes() + 1 );

            productOperation.addLog( "Lxc container created successfully\nSaving information to database..." );

            manager.getPluginDAO().saveInfo( SolrClusterConfig.PRODUCT_KEY, clusterName, solrClusterConfig );
            productOperation.addLogDone( "Information saved to database" );
        }
        catch ( LxcCreateException ex )
        {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
