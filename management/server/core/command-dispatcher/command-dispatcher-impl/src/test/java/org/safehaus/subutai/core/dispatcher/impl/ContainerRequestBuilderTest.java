package org.safehaus.subutai.core.dispatcher.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.dispatcher.api.ContainerRequestBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for ContainerRequestBuilder
 */
public class ContainerRequestBuilderTest
{
    private static final String COMMAND = "pwd";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullContainer()
    {
        new ContainerRequestBuilder( null, COMMAND );
    }


    @Test
    public void testGetContainer() throws Exception
    {
        Container container = mock( Container.class );
        ContainerRequestBuilder containerRequestBuilder = new ContainerRequestBuilder( container, COMMAND );

        assertEquals( container, containerRequestBuilder.getContainer() );
    }


    @Test
    public void testEquals() throws Exception
    {
        Container container = mock( Container.class );
        ContainerRequestBuilder containerRequestBuilder = new ContainerRequestBuilder( container, COMMAND );
        ContainerRequestBuilder containerRequestBuilder2 = new ContainerRequestBuilder( container, COMMAND );

        assertEquals( containerRequestBuilder, containerRequestBuilder2 );
        assertEquals( containerRequestBuilder, containerRequestBuilder );
    }


    @Test
    public void testNotEquals() throws Exception
    {
        Container container = mock( Container.class );
        ContainerRequestBuilder containerRequestBuilder = new ContainerRequestBuilder( container, COMMAND );
        ContainerRequestBuilder containerRequestBuilder2 =
                new ContainerRequestBuilder( mock( Container.class ), COMMAND );

        assertNotEquals( containerRequestBuilder, containerRequestBuilder2 );
    }


    @Test
    public void testHashCode() throws Exception
    {
        Container container = mock( Container.class );

        Map<ContainerRequestBuilder, ContainerRequestBuilder> map = new HashMap<>();
        ContainerRequestBuilder containerRequestBuilder = new ContainerRequestBuilder( container, COMMAND );
        ContainerRequestBuilder containerRequestBuilder2 = new ContainerRequestBuilder( container, COMMAND );

        map.put( containerRequestBuilder, containerRequestBuilder );

        assertEquals( containerRequestBuilder2, map.get( containerRequestBuilder ) );
    }


    @Test
    public void testBuild() throws Exception
    {
        UUID commandId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        Container container = mock( Container.class );
        when( container.getAgentId() ).thenReturn( agentId );
        ContainerRequestBuilder containerRequestBuilder = new ContainerRequestBuilder( container, COMMAND );

        Request request = containerRequestBuilder.build( commandId );

        assertEquals( agentId, request.getUuid() );
    }
}
