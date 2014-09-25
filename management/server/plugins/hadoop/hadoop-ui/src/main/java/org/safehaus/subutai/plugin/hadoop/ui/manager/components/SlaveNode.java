package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * Created by daralbaev on 12.04.14.
 */
public class SlaveNode extends ClusterNode
{

    private final ExecutorService executorService;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private Agent agent;
    private boolean isDataNode;


    public SlaveNode( Hadoop hadoop, Tracker tracker, ExecutorService executorService, HadoopClusterConfig cluster,
                      Agent agent, boolean isDataNode )
    {

        super( cluster );
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.executorService = executorService;
        this.agent = agent;
        this.isDataNode = isDataNode;

        setHostname( agent.getHostname() );

        restartButton.setVisible( false );
        startButton.setEnabled( false );
        stopButton.setEnabled( false );

        getStatus( null );
    }


    @Override
    protected void getStatus( UUID trackID )
    {
        setLoading( true );

        executorService.execute( new CheckTask( hadoop, tracker, cluster, new CompleteEvent()
        {

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
        }, trackID, agent, isDataNode ) );
    }


    @Override
    protected void setLoading( boolean isLoading )
    {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
    }


    public Agent getAgent()
    {
        return agent;
    }
}
