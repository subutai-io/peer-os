package io.subutai.core.messenger.rest;


import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.MessageProcessor;
import io.subutai.core.messenger.rest.RestServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    @Mock
    MessageProcessor messageProcessor;
    RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl( messageProcessor );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new RestServiceImpl( null );
    }


    @Test
    public void testProcessMessage() throws Exception
    {
        Response response = restService.processMessage( "" );

        assertEquals( Response.Status.ACCEPTED.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testProcessMessageException() throws Exception
    {
        doThrow( new MessageException( "" ) ).when( messageProcessor ).processMessage( anyString() );

        Response response = restService.processMessage( "" );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
}
