package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.Set;

import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.SolrSetupStrategy;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;


public class AddNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {

    public AddNodeOperationHandler( SolrImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run() {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        try {
            productOperation.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager().clone( solrClusterConfig.getTemplateName(), 1, null,
                    SolrSetupStrategy.getPlacementStrategy() );

            Agent agent = agents.iterator().next();

            solrClusterConfig.getNodes().add( agent );
            solrClusterConfig.setNumberOfNodes( solrClusterConfig.getNumberOfNodes() + 1 );

            productOperation.addLog( "Lxc container created successfully\nSaving information to database..." );

            try {
                manager.getDbManager().saveInfo2( SolrClusterConfig.PRODUCT_KEY, clusterName, solrClusterConfig );
                productOperation.addLogDone( "Information saved to database" );
            }
            catch ( DBException e ) {
                productOperation
                        .addLogFailed( String.format( "Failed to save information to database, %s", e.getMessage() ) );
            }
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }
}
