package org.safehaus.subutai.plugin.hive.impl.handler.mock;


import java.util.Arrays;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
import org.safehaus.subutai.plugin.common.mock.DbManagerMock;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;


public class HiveImplMock extends HiveImpl
{

    private HiveConfig config;


    public HiveImplMock()
    {
        setAgentManager( new AgentManagerMock() );
        setCommandRunner( new CommandRunnerMock() );
        setDbManager( new DbManagerMock() );
        setTracker( new TrackerMock() );
    }


    public static Agent createAgent( String hostname )
    {
        return new Agent( UUIDUtil.generateTimeBasedUUID(), hostname, "parent-host", "00:00:00:00",
                Arrays.asList( "127.0.0.1", "127.0.0.1" ), true, "transportId", UUIDUtil.generateTimeBasedUUID(), UUIDUtil.generateTimeBasedUUID() );
    }


    @Override
    public HiveConfig getCluster( String clusterName )
    {
        return config;
    }


    public void setConfig( HiveConfig config )
    {
        this.config = config;
    }
}
