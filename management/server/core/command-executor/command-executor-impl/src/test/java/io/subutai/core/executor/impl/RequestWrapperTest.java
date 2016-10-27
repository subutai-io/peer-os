package io.subutai.core.executor.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.Request;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class RequestWrapperTest
{

    @Mock
    Request request;

    RequestWrapper requestWrapper;


    @Before
    public void setUp() throws Exception
    {
        requestWrapper = new RequestWrapper( request );
    }


    @Test
    public void testGetRequest() throws Exception
    {
        assertEquals( request, requestWrapper.getRequest() );
    }
}
