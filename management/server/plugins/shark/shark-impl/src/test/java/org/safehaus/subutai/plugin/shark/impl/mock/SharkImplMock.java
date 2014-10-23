package org.safehaus.subutai.plugin.shark.impl.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;

import static org.mockito.Mockito.mock;


public class SharkImplMock extends SharkImpl
{
    private SharkClusterConfig clusterConfig;


    public SharkImplMock()
    {
        super( mock( DataSource.class ) );
        setCommandRunner( mock( CommandRunner.class ) );
        setAgentManager( mock( AgentManager.class ) );
        setTracker( new TrackerMock() );
    }


    public SharkImplMock setClusterConfig( SharkClusterConfig clusterConfig )
    {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public SharkClusterConfig getCluster( String clusterName )
    {
        return clusterConfig;
    }
}

