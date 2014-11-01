package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.vaadin.event.MouseEvents;


/**
 * Created by daralbaev on 12.04.14.
 */
public class JobTracker extends ClusterNode
{

    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;


    public JobTracker( final Hadoop hadoop, final Tracker tracker, final ExecutorService executorService,
                       final HadoopClusterConfig cluster )
    {
        super( cluster );
        this.tracker = tracker;
        this.executorService = executorService;
        this.hadoop = hadoop;

//        setHostname( cluster.getJobTracker().getHostname() );

        startButton.addClickListener( new MouseEvents.ClickListener()
        {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent )
            {
                setLoading( true );
                getStatus( hadoop.startJobTracker( cluster ) );
            }
        } );
        startButton.setId( "jobTrackerStart" );

        stopButton.addClickListener( new MouseEvents.ClickListener()
        {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent )
            {
                setLoading( true );
                getStatus( hadoop.stopJobTracker( cluster ) );
            }
        } );
        stopButton.setId( "jobTrackerStop" );

        restartButton.addClickListener( new MouseEvents.ClickListener()
        {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent )
            {
                setLoading( true );
                getStatus( hadoop.restartJobTracker( cluster ) );
            }
        } );
        restartButton.setId( "jobTrackerRestart" );

        getStatus( null );
    }


    @Override
    protected void getStatus( UUID trackID )
    {
        setLoading( true );
        for ( ClusterNode slaveNode : slaveNodes )
        {
            slaveNode.setLoading( true );
        }

//        executorService.execute( new CheckTask( hadoop, tracker, NodeType.JOBTRACKER, cluster, new CompleteEvent()
//        {
//
//            public void onComplete( NodeState state )
//            {
//                synchronized ( progressButton )
//                {
//                    boolean isRunning = false;
//                    if ( state == NodeState.RUNNING )
//                    {
//                        isRunning = true;
//                    }
//                    else if ( state == NodeState.STOPPED )
//                    {
//                        isRunning = false;
//                    }
//
//                    startButton.setEnabled( !isRunning );
//                    restartButton.setEnabled( isRunning );
//                    stopButton.setEnabled( isRunning );
//
//                    for ( ClusterNode slaveNode : slaveNodes )
//                    {
//                        slaveNode.getStatus( null );
//                    }
//
//                    setLoading( false );
//                }
//            }
//        }, trackID, cluster.getJobTracker() ) );
    }


    @Override
    protected void setLoading( boolean isLoading )
    {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        restartButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
        progressButton.setId( "jobTrackerProgress" );
    }
}
