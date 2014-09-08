package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;


/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends ClusterNode {

    public SecondaryNameNode( HadoopClusterConfig cluster ) {
        super( cluster );
        setHostname( cluster.getSecondaryNameNode().getHostname() );

        restartButton.setVisible( false );
        startButton.setEnabled( false );
        stopButton.setEnabled( false );

        getStatus( null );
    }


    @Override
    protected void getStatus( UUID trackID ) {
        setLoading( true );

        HadoopUI.getExecutor().execute( new CheckTask( cluster, new CompleteEvent() {

            public void onComplete( NodeState state ) {
                synchronized ( progressButton ) {
                    boolean isRunning = false;
                    if ( state == NodeState.RUNNING ) {
                        isRunning = true;
                    }
                    else if ( state == NodeState.STOPPED ) {
                        isRunning = false;
                    }

                    setLoading( false );
                    startButton.setVisible( isRunning );
                    stopButton.setVisible( !isRunning );
                }
            }
        }, trackID, cluster.getSecondaryNameNode() ) );
    }


    @Override
    protected void setLoading( boolean isLoading ) {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
    }
}
