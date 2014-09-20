package org.safehaus.subutai.plugin.solr.impl.handler.mock;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
import org.safehaus.subutai.plugin.common.mock.DbManagerMock;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.Commands;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;

import static org.mockito.Mockito.mock;


public class SolrImplMock extends SolrImpl
{

    private SolrClusterConfig clusterSolrClusterConfig = null;


    public SolrImplMock()
    {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
                mock( EnvironmentManager.class ), mock( ContainerManager.class ) );
    }


    public SolrImplMock setCommands( Commands commands )
    {
        this.commands = commands;
        return this;
    }


    public SolrImplMock setClusterSolrClusterConfig( SolrClusterConfig clusterSolrClusterConfig )
    {
        this.clusterSolrClusterConfig = clusterSolrClusterConfig;
        return this;
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName )
    {
        return clusterSolrClusterConfig;
    }


    public SolrImplMock setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
        return this;
    }
}
