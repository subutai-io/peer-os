package org.safehaus.kiskis.mgmt.impl.solr.mock;


import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.impl.solr.handler.InstallOperationHandler;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommandMock;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;


public class MockBuilder {

    public static AbstractOperationHandler getInstallOperationWithResult( boolean succeeded ) {
        CommandMock installCommand = new CommandMock().setSucceeded( succeeded );
        CommandsMock commands = new CommandsMock().setInstallCommand( installCommand );
        SolrImpl solrImpl = new SolrImplMock().setCommands( commands );
        Config config = new Config().setClusterName( "test-cluster" );

        return new InstallOperationHandler( solrImpl, config );
    }


    public static SolrImpl getSorlImplWithExistingCluster() {
        return new SolrImplMock() {
            @Override
            public Config getCluster( String clusterName ) {
                return new Config();
            }
        };
    }

}
