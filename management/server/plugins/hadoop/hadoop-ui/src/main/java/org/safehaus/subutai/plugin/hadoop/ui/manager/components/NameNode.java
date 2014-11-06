package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.vaadin.event.MouseEvents;


/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNode extends ClusterNode
{

    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;


    public NameNode( final Hadoop hadoop, Tracker tracker, final ExecutorService executorService,
                     final HadoopClusterConfig cluster )
    {
        super( cluster );
        this.executorService = executorService;
        this.hadoop = hadoop;
        this.tracker = tracker;
        //        setHostname( cluster.getNameNode().getHostname() );

        startButton.addClickListener( new MouseEvents.ClickListener()
        {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent )
            {
                setLoading( true );
                getStatus( hadoop.startNameNode( cluster ) );
            }
        } );
        startButton.setId( "nameNodeStart" );

        stopButton.addClickListener( new MouseEvents.ClickListener()
        {
            @Override
            public void click( MouseEvents.ClickEvent clickEvent )
            {
                setLoading( true );
                getStatus( hadoop.stopNameNode( cluster ) );
            }
        } );
        stopButton.setId( "nameNodeStop" );

        //        restartButton.addClickListener( new MouseEvents.ClickListener()
        //        {
        //            @Override
        //            public void click( MouseEvents.ClickEvent clickEvent )
        //            {
        //                setLoading( true );
        //                getStatus( hadoop.restartNameNode( cluster ) );
        //            }
        //        } );
        restartButton.setId( "nameNodeRestart" );

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

        //        executorService.execute( new CheckTask( hadoop, tracker, NodeType.NAMENODE, cluster,
        // new CompleteEvent()
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
        //        }, trackID, cluster.getNameNode() ) );
    }


    @Override
    protected void setLoading( boolean isLoading )
    {
        startButton.setVisible( !isLoading );
        stopButton.setVisible( !isLoading );
        restartButton.setVisible( !isLoading );
        progressButton.setVisible( isLoading );
        progressButton.setId( "nameNodeProgress" );
    }
}
