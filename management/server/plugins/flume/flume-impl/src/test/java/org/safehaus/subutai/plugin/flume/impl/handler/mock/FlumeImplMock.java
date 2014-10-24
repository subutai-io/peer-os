package org.safehaus.subutai.plugin.flume.impl.handler.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;

import static org.mockito.Mockito.mock;


public class FlumeImplMock extends FlumeImpl
{

    private FlumeConfig config;


    public FlumeImplMock()
    {
        super( mock( DataSource.class ) );
        setCommandRunner( new CommandRunnerMock() );
        setAgentManager( new AgentManagerMock() );
        setTracker( new TrackerMock() );
    }


    @Override
    public FlumeConfig getCluster( String clusterName )
    {
        return config;
    }


    public void setConfig( FlumeConfig config )
    {
        this.config = config;
    }
}
