package io.subutai.core.identity.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.impl.UserToken;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class UserTokenTest
{
    private UserToken userToken;


    @Before
    public void setUp() throws Exception
    {
        userToken = new UserToken( "userName" );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( userToken.getPrincipal() );
        assertNull( userToken.getCredentials() );
    }
}