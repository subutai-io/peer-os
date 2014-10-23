package org.safehaus.subutai.plugin.common.ui;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;


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
                TrackerOperationView prevPo =
                        tracker.getTrackerOperation( clusterConfig.getProductKey(), trackID );
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
            TrackerOperationView po =
                    tracker.getTrackerOperation( clusterConfig.getProductKey(), trackID );
            if ( po != null ) {
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    if ( po.getLog().toLowerCase().contains( getProductStoppedIdentifier( product ).toLowerCase() ) ) {
                        state = NodeState.STOPPED;
                    }
                    else if ( po.getLog().toLowerCase().contains( getProductRunningIdentifier( product ).toLowerCase() ) ) {
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


    private String getProductRunningIdentifier( final ApiBase product )
    {
        // TODO add product's running identifier if it is different from the above default value

        String runningIdentifier =  NodeState.RUNNING.toString();
        return runningIdentifier;
    }


    private String getProductStoppedIdentifier( final ApiBase product )
    {
        String stoppedIdentifier = NodeState.STOPPED.toString();
        if ( product instanceof  Oozie ) {
            stoppedIdentifier = "not running";
        }
        // TODO add product's stopped identifier if it is different from the above default value
        return stoppedIdentifier;

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
        Oozie oozie = (Oozie) product;
        OozieClusterConfig oozieClusterConfig = (OozieClusterConfig) clusterConfig;

        if ( nodeType.equals( NodeType.SERVER ) ) {
            trackID = getOozieServerOperation( oozie, oozieClusterConfig );
        }
        else if ( nodeType.equals( NodeType.CLIENT ) ) {
            trackID = getOozieClientOperation( oozie, oozieClusterConfig );
        }

        return trackID;
    }


    private UUID getOozieServerOperation( final Oozie oozie, final OozieClusterConfig oozieClusterConfig )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Start:
                trackID = oozie.startServer( oozieClusterConfig );
                break;
            case Stop:
                trackID = oozie.stopServer( oozieClusterConfig );
                break;
            case Status:
                trackID = oozie.checkServerStatus( oozieClusterConfig );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;

    }


    private UUID getOozieClientOperation( final Oozie oozie, final OozieClusterConfig oozieClusterConfig )
    {
        UUID trackID;
        switch ( operationType )
        {
            case Destroy:
                trackID = oozie.destroyNode( oozieClusterConfig.getClusterName(), agent.getHostname() );
                break;
            default:
                trackID = null;
                break;
        }
        return trackID;

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

