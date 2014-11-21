package org.safehaus.subutai.core.executor.impl;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class ResponseWrapperTest
{

    @Test
    public void testGetResponse() throws Exception
    {
        ResponseImpl response = mock( ResponseImpl.class );

        ResponseWrapper responseWrapper = new ResponseWrapper( response );

        assertEquals( response, responseWrapper.getResponse() );
    }
}
