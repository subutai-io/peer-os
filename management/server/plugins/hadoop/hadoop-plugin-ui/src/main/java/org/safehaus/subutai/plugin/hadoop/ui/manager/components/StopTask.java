/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;

import com.vaadin.ui.Notification;


public class StopTask implements Runnable {

    private final CompleteEvent completeEvent;
    private UUID trackID;
    private HadoopClusterConfig hadoopClusterConfig;
    private Agent agent;
    private NodeType nodeType;


    public StopTask( NodeType nodeType, HadoopClusterConfig hadoopClusterConfig, CompleteEvent completeEvent,
                     UUID trackID, Agent agent ) {
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
        this.nodeType = nodeType;
    }


    public void run() {

        if ( trackID != null ) {
            while ( true ) {
                ProductOperationView prevPo =
                        HadoopUI.getTracker().getProductOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( prevPo.getState() == ProductOperationState.RUNNING ) {
                    try {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex ) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }

        NodeState state = NodeState.UNKNOWN;
        if ( agent != null ) {
            if ( nodeType.equals( NodeType.NAMENODE ) ) {
                trackID = HadoopUI.getHadoopManager().stopNameNode( hadoopClusterConfig );
            }
            else if ( nodeType.equals( NodeType.JOBTRACKER ) ) {
                trackID = HadoopUI.getHadoopManager().stopJobTracker( hadoopClusterConfig );
            }
//            if ( nodeType.equals( NodeType.DATANODE )  ) {
//                trackID = HadoopUI.getHadoopManager().stopDataNode( hadoopClusterConfig, agent );
//            }
//            else {
//                trackID = HadoopUI.getHadoopManager().stopTaskTracker( hadoopClusterConfig, agent );
//            }


            long start = System.currentTimeMillis();
            while ( !Thread.interrupted() ) {
                ProductOperationView po =
                        HadoopUI.getTracker().getProductOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( po != null ) {
                    if ( po.getState() != ProductOperationState.RUNNING ) {
                        if ( po.getLog().contains( NodeState.STOPPED.toString() ) ) {
                            state = NodeState.STOPPED;
                        }
                        else if ( po.getLog().contains( NodeState.RUNNING.toString() ) ) {
                            state = NodeState.RUNNING;
                        }
                        break;
                    }
                }

                try {
                    Thread.sleep( 1000 );
                }
                catch ( InterruptedException ex ) {
                    break;
                }
                if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 ) {
                    break;
                }
            }
        }
        completeEvent.onComplete( state );
    }
}
