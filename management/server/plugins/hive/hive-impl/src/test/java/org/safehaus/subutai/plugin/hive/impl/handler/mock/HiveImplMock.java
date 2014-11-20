//package org.safehaus.subutai.plugin.hive.impl.handler.mock;
//
//
//import java.util.Arrays;
//
//import javax.sql.DataSource;
//
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.util.UUIDUtil;
//import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
//import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
//import org.safehaus.subutai.plugin.common.mock.TrackerMock;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
//
//import static org.mockito.Mockito.mock;
//
//
//public class HiveImplMock extends HiveImpl
//{
//
//    private HiveConfig config;
//
//
//    public HiveImplMock()
//    {
//        super( mock( DataSource.class ) );
//        setAgentManager( new AgentManagerMock() );
//        setCommandRunner( new CommandRunnerMock() );
//        setTracker( new TrackerMock() );
//    }
//
//
//    public static Agent createAgent( String hostname )
//    {
//        return new Agent( UUIDUtil.generateTimeBasedUUID(), hostname, "parent-host", "00:00:00:00",
//                Arrays.asList( "127.0.0.1", "127.0.0.1" ), true, "transportId", UUIDUtil.generateTimeBasedUUID(),
//                UUIDUtil.generateTimeBasedUUID() );
//    }
//
//
//    @Override
//    public HiveConfig getCluster( String clusterName )
//    {
//        return config;
//    }
//
//
//    public void setConfig( HiveConfig config )
//    {
//        this.config = config;
//    }
//}
