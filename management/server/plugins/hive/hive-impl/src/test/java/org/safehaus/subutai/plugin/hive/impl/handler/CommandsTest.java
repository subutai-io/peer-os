//package org.safehaus.subutai.plugin.hive.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
//import org.safehaus.subutai.plugin.hive.impl.CommandType;
//import org.safehaus.subutai.plugin.hive.impl.Commands;
//import org.safehaus.subutai.plugin.hive.impl.Product;
//
//
//public class CommandsTest
//{
//
//    private Agent hiveServer;
//
//
//    @Before
//    public void before() throws Exception
//    {
//        hiveServer = CommonMockBuilder.createAgent();
//    }
//
//
//    @Test
//    public void testMake()
//    {
//        for ( CommandType t : CommandType.values() )
//        {
//            for ( Product p : Product.values() )
//            {
//                String s = Commands.make( t, p );
//                Assert.assertNotNull( s );
//                if ( t != CommandType.LIST )
//                {
//                    Assert.assertTrue( s.contains( t.toString().toLowerCase() ) );
//                }
//            }
//        }
//    }
//
//
//    @Test
//    public void testConfigureHiveServer()
//    {
//        String hiveServerIp = hiveServer.getListIP().get( 0 );
//        String s = Commands.configureHiveServer( hiveServerIp );
//        Assert.assertNotNull( s );
//        Assert.assertTrue( s.contains( hiveServerIp ) );
//    }
//
//
//    @Test
//    public void testConfigureClient()
//    {
//        String s = Commands.configureClient( hiveServer );
//        Assert.assertNotNull( s );
//    }
//
//
//    @Test
//    public void testAddHivePoperty()
//    {
//        String s = Commands.addHiveProperty( "add", "file.xml", "property", null );
//        Assert.assertNotNull( s );
//
//        s = Commands.addHiveProperty( "add", "file.xml", "property", "value" );
//        Assert.assertNotNull( s );
//    }
//}
