//package org.safehaus.subutai.plugin.hive.impl.handler;
//
//
//import java.util.HashSet;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;
//
//
//public class AbstractHandlerTest
//{
//
//    private final AbstractHandler impl;
//    private final String serverHostname = "server-host";
//    private HiveConfig config;
//
//
//    public AbstractHandlerTest()
//    {
//        impl = new AbstractHandlerMock( new HiveImplMock(), "test-cluster" );
//    }
//
//
//    @Before
//    public void setUp()
//    {
//        config = new HiveConfig();
//        config.setClusterName( "hive-cluster" );
//
//        Agent a = HiveImplMock.createAgent( serverHostname );
//        config.setServer( a );
//        config.setClients( new HashSet<Agent>( 4 ) );
//        for ( int i = 0; i < config.getClients().size(); i++ )
//        {
//            a = HiveImplMock.createAgent( "hostname" + i );
//            config.getClients().add( a );
//        }
//    }
//
//
//    @Test
//    public void testIsServerNode()
//    {
//        Assert.assertTrue( impl.isServerNode( config, serverHostname ) );
//        Assert.assertFalse( impl.isServerNode( config, "other-name" ) );
//    }
//}
