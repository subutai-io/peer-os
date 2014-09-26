package org.safehaus.subutai.core.command.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import static org.junit.Assert.assertEquals;


/**
 * Created by dilshat on 9/26/14.
 */
public class AgentRequestBuilderTest
{
    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgent()
    {
        RequestBuilder requestBuilder = new AgentRequestBuilder( null, "cmd" );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullOrEmptyCmd()
    {
        RequestBuilder requestBuilder = new AgentRequestBuilder( null, null );
    }


    @Test
    public void shouldReturnAgent()
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        AgentRequestBuilder requestBuilder = new AgentRequestBuilder( agent, "cmd" );

        assertEquals( agent, requestBuilder.getAgent() );
    }


    @Test
    public void shouldBuildRequest()
    {
        Agent agent = MockUtils.getAgent( UUID.randomUUID() );
        UUID taskUUID = UUID.randomUUID();
        AgentRequestBuilder requestBuilder = new AgentRequestBuilder( agent, "cmd" );

        Request request = requestBuilder.build( taskUUID );

        assertEquals( taskUUID, request.getTaskUuid() );
    }
}
