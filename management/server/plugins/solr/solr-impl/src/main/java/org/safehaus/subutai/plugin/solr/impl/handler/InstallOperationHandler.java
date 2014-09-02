package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;


public class InstallOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final SolrClusterConfig solrClusterConfig;


    public InstallOperationHandler( SolrImpl manager, SolrClusterConfig solrClusterConfig ) {
        super( manager, solrClusterConfig.getClusterName() );
        this.solrClusterConfig = solrClusterConfig;
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run() {

        productOperation.addLog( "Building environment..." );

        try {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( solrClusterConfig ) );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, solrClusterConfig, productOperation );
            clusterSetupStrategy.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            productOperation.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
