///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.safehaus.subutai.plugin.zookeeper.ui.manager;
//
//
//import java.util.UUID;
//
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperationView;
//import org.safehaus.subutai.core.tracker.api.Tracker;
//import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
//import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
//
//
//public class StopTask implements Runnable
//{
//
//    private final String clusterName, lxcHostname;
//    private final CompleteEvent completeEvent;
//    private final Zookeeper zookeeper;
//    private final Tracker tracker;
//
//
//    public StopTask( Zookeeper zookeeper, Tracker tracker, String clusterName, String lxcHostname,
//                     CompleteEvent completeEvent )
//    {
//        this.zookeeper = zookeeper;
//        this.tracker = tracker;
//        this.clusterName = clusterName;
//        this.lxcHostname = lxcHostname;
//        this.completeEvent = completeEvent;
//    }
//
//
//    public void run()
//    {
//
//        UUID trackID = zookeeper.stopNode( clusterName, lxcHostname );
//
//        long start = System.currentTimeMillis();
//
//        while ( !Thread.interrupted() )
//        {
//            TrackerOperationView po = tracker.getTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY, trackID );
//            if ( po != null )
//            {
//                if ( po.getState() != OperationState.RUNNING )
//                {
//                    completeEvent.onComplete( po.getLog() );
//                    break;
//                }
//            }
//            try
//            {
//                Thread.sleep( 1000 );
//            }
//            catch ( InterruptedException ex )
//            {
//                break;
//            }
//            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
//            {
//                break;
//            }
//        }
//    }
//}
