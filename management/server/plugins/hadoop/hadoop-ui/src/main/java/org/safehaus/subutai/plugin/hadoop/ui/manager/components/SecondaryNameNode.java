package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.common.api.NodeType;


/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends ClusterNode
{
    private final ExecutorService executorService;
    private final Hadoop hadoop;
    private final Tracker tracker;


    public SecondaryNameNode( Hadoop hadoop, Tracker tracker, final ExecutorService executorService,
                              HadoopClusterConfig cluster )
    {
        super( cluster );
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.executorService = executorService;
        setHostname( cluster.getSecondaryNameNode().getHostname() );

        restartButton.setVisible( false );
        restartButton.setId("secondaryRestart");
        startButton.setEnabled( false );
        startButton.setId("secondaryStart");
        stopButton.setEnabled( false );
        stopButton.setId("secondaryStop");

        getStatus( null );
    }


    @Override
    protected void getStatus( UUID trackID )
    {
        setLoading( true );

        executorService.execute( new CheckTask( hadoop, tracker, NodeType.SECONDARY_NAMENODE, cluster, new CompleteEvent() {

            public void onComplete( NodeState state )
            {
                synchronized ( progressButton )
                {
                    boolean isRunning = false;
                    if ( state == NodeState.RUNNING )
                    {
                        isRunning = true;
                    }
                    else if ( state == NodeState.STOPPED )
                    {
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
    protected void setLoading( boolean isLoading )
    {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
        progressButton.setId("secondaryProgress");
    }
}
