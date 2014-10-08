package org.safehaus.subutai.plugin.common.ui;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;


import java.util.UUID;


public class OperationTask implements Runnable {

    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private UUID trackID;
    private ConfigBase clusterConfig;
    private Agent agent;
    private ApiBase product;
    private NodeType nodeType;
    private OperationType operationType;


    public OperationTask( ApiBase product, Tracker tracker, OperationType operationType, NodeType nodeType,
                          ConfigBase clusterConfig, CompleteEvent completeEvent, UUID trackID, Agent agent ) {
        this.product = product;
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.clusterConfig = clusterConfig;
        this.agent = agent;
        this.nodeType = nodeType;
        this.operationType = operationType;
    }


    public void run() {

        if ( trackID != null ) {
            while ( true ) {
                ProductOperationView prevPo =
                        tracker.getProductOperation(clusterConfig.getProductKey(), trackID);
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

        if ( agent != null ) {
            UUID trackID = startOperation();
            waitUntilOperationFinish( trackID );
        }
    }


    private void waitUntilOperationFinish( UUID trackID )
    {
        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() ) {
            ProductOperationView po =
                    tracker.getProductOperation(HadoopClusterConfig.PRODUCT_KEY, trackID);
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
        completeEvent.onComplete( state );
    }


    private UUID startOperation()
    {
        UUID trackID = null;
        if ( product instanceof Hadoop ) {
            trackID = startHadoopOperation();
        }
        else if ( product instanceof Oozie ) {
            trackID = startOozieOperation();
        }
        // TODO implement each case for each product's operation
        return trackID;
    }


    private UUID startOozieOperation()
    {
        // TODO implement Oozie product operations
        return null;
    }


    private UUID startHadoopOperation()
    {
        Hadoop hadoop = (Hadoop) product;
        HadoopClusterConfig hadoopClusterConfig = (HadoopClusterConfig) clusterConfig;

        if ( nodeType.equals( NodeType.NAMENODE ) ) {
            trackID = getNameNodeOperation( hadoop, hadoopClusterConfig );
        }
        else if ( nodeType.equals( NodeType.JOBTRACKER ) ) {
            trackID = getJobTrackerOperation( hadoop, hadoopClusterConfig );
        }
        else if ( nodeType.equals( NodeType.DATANODE )  ) {
            trackID = getDataNodeOperation( hadoop, hadoopClusterConfig, agent );
        }
        else {
            trackID = getTaskTrackerOperation( hadoop, hadoopClusterConfig, agent );
        }

        return trackID;
    }


    public UUID getNameNodeOperation( Hadoop hadoop, HadoopClusterConfig hadoopClusterConfig )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Start:
                trackID = hadoop.startNameNode( hadoopClusterConfig );
                break;
            case Stop:
                trackID = hadoop.stopNameNode( hadoopClusterConfig );
                break;
            case Restart:
                trackID = hadoop.restartNameNode( hadoopClusterConfig );
                break;
            case Status:
                trackID = hadoop.statusNameNode( hadoopClusterConfig );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;
    }


    public UUID getJobTrackerOperation( Hadoop hadoop, HadoopClusterConfig hadoopClusterConfig )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Start:
                trackID = hadoop.startJobTracker( hadoopClusterConfig );
                break;
            case Stop:
                trackID = hadoop.stopJobTracker( hadoopClusterConfig );
                break;
            case Restart:
                trackID = hadoop.restartJobTracker( hadoopClusterConfig );
                break;
            case Status:
                trackID = hadoop.statusJobTracker( hadoopClusterConfig );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;
    }


    public UUID getDataNodeOperation( Hadoop hadoop, HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Start:
                trackID = hadoop.startDataNode( hadoopClusterConfig, agent );
                break;
            case Stop:
                trackID = hadoop.stopDataNode( hadoopClusterConfig, agent );
                break;
            case Status:
                trackID = hadoop.statusDataNode( hadoopClusterConfig, agent );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;
    }


    public UUID getTaskTrackerOperation( Hadoop hadoop, HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Start:
                trackID = hadoop.startTaskTracker( hadoopClusterConfig, agent );
                break;
            case Stop:
                trackID = hadoop.stopTaskTracker( hadoopClusterConfig, agent );
                break;
            case Status:
                trackID = hadoop.statusTaskTracker( hadoopClusterConfig, agent );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;
    }
}

