package io.subutai.core.identity.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.shiro.session.Session;

import io.subutai.core.identity.impl.SubutaiSessionListener;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiSessionListenerTest
{
    private SubutaiSessionListener subutaiSessionListener;

    @Mock
    Session session;

    @Before
    public void setUp() throws Exception
    {
        subutaiSessionListener = new SubutaiSessionListener();
    }


    @Test
    public void testOnStart() throws Exception
    {
         subutaiSessionListener.onStart( session );
    }


    @Test
    public void testOnStop() throws Exception
    {
        subutaiSessionListener.onStop( session );
    }


    @Test
    public void testOnExpiration() throws Exception
    {
        subutaiSessionListener.onExpiration( session );
    }
}