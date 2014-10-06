package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for CommandImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class CommandImplTest
{
    @Mock
    CommandRunnerBase commandRunnerBase;
    @Mock
    PeerManager peerManager;
    private static final UUID localPeerId = UUID.randomUUID();
    private static final UUID remotePeerId = UUID.randomUUID();
    private static final UUID agentId = UUID.randomUUID();
    private static final UUID agentId2 = UUID.randomUUID();
    private static final UUID environmentId = UUID.randomUUID();


    @Test
    public void testRemoteCommand()
    {
        RequestBuilder requestBuilder = mock( RequestBuilder.class );
        when( peerManager.getSiteId() ).thenReturn( localPeerId );
        Container container = mock( Container.class );
        Container container2 = mock( Container.class );
        when( container.getPeerId() ).thenReturn( remotePeerId );
        when( container2.getPeerId() ).thenReturn( remotePeerId );
        when( container.getAgentId() ).thenReturn( agentId );
        when( container2.getAgentId() ).thenReturn( agentId2 );
        when( container.getEnvironmentId() ).thenReturn( environmentId );
        when( container2.getEnvironmentId() ).thenReturn( environmentId );
        Request request = mock( Request.class );
        when( requestBuilder.build( any( UUID.class ), any( UUID.class ) ) ).thenReturn( request );

        CommandImpl command =
                new CommandImpl( requestBuilder, Sets.newHashSet( container ), peerManager, commandRunnerBase );
    }


    @Test
    public void testRemoteCommand2()
    {
        RequestBuilder requestBuilder = mock( RequestBuilder.class );
        when( peerManager.getSiteId() ).thenReturn( localPeerId );
        Agent container = mock( Agent.class );
        Agent container2 = mock( Agent.class );
        when( container.getSiteId() ).thenReturn( remotePeerId );
        when( container2.getSiteId() ).thenReturn( remotePeerId );
        when( container.getUuid() ).thenReturn( agentId );
        when( container2.getUuid() ).thenReturn( agentId2 );
        when( container.getEnvironmentId() ).thenReturn( environmentId );
        when( container2.getEnvironmentId() ).thenReturn( environmentId );
        Request request = mock( Request.class );
        when( requestBuilder.build( any( UUID.class ), any( UUID.class ) ) ).thenReturn( request );

        CommandImpl command =
                new CommandImpl( "", requestBuilder, Sets.newHashSet( container ), peerManager, commandRunnerBase );
    }
}
