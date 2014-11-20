package org.safehaus.subutai.core.command.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * Test for AgentRequestBuilder
 */
public class AgentRequestBuilderTest
{
    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgent()
    {
        new AgentRequestBuilder( null, "cmd" );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullOrEmptyCmd()
    {
        new AgentRequestBuilder( null, null );
    }


    @Test
    public void shouldReturnAgent()
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        AgentRequestBuilder requestBuilder = new AgentRequestBuilder( agent, "cmd" );

        assertEquals( agent, requestBuilder.getAgent() );
    }


    @Ignore
    @Test
    public void shouldBuildRequest()
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        UUID taskUUID = UUIDUtil.generateTimeBasedUUID();
        AgentRequestBuilder requestBuilder = new AgentRequestBuilder( agent, "cmd" );

        Request request = requestBuilder.build( taskUUID );

        assertEquals( taskUUID, request.getUuid() );
    }


    @Test
    public void testEquals()
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        AgentRequestBuilder requestBuilder1 = new AgentRequestBuilder( agent, "cmd" );
        AgentRequestBuilder requestBuilder2 = new AgentRequestBuilder( agent, "cmd" );
        AgentRequestBuilder requestBuilder3 = new AgentRequestBuilder( agent, "other cmd" );

        assertEquals( requestBuilder1, requestBuilder2 );
        assertNotEquals( requestBuilder1, requestBuilder3 );
    }


    @Test
    public void testHashCode()
    {
        Agent agent = MockUtils.getAgent( UUIDUtil.generateTimeBasedUUID() );
        AgentRequestBuilder requestBuilder1 = new AgentRequestBuilder( agent, "cmd" );
        AgentRequestBuilder requestBuilder2 = new AgentRequestBuilder( agent, "cmd" );
        Map<AgentRequestBuilder, AgentRequestBuilder> map = new HashMap<>();
        map.put( requestBuilder1, requestBuilder1 );

        assertEquals( requestBuilder2, map.get( requestBuilder2 ) );
    }
}
