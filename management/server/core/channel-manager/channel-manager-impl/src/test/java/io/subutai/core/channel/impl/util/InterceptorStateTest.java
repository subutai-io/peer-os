package io.subutai.core.channel.impl.util;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.channel.impl.interceptor.InterceptorStateHelper;

import static junit.framework.TestCase.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class InterceptorStateTest
{

    InterceptorState interceptorState;


    @Test
    public void testIsActiveClientOut() throws Exception
    {
        interceptorState = InterceptorState.CLIENT_OUT;

        assertTrue( interceptorState.isActive( InterceptorStateHelper.getMessage( InterceptorState.CLIENT_OUT ) ) );
    }


    @Test
    public void testIsActiveClientIn() throws Exception
    {
        interceptorState = InterceptorState.CLIENT_IN;

        assertTrue( interceptorState.isActive( InterceptorStateHelper.getMessage( InterceptorState.CLIENT_IN ) ) );
    }


    @Test
    public void testIsActiveServiceOut() throws Exception
    {
        interceptorState = InterceptorState.SERVER_OUT;

        assertTrue( interceptorState.isActive( InterceptorStateHelper.getMessage( InterceptorState.SERVER_OUT ) ) );
    }


    @Test
    public void testIsActiveServiceIn() throws Exception
    {
        interceptorState = InterceptorState.SERVER_IN;

        assertTrue( interceptorState.isActive( InterceptorStateHelper.getMessage( InterceptorState.SERVER_IN ) ) );
    }
}
