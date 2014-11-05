package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.peer.api.PeerManager;


/**
 * Created by bahadyr on 11/5/14.
 */
public class EnvironmentBuilderThread extends Observable implements Runnable
{
    EnvironmentBuilderImpl environmentBuilder;
    List<ContainerDistributionMessage> messages;
    PeerManager peerManager;
    ExecutorService executorService;


    public EnvironmentBuilderThread( final EnvironmentBuilderImpl environmentBuilder,
                                     final List<ContainerDistributionMessage> messages, PeerManager peerManager )
    {
        this.environmentBuilder = environmentBuilder;
        this.messages = messages;
        this.peerManager = peerManager;
        this.executorService = Executors.newCachedThreadPool();
    }


    @Override
    public void run()
    {
        for ( ContainerDistributionMessage message : messages )
        {
            ContainerCreatorThread creatorThread = new ContainerCreatorThread( message, peerManager );
            executorService.execute( creatorThread );
        }
    }
}
