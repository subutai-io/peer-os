/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


/**
 * @author dilshat
 */
public class CheckTask implements Runnable
{

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Zookeeper zookeeper;
    private final Tracker tracker;


    public CheckTask( Zookeeper zookeeper, Tracker tracker, String clusterName, String lxcHostname,
                      CompleteEvent completeEvent )
    {
        this.zookeeper = zookeeper;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    public void run()
    {

        UUID trackID = zookeeper.checkNode( clusterName, lxcHostname );

        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            ProductOperationView po = tracker.getProductOperation( ZookeeperClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    if ( po.getLog().contains( NodeState.STOPPED.toString() ) )
                    {
                        state = NodeState.STOPPED;
                    }
                    else if ( po.getLog().contains( NodeState.RUNNING.toString() ) )
                    {
                        state = NodeState.RUNNING;
                    }
                    break;
                }
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }

        completeEvent.onComplete( state );
    }
}
