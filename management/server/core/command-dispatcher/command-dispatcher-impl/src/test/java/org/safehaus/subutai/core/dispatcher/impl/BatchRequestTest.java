package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for BatchRequest
 */
public class BatchRequestTest
{
    private static final UUID AGENT_ID = UUID.randomUUID();
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID ENV_ID = UUID.randomUUID();
    private static final String SOURCE = "source";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullRequest()
    {
        new BatchRequest( null, AGENT_ID, ENV_ID );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentId()
    {
        new BatchRequest( mock( Request.class ), null, ENV_ID );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullEnvId()
    {
        new BatchRequest( mock( Request.class ), AGENT_ID, null );
    }


    @Test
    public void shouldReturnSameProperties()
    {
        Request request = mock( Request.class );
        when( request.getTaskUuid() ).thenReturn( COMMAND_ID );
        when( request.getUuid() ).thenReturn( AGENT_ID );
        when( request.getType() ).thenReturn( RequestType.EXECUTE_REQUEST );
        when( request.getSource() ).thenReturn( SOURCE );
        BatchRequest batchRequest = new BatchRequest( request, AGENT_ID, ENV_ID );

        assertEquals( ENV_ID, batchRequest.getEnvironmentId() );
        assertEquals( COMMAND_ID, batchRequest.getCommandId() );
        assertEquals( 1, batchRequest.getRequestsCount() );
        assertTrue( batchRequest.getAgentIds().contains( AGENT_ID ) );
    }


    @Test
    public void shouldAddAgentId()
    {
        Request request = mock( Request.class );
        when( request.getTaskUuid() ).thenReturn( COMMAND_ID );
        when( request.getUuid() ).thenReturn( AGENT_ID );
        when( request.getType() ).thenReturn( RequestType.EXECUTE_REQUEST );
        when( request.getSource() ).thenReturn( SOURCE );
        BatchRequest batchRequest = new BatchRequest( request, AGENT_ID, ENV_ID );

        batchRequest.addAgentId( UUID.randomUUID() );

        assertEquals( 2, batchRequest.getRequestsCount() );
        assertEquals( 2, batchRequest.getRequests().size() );
    }
}
