package org.safehaus.kiskis.mgmt.impl.solr.handler;


import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.api.solr.Solr;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.impl.solr.util.CommandMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.CommandsMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.SolrImplMock;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;


class MockBuilder {

    static AbstractOperationHandler getInstallOperationWithResult( boolean succeeded ) {
        CommandMock installCommand = new CommandMock().setSucceeded( succeeded );
        CommandsMock commands = new CommandsMock().setInstallCommand( installCommand );
        SolrImpl solrImpl = new SolrImplMock().setCommands( commands );
        Config config = new Config().setClusterName( "test-cluster" );

        return new InstallOperationHandler( solrImpl, config );
    }


    static SolrImpl getSorlImplWithClusterExists() {
        return new SolrImplMock() {
            @Override
            public Config getCluster( String clusterName ) {
                return new Config();
            }
        };

    }

}
