//package org.safehaus.subutai.plugin.flume.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.safehaus.subutai.plugin.flume.impl.CommandType;
//import org.safehaus.subutai.plugin.flume.impl.Commands;
//
//
//@Ignore
//public class CommandsTest
//{
//
//    @Test
//    public void testMake()
//    {
//        for ( CommandType t : CommandType.values() )
//        {
//            String s = Commands.make( t );
//            Assert.assertNotNull( "Empty command string", s );
//            if ( t != CommandType.STATUS )
//            {
//                Assert.assertTrue( s.contains( t.toString().toLowerCase() ) );
//            }
//        }
//    }
//}
