package io.subutai.core.executor.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.hostregistry.api.HostRegistry;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandExecutorImplTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    @Mock
    HostRegistry hostRegistry;
    @Mock
    CommandProcessor commandProcessor;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    CommandCallback callback;
    @Mock
    Request request;

    CommandExecutorImpl commandExecutor;


    @Before
    public void setUp() throws Exception
    {
        commandExecutor = new CommandExecutorImpl( commandProcessor );
        when( requestBuilder.build( HOST_ID ) ).thenReturn( request );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new CommandExecutorImpl( null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testExecute() throws Exception
    {
        commandExecutor.execute( HOST_ID, requestBuilder );

        verify( commandProcessor ).executeSystemCall( any( Request.class ), any( CommandCallback.class ) );
        verify( commandProcessor ).getResult( any( UUID.class ) );
    }


    @Test
    public void testExecute2() throws Exception
    {
        commandExecutor.execute( HOST_ID, requestBuilder, callback );

        verify( commandProcessor ).executeSystemCall( any( Request.class ), eq( callback ) );
        verify( commandProcessor ).getResult( any( UUID.class ) );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        commandExecutor.executeAsync( HOST_ID, requestBuilder );

        verify( commandProcessor ).executeSystemCall( any( Request.class ), isA( DummyCallback.class ) );
    }


    @Test
    public void testExecuteAsync2() throws Exception
    {
        commandExecutor.executeAsync( HOST_ID, requestBuilder, callback );

        verify( commandProcessor ).executeSystemCall( any( Request.class ), eq( callback ) );
    }
}
