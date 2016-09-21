package io.subutai.core.executor.impl;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.collect.Sets;

import io.subutai.common.cache.EntryExpiryCallback;
import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.Request;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.HeartbeatListener;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.security.api.SecurityManager;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandProcessorTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final String RESPONSE_JSON = String.format(
            " { response: {" + "      \"type\":\"EXECUTE_RESPONSE\"," + "      \"id\":\"%s\","
                    + "      \"commandId\":\"%s\"," + "      \"pid\":123," + "      \"responseNumber\":2,"
                    + "      \"stdOut\":\"output\"," + "      \"stdErr\":\"err\"," + "      \"exitCode\" : 0" + "  } }",
            HOST_ID, COMMAND_ID.toString() );
    private static final String MSG = "MSG";

    @Mock
    HostRegistry hostRegistry;
    @Mock
    ExpiringCache commands;
    @Mock
    CommandProcess process;
    @Mock
    ResourceHostInfo resourceHostInfo;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    Request request;
    @Mock
    CommandCallback callback;
    @Mock
    User user;
    @Mock
    Session session;
    @Mock
    IdentityManager identityManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    HeartbeatListener heartbeatListener;
    @Mock
    Set<HeartbeatListener> listeners;
    @Mock
    HeartBeat heartBeat;
    @Mock
    JsonUtil jsonUtil;
    @Mock
    ExecutorService notifierPool;
    @Mock
    IPUtil ipUtil;

    CommandProcessor commandProcessor;


    @Before
    public void setUp() throws Exception
    {
        commandProcessor = spy( new CommandProcessor( hostRegistry, identityManager ) );
        commandProcessor.commands = commands;
        commandProcessor.listeners = listeners;
        commandProcessor.jsonUtil = jsonUtil;
        commandProcessor.notifierPool = notifierPool;
        commandProcessor.ipUtil = ipUtil;
        doThrow( new HostDisconnectedException( "" ) ).when( hostRegistry ).getResourceHostInfoById( HOST_ID );
        when( hostRegistry.getContainerHostInfoById( HOST_ID ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getResourceHostByContainerHost( containerHostInfo ) ).thenReturn( resourceHostInfo );
        when( request.getId() ).thenReturn( HOST_ID );
        when( resourceHostInfo.getId() ).thenReturn( HOST_ID );
        when( request.getCommandId() ).thenReturn( COMMAND_ID );
        doReturn( session ).when( commandProcessor ).getActiveSession();
    }


    @Test
    public void testConstructor() throws Exception
    {

        try
        {

            new CommandProcessor( null, identityManager );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {

            new CommandProcessor( hostRegistry, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void addListener() throws Exception
    {

        commandProcessor.addListener( heartbeatListener );

        verify( listeners ).add( heartbeatListener );
    }


    @Test
    public void removeListener() throws Exception
    {

        commandProcessor.removeListener( heartbeatListener );

        verify( listeners ).remove( heartbeatListener );
    }


    @Test
    public void testHandleHeartbeat() throws Exception
    {

        commandProcessor.listeners = Sets.newHashSet( heartbeatListener );

        commandProcessor.handleHeartbeat( heartBeat );

        verify( notifierPool ).submit( isA( Runnable.class ) );
    }


    @Test
    public void testRemove() throws Exception
    {

        commandProcessor.remove( request );

        verify( commands ).remove( COMMAND_ID );
    }


    @Test
    public void testGetResult() throws Exception
    {
        try
        {
            commandProcessor.getResult( COMMAND_ID );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        when( commands.get( COMMAND_ID ) ).thenReturn( process );

        commandProcessor.getResult( COMMAND_ID );

        verify( process ).waitResult();
    }


    @Test
    public void testExecute() throws Exception
    {

        doThrow( new HostDisconnectedException( "" ) ).when( commandProcessor ).getResourceHostInfo( HOST_ID );

        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        doReturn( resourceHostInfo ).when( commandProcessor ).getResourceHostInfo( HOST_ID );

        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        doReturn( resourceHostInfo ).when( commandProcessor ).getResourceHostInfo( HOST_ID );
        doReturn( true ).when( commands ).put( anyObject(), anyObject(), anyLong(), any( EntryExpiryCallback.class ) );
        doReturn( "" ).when( commandProcessor ).encrypt( anyString(), anyString() );

        commandProcessor.execute( request, callback );

        verify( commandProcessor ).queueRequest( eq( resourceHostInfo ), eq( request ) );

        doThrow( new NamingException() ).when( commandProcessor ).queueRequest( resourceHostInfo, request );

        try
        {
            commandProcessor.execute( request, callback );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
    }


    @Test
    public void testGetRequests() throws Exception
    {
        Set<String> requests = commandProcessor.getRequests( HOST_ID );

        assertNotNull( requests );
    }


    @Test
    public void testEncrypt() throws Exception
    {

        doReturn( securityManager ).when( commandProcessor ).getSecurityManager();

        commandProcessor.encrypt( MSG, HOST_ID );

        verify( securityManager ).signNEncryptRequestToHost( MSG, HOST_ID );
    }


    @Test
    public void testNotifyAgent() throws Exception
    {

        WebClient webClient = mock( WebClient.class );
        Response response = mock( Response.class );

        doReturn( "IP" ).when( commandProcessor ).getResourceHostIp( resourceHostInfo );
        doReturn( webClient ).when( commandProcessor ).getWebClient( resourceHostInfo );
        doReturn( response ).when( webClient ).form( any( Form.class ) );
        doReturn( Response.Status.ACCEPTED.getStatusCode() ).when( response ).getStatus();

        commandProcessor.notifyAgent( resourceHostInfo );

        verify( hostRegistry ).updateResourceHostEntryTimestamp( HOST_ID );
    }


    @Test
    public void testDispose() throws Exception
    {
        commandProcessor.dispose();

        verify( commands ).dispose();

        verify( notifierPool ).shutdown();
    }


    @Test
    public void testHandleResponse() throws Exception
    {
        io.subutai.common.command.Response response = mock( io.subutai.common.command.Response.class );
        doReturn( resourceHostInfo ).when( commandProcessor ).getResourceHostInfo( anyString() );


        commandProcessor.handleResponse( response );

        verify( jsonUtil ).to( response );

        doReturn( process ).when( commands ).get( anyString() );

        commandProcessor.handleResponse( response );

        verify( hostRegistry, times( 2 ) ).updateResourceHostEntryTimestamp( anyString() );
    }


    @Test
    public void testGetResourcehostIp() throws Exception
    {
        HostInterfaces hostInterfaces = mock( HostInterfaces.class );
        HostInterface hostInterface = mock( HostInterface.class );
        Set<HostInterface> ifaces = Sets.newHashSet( hostInterface );

        doReturn( hostInterfaces ).when( resourceHostInfo ).getHostInterfaces();
        doReturn( ifaces ).when( hostInterfaces ).getAll();
        doReturn( hostInterface ).when( ipUtil ).findAddressableIface( eq( ifaces ), anyString() );


        commandProcessor.getResourceHostIp( resourceHostInfo );

        verify( hostInterface ).getIp();
    }
}
