package io.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.impl.entity.RestEndpointScopeEntity;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class RestEndpointScopeEntityTest
{
    private RestEndpointScopeEntity restEndpointScopeEntity;


    @Before
    public void setUp() throws Exception
    {
        restEndpointScopeEntity = new RestEndpointScopeEntity( "restEndPoint" );
    }


    @Test
    public void testGetRestEndpoint() throws Exception
    {
        assertNotNull( restEndpointScopeEntity.getRestEndpoint() );
    }


    @Test
    public void testGetPort() throws Exception
    {
        restEndpointScopeEntity.getPort();
    }
}