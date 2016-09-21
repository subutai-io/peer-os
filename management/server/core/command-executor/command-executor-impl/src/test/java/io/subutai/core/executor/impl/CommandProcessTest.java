package io.subutai.core.executor.impl;


import java.io.PrintStream;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.Request;
import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseType;
import io.subutai.core.identity.api.model.Session;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//import static junit.framework.Assert.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CommandProcessTest
{
    private static final String OUTPUT = "output";
    private static final Integer EXIT_CODE = 0;
    @Mock
    CommandProcessor commandProcessor;
    @Mock
    CommandCallback callback;
    @Mock
    ExecutorService executor;
    @Mock
    Semaphore semaphore;
    @Mock
    Response response;
    @Mock
    Request request;
    @Mock
    Session session;


    Set<Response> queuedResponses;

    CommandProcess commandProcess;


    @Before
    public void setUp() throws Exception
    {
        commandProcess = new CommandProcess( commandProcessor, callback, request, session );
        commandProcess.executor = executor;
        commandProcess.semaphore = semaphore;
        queuedResponses = spy( Sets.newTreeSet( new Comparator<Response>()
        {
            @Override
            public int compare( final Response o1, final Response o2 )
            {
                return Integer.compare( o1.getResponseNumber(), o2.getResponseNumber() );
            }
        } ) );
        commandProcess.queuedResponses = queuedResponses;
    }


    @Test
    public void testConstructor() throws Exception
    {

        try
        {

            new CommandProcess( null, callback, request, session );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {

            new CommandProcess( commandProcessor, null, request, session );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testWaitResult() throws Exception
    {
        CommandResult result = commandProcess.waitResult();

        verify( semaphore ).acquire();
        assertNotNull( result );
    }


    @Test
    public void testStop() throws Exception
    {

        commandProcess.stop();

        verify( semaphore ).release();
        verify( executor ).shutdown();

        commandProcess.status = CommandStatus.RUNNING;

        commandProcess.stop();

        assertEquals( CommandStatus.TIMEOUT, commandProcess.status );
    }


    @Test
    public void testProcessResponse() throws Exception
    {
        when( response.getResponseNumber() ).thenReturn( 1 );

        Response response1 = mock( Response.class );
        doReturn( 2 ).when( response1 ).getResponseNumber();
        queuedResponses.add( response1 );

        commandProcess.processResponse( response );

        verify( executor, times( 2 ) ).execute( isA( ResponseProcessor.class ) );

        when( response.getResponseNumber() ).thenReturn( 100 );

        commandProcess.processResponse( response );

        verify( queuedResponses ).add( response );
    }


    @Test
    public void testStart() throws Exception
    {
        commandProcess.start();

        assertEquals( CommandStatus.RUNNING, commandProcess.status );

        commandProcess.status = CommandStatus.RUNNING;

        try
        {
            commandProcess.start();
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
    }


    @Test
    public void testIsDone() throws Exception
    {
        boolean done = commandProcess.isDone();

        assertFalse( done );


        commandProcess.status = CommandStatus.FAILED;

        done = commandProcess.isDone();

        assertTrue( done );
    }


    @Test
    public void testGetCallback() throws Exception
    {

        assertEquals( callback, commandProcess.getCallback() );
    }


    @Test
    public void testGetResult() throws Exception
    {

        CommandResult result = commandProcess.getResult();

        assertNotNull( result );
    }


    @Test
    public void testAppendResponse() throws Exception
    {
        when( response.getStdOut() ).thenReturn( OUTPUT );
        when( response.getStdErr() ).thenReturn( OUTPUT );
        when( response.getExitCode() ).thenReturn( EXIT_CODE );

        commandProcess.appendResponse( response );

        verify( response, times( 2 ) ).getStdOut();
        verify( response, times( 2 ) ).getStdErr();
        verify( response ).getExitCode();
        assertEquals( CommandStatus.SUCCEEDED, commandProcess.status );


        reset( response );
        when( response.getStdOut() ).thenReturn( OUTPUT );
        when( response.getStdErr() ).thenReturn( OUTPUT );
        when( response.getExitCode() ).thenReturn( null );
        when( response.getType() ).thenReturn( ResponseType.EXECUTE_TIMEOUT );

        commandProcess.appendResponse( response );

        verify( response, times( 2 ) ).getStdOut();
        verify( response, times( 2 ) ).getStdErr();
        verify( response ).getExitCode();
        assertEquals( CommandStatus.KILLED, commandProcess.status );
    }


    @Test
    public void testProcessNextResponse() throws Exception
    {
        commandProcess.processNextResponse( response );

        verify( executor ).execute( isA( ResponseProcessor.class ) );

        //----------

        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( executor ).execute( any( ResponseProcessor.class ) );

        commandProcess.processNextResponse( response );

        verify( exception ).printStackTrace(any( PrintStream.class) );

        //----------

        reset( executor );
        commandProcess.userSession = null;

        commandProcess.processNextResponse( response );

        verify( executor ).execute( isA( ResponseProcessor.class ) );
        verify( session , times( 2 )).getSubject();
    }
}
