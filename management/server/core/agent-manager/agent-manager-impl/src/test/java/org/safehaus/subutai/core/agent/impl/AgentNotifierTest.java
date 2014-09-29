package org.safehaus.subutai.core.agent.impl;


import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentListener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by dilshat on 9/26/14.
 */
public class AgentNotifierTest
{

    @Test
    public void shouldNotifyListeners() throws InterruptedException
    {
        AgentListener agentListener = mock( AgentListener.class );
        Queue<AgentListener> agentListenerQueue = new ConcurrentLinkedQueue<>();
        agentListenerQueue.add( agentListener );
        UUID agentId = UUID.randomUUID();
        AgentManagerImpl agentManager = mock( AgentManagerImpl.class );
        when( agentManager.isNotifyAgentListeners() ).thenReturn( true );
        Set<Agent> agents = MockUtils.getAgents( agentId );
        when( agentManager.getAgents() ).thenReturn( agents );
        when( agentManager.getListenersQueue() ).thenReturn( agentListenerQueue );

        final AgentNotifier agentNotifier = new AgentNotifier( agentManager );

        Thread t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                agentNotifier.run();
            }
        } );
        t.start();

        Thread.sleep( 500 );

        verify( agentListener ).onAgent( agents );
    }
}
