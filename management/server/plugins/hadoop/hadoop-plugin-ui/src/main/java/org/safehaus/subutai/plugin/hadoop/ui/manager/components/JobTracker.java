package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;

import com.vaadin.event.MouseEvents;


/**
 * Created by daralbaev on 12.04.14.
 */
public class JobTracker extends ClusterNode {

    public JobTracker( final HadoopClusterConfig cluster ) {
        super( cluster );
        setHostname( cluster.getJobTracker().getHostname() );

        startButton.addClickListener( new MouseEvents.ClickListener() {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent ) {
                setLoading( true );
                getStatus( HadoopUI.getHadoopManager().startJobTracker( cluster ) );
            }
        } );

        stopButton.addClickListener( new MouseEvents.ClickListener() {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent ) {
                setLoading( true );
                getStatus( HadoopUI.getHadoopManager().stopJobTracker( cluster ) );
            }
        } );

        restartButton.addClickListener( new MouseEvents.ClickListener() {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent ) {
                setLoading( true );
                getStatus( HadoopUI.getHadoopManager().restartJobTracker( cluster ) );
            }
        } );

        getStatus( null );
    }


    @Override
    protected void getStatus( UUID trackID ) {
        setLoading( true );
        for ( ClusterNode slaveNode : slaveNodes ) {
            slaveNode.setLoading( true );
        }

        HadoopUI.getExecutor().execute( new CheckTask( NodeType.JOBTRACKER, cluster, new CompleteEvent() {

            public void onComplete( NodeState state ) {
                synchronized ( progressButton ) {
                    boolean isRunning = false;
                    if ( state == NodeState.RUNNING ) {
                        isRunning = true;
                    }
                    else if ( state == NodeState.STOPPED ) {
                        isRunning = false;
                    }

                    startButton.setEnabled( !isRunning );
                    restartButton.setEnabled( isRunning );
                    stopButton.setEnabled( isRunning );

                    for ( ClusterNode slaveNode : slaveNodes ) {
                        slaveNode.getStatus( null );
                    }

                    setLoading( false );
                }
            }
        }, trackID, cluster.getJobTracker() ) );
    }


    @Override
    protected void setLoading( boolean isLoading ) {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        restartButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
    }
}
