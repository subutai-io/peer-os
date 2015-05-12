package org.safehaus.subutai.common.security;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiLoginContextTest
{
    private SubutaiLoginContext context;


    @Before
    public void setUp() throws Exception
    {
        context = new SubutaiLoginContext( "sessionId", "userName", "remoteAddress" );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( context.getRemoteAddress() );
        assertNotNull( context.getSessionId() );
        assertNotNull( context.getUsername() );
        assertNotNull( context.toString() );
    }
}