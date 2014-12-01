package org.safehaus.subutai.core.executor.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.Request;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandExecutorImplTest
{
    private static final UUID HOST_ID = UUID.randomUUID();
    @Mock
    Broker broker;
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
        commandExecutor = new CommandExecutorImpl( broker, hostRegistry );
        commandExecutor.commandProcessor = commandProcessor;
        when( requestBuilder.build( HOST_ID ) ).thenReturn( request );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new CommandExecutorImpl( null, hostRegistry );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new CommandExecutorImpl( broker, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testInit() throws Exception
    {
        commandExecutor.init();

        verify( broker ).addByteMessageListener( commandProcessor );

        doThrow( new BrokerException( "" ) ).when( broker ).addByteMessageListener( commandProcessor );

        try
        {
            commandExecutor.init();
            fail( "Expected CommandExecutorException" );
        }
        catch ( CommandExecutorException e )
        {
        }
    }


    @Test
    public void testDispose() throws Exception
    {
        commandExecutor.dispose();

        verify( broker ).removeMessageListener( commandProcessor );
    }


    @Test
    public void testExecute() throws Exception
    {
        commandExecutor.execute( HOST_ID, requestBuilder );

        verify( commandProcessor ).execute( any( Request.class ), any( CommandCallback.class ) );
        verify( commandProcessor ).getResult( any( UUID.class ) );
    }


    @Test
    public void testExecute2() throws Exception
    {
        commandExecutor.execute( HOST_ID, requestBuilder, callback );

        verify( commandProcessor ).execute( any( Request.class ), eq( callback ) );
        verify( commandProcessor ).getResult( any( UUID.class ) );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        commandExecutor.executeAsync( HOST_ID, requestBuilder );

        verify( commandProcessor ).execute( any( Request.class ), isA( DummyCallback.class ) );
    }


    @Test
    public void testExecuteAsync2() throws Exception
    {
        commandExecutor.executeAsync( HOST_ID, requestBuilder, callback );

        verify( commandProcessor ).execute( any( Request.class ), eq( callback ) );
    }
}
