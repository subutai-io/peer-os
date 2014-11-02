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
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;


public class StopTask implements Runnable
{

    private final CompleteEvent completeEvent;
    private UUID trackID;
    private HadoopClusterConfig hadoopClusterConfig;
    private Agent agent;
    private NodeType nodeType;
    private Hadoop hadoop;
    private Tracker tracker;


    public StopTask( Hadoop hadoop, Tracker tracker, NodeType nodeType, HadoopClusterConfig hadoopClusterConfig,
                     CompleteEvent completeEvent, UUID trackID, Agent agent )
    {
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.agent = agent;
        this.nodeType = nodeType;
    }


    public void run()
    {

        if ( trackID != null )
        {
            while ( true )
            {
                TrackerOperationView prevPo = tracker.getTrackerOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( prevPo.getState() == OperationState.RUNNING )
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
        }

        NodeState state = NodeState.UNKNOWN;
        if ( agent != null )
        {
            if ( nodeType.equals( NodeType.NAMENODE ) )
            {
                trackID = hadoop.stopNameNode( hadoopClusterConfig );
            }
            else if ( nodeType.equals( NodeType.JOBTRACKER ) )
            {
                trackID = hadoop.stopJobTracker( hadoopClusterConfig );
            }
            //            if ( nodeType.equals( NodeType.DATANODE )  ) {
            //                trackID = HadoopUI.getHadoopManager().stopDataNode( hadoopClusterConfig, agent );
            //            }
            //            else {
            //                trackID = HadoopUI.getHadoopManager().stopTaskTracker( hadoopClusterConfig, agent );
            //            }


            long start = System.currentTimeMillis();
            while ( !Thread.interrupted() )
            {
                TrackerOperationView po = tracker.getTrackerOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( po != null )
                {
                    if ( po.getState() != OperationState.RUNNING )
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
        }
        completeEvent.onComplete( state );
    }
}
