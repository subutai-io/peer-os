package org.safehaus.subutai.core.agent.impl;


import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentListener;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AgentNotifier
 */
public class AgentNotifierTest
{
    private AgentNotifier agentNotifier;
    private AgentListener agentListener;
    private Set<Agent> agents;
    private Queue<AgentListener> agentListenerQueue;


    @Test( expected = NullPointerException.class )
    public void constructorShouldFail()
    {
        new AgentNotifier( null );
    }


    @Before
    public void setUp()
    {
        agentListener = mock( AgentListener.class );
        agentListenerQueue = new ConcurrentLinkedQueue<>();
        agentListenerQueue.add( agentListener );
        UUID agentId = UUIDUtil.generateTimeBasedUUID();
        ;
        AgentManagerImpl agentManager = mock( AgentManagerImpl.class );
        when( agentManager.isNotifyAgentListeners() ).thenReturn( true );
        agents = MockUtils.getAgents( agentId );
        when( agentManager.getAgents() ).thenReturn( agents );
        when( agentManager.getListenersQueue() ).thenReturn( agentListenerQueue );
        agentNotifier = new AgentNotifier( agentManager );
    }


    @Test
    public void shouldNotifyListeners() throws InterruptedException
    {


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


    @Test
    public void shouldRemoveListenerOnError() throws InterruptedException
    {

        Mockito.doThrow( new RuntimeException() ).when( agentListener ).onAgent( anySet() );


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

        assertFalse( agentListenerQueue.contains( agentListener ) );
    }
}
