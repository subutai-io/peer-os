package io.subutai.core.executor.impl;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;

import io.subutai.core.executor.impl.CommandProcess;
import io.subutai.core.executor.impl.CommandProcessor;
import io.subutai.core.executor.impl.ResponseProcessor;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ResponseProcessorTest
{
    @Mock
    CommandProcess commandProcess;
    @Mock
    CommandProcessor commandProcessor;
    @Mock
    Response response;
    @Mock
    CommandCallback callback;

    ResponseProcessor responseProcessor;


    @Before
    public void setUp() throws Exception
    {
        responseProcessor = new ResponseProcessor( response, commandProcess, commandProcessor );
        when( commandProcess.getCallback() ).thenReturn( callback );
        when( commandProcess.isDone() ).thenReturn( true );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new ResponseProcessor( null, commandProcess, commandProcessor );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new ResponseProcessor( response, null, commandProcessor );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new ResponseProcessor( response, commandProcess, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testRun() throws Exception
    {

        responseProcessor.run();

        verify( commandProcess ).appendResponse( response );
        verify( commandProcess ).getCallback();
        verify( callback ).onResponse( eq( response ), any( CommandResult.class ) );
        verify( commandProcess ).isDone();
        verify( commandProcessor ).remove( any( UUID.class ) );
        verify( commandProcess ).stop();


        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( commandProcess ).isDone();

        responseProcessor.run();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
