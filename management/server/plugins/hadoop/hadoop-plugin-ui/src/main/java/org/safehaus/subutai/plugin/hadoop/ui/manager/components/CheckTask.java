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
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * @author dilshat
 */
public class CheckTask implements Runnable {

    private final CompleteEvent completeEvent;
    private UUID trackID;
    private HadoopClusterConfig hadoopClusterConfig;
    private Agent agent;
    private Boolean isDataNode;

    private final Hadoop hadoop;
    private final Tracker tracker;


    public CheckTask( Hadoop hadoop, Tracker tracker, HadoopClusterConfig hadoopClusterConfig,
                      CompleteEvent completeEvent, UUID trackID, Agent agent ) {
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
    }


    public CheckTask( Hadoop hadoop, Tracker tracker, HadoopClusterConfig hadoopClusterConfig,
                      CompleteEvent completeEvent, UUID trackID, Agent agent, boolean isDataNode ) {
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
        this.isDataNode = isDataNode;
    }


    public void run() {

        if ( trackID != null ) {
            while ( true ) {
                ProductOperationView prevPo = tracker.getProductOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
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
            if ( agent.equals( hadoopClusterConfig.getNameNode() ) ) {
                trackID = hadoop.statusNameNode( hadoopClusterConfig );
            }
            else if ( agent.equals( hadoopClusterConfig.getJobTracker() ) ) {
                trackID = hadoop.statusJobTracker( hadoopClusterConfig );
            }
            else if ( agent.equals( hadoopClusterConfig.getSecondaryNameNode() ) ) {
                trackID = hadoop.statusSecondaryNameNode( hadoopClusterConfig );
            }
            else if ( isDataNode != null ) {
                if ( isDataNode ) {
                    trackID = hadoop.statusDataNode( hadoopClusterConfig, agent );
                }
                else {
                    trackID = hadoop.statusTaskTracker( hadoopClusterConfig, agent );
                }
            }


            long start = System.currentTimeMillis();
            while ( !Thread.interrupted() ) {
                ProductOperationView po = tracker.getProductOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
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
