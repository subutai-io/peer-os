package org.safehaus.subutai.plugin.presto.impl.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

import static org.mockito.Mockito.mock;


public class PrestoImplMock extends PrestoImpl
{

    private PrestoClusterConfig clusterConfig;


    public PrestoImplMock()
    {
        super( mock( DataSource.class ) );
        setCommandRunner( mock( CommandRunner.class ) );
        setAgentManager( mock( AgentManager.class ) );
        setTracker( new TrackerMock() );
    }


    public PrestoImplMock setClusterConfig( PrestoClusterConfig clusterConfig )
    {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public PrestoClusterConfig getCluster( String clusterName )
    {
        return clusterConfig;
    }
}
