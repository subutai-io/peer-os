package org.safehaus.subutai.plugin.solr.impl.handler.mock;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.Commands;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

import static org.mockito.Mockito.mock;


public class SolrImplMock extends SolrImpl {

    private SolrClusterConfig clusterSolrClusterConfig = null;


    public SolrImplMock() {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
                mock( EnvironmentManager.class ), mock( ContainerManager.class ) );
    }


    public SolrImplMock setCommands( Commands commands ) {
        this.commands = commands;
        return this;
    }


    public SolrImplMock setClusterSolrClusterConfig( SolrClusterConfig clusterSolrClusterConfig ) {
        this.clusterSolrClusterConfig = clusterSolrClusterConfig;
        return this;
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName ) {
        return clusterSolrClusterConfig;
    }


    public SolrImplMock setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
        return this;
    }
}
