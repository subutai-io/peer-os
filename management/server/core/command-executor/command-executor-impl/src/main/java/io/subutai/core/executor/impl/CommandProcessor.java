package io.subutai.core.executor.impl;


import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;
import javax.ws.rs.core.Form;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseImpl;
import io.subutai.common.command.ResponseWrapper;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.Topic;
import io.subutai.core.executor.api.RestProcessor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * Executes commands and processes responses
 */
public class CommandProcessor implements ByteMessageListener, RestProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcessor.class.getName() );
    private final HostRegistry hostRegistry;
    private IdentityManager identityManager;

    protected ExpiringCache<UUID, CommandProcess> commands = new ExpiringCache<>();
    protected ExpiringCache<String, Set<Request>> requests = new ExpiringCache<>();


    public CommandProcessor( final HostRegistry hostRegistry, final IdentityManager identityManager )
    {
        Preconditions.checkNotNull( hostRegistry );
        Preconditions.checkNotNull( identityManager );

        this.hostRegistry = hostRegistry;
        this.identityManager = identityManager;
    }


    @Override
    public Topic getTopic()
    {
        return Topic.RESPONSE_TOPIC;
    }


    public void execute( final Request request, CommandCallback callback ) throws CommandException
    {
        //TODO refactor this method when broker is gone

        //find target host
        ResourceHostInfo targetHost;
        try
        {
            targetHost = getTargetHost( request.getId() );
        }
        catch ( HostDisconnectedException e )
        {
            throw new CommandException( e );
        }

        //create command process
        CommandProcess commandProcess = new CommandProcess( this, callback, request, getActiveSession() );
        boolean queued =
                commands.put( request.getCommandId(), commandProcess, Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000,
                        new CommandProcessExpiryCallback() );
        if ( !queued )
        {
            throw new CommandException( "This command is already queued for execution" );
        }


        //send command
        try
        {
            commandProcess.start();

            String command = JsonUtil.toJson( new RequestWrapper( request ) );

            LOG.info( String.format( "Sending:%n%s", command ) );

            //leave this call temporarily to be compatible with MQTT clients
            //todo remove this after REST tested
            getBroker().sendTextMessage( targetHost.getId(), command );

            //add request to outgoing agent queue
            synchronized ( requests )
            {
                Set<Request> hostRequests = requests.get( request.getId() );
                if ( hostRequests == null )
                {
                    hostRequests = Sets.newLinkedHashSet();
                    requests.put( request.getId(), hostRequests, Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000 );
                }
            }

            //notify agent about requests
            //TODO use queue for notifications
            WebClient webClient = null;
            try
            {
                ResourceHostInfo resourceHostInfo = getResourceHostInfo( request.getId() );

                webClient = getWebClient( request, resourceHostInfo );

                webClient.form( new Form() );
            }
            catch ( Exception e )
            {
                //ignore for now
            }
            finally
            {
                if ( webClient != null )
                {
                    try
                    {
                        webClient.close();
                    }
                    catch ( Exception ignore )
                    {
                        //ignore
                        LOG.warn( "Error disposing web client", ignore );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            remove( request );

            commandProcess.stop();

            throw new CommandException( e );
        }
    }


    protected WebClient getWebClient( Request request, ResourceHostInfo resourceHostInfo )
    {
        return RestUtil.createTrustedWebClientWithAuth(
                String.format( "https://%s:%d/execute/%s", getResourceHostIp( resourceHostInfo ),
                        SystemSettings.getAgentPort(), request.getId() ), resourceHostInfo.getId() );
    }


    protected String getResourceHostIp( ResourceHostInfo resourceHostInfo )
    {
        return resourceHostInfo.getHostInterfaces().findByName( SystemSettings.getExternalIpInterface() ).getIp();
    }


    protected ResourceHostInfo getResourceHostInfo( String requestHostId ) throws HostDisconnectedException
    {
        try
        {
            return hostRegistry.getResourceHostInfoById( requestHostId );
        }
        catch ( HostDisconnectedException e )
        {
            ContainerHostInfo containerHostInfo = hostRegistry.getContainerHostInfoById( requestHostId );

            return hostRegistry.getResourceHostByContainerHost( containerHostInfo );
        }
    }


    //todo move this method to SecurityManager
    protected String encrypt( String message ) throws PGPException, NamingException
    {
        if ( SystemSettings.getEncryptionState() )
        {

            EncryptionTool encryptionTool = getSecurityManager().getEncryptionTool();

            RequestImplWrapper requestImplWrapper = JsonUtil.fromJson( message, RequestImplWrapper.class );

            Request originalRequest = requestImplWrapper.getRequest();

            //obtain target host pub key for encrypting
            PGPPublicKey hostKeyForEncrypting =
                    getSecurityManager().getKeyManager().getPublicKey( originalRequest.getId() );

            String encryptedRequestString = new String( encryptionTool
                    .signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), hostKeyForEncrypting, true ) );

            EncryptedRequestWrapper encryptedRequestWrapper =
                    new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

            return JsonUtil.toJson( encryptedRequestWrapper );
        }

        return message;
    }


    protected SecurityManager getSecurityManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( SecurityManager.class );
    }


    protected Broker getBroker() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( Broker.class );
    }


    protected Session getActiveSession()
    {
        return identityManager.getActiveSession();
    }


    public CommandResult getResult( UUID commandId ) throws CommandException
    {
        Preconditions.checkNotNull( commandId );

        CommandProcess commandProcess = commands.get( commandId );
        if ( commandProcess != null )
        {
            //wait until process completes  & return result
            return commandProcess.waitResult();
        }
        else
        {
            throw new CommandException( String.format( "Command process not found by id: %s", commandId ) );
        }
    }


    @Override
    public void handleResponse( final Response response )
    {
        try
        {
            Preconditions.checkNotNull( response );

            CommandProcess commandProcess = commands.get( response.getCommandId() );

            if ( commandProcess != null )
            {
                //process response
                commandProcess.processResponse( response );
            }
            else
            {
                LOG.warn( String.format( "Callback not found for response: %s", response ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing response", e );
        }
    }


    @Override
    public Set<Request> getRequests( final String hostId )
    {
        Set<Request> hostRequests = requests.remove( hostId );

        return hostRequests == null ? Sets.<Request>newHashSet() : hostRequests;
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String responseString = new String( message, "UTF-8" );

            ResponseWrapper responseWrapper = JsonUtil.fromJson( responseString, ResponseWrapper.class );

            LOG.info( String.format( "Received:%n%s", JsonUtil.toJson( responseWrapper ) ) );

            ResponseImpl response = responseWrapper.getResponse();

            handleResponse( response );
        }
        catch ( Exception e )
        {
            LOG.error( "Error parsing response", e );
        }
    }


    protected ResourceHostInfo getTargetHost( String hostId ) throws HostDisconnectedException
    {
        ResourceHostInfo targetHost;

        try
        {
            targetHost = hostRegistry.getResourceHostInfoById( hostId );
        }
        catch ( HostDisconnectedException e )
        {
            ContainerHostInfo containerHostInfo = hostRegistry.getContainerHostInfoById( hostId );
            if ( containerHostInfo.getState() != ContainerHostState.RUNNING )
            {
                throw new HostDisconnectedException(
                        String.format( "Container state is %s", containerHostInfo.getState() ) );
            }
            targetHost = hostRegistry.getResourceHostByContainerHost( containerHostInfo );
        }

        return targetHost;
    }


    protected void remove( Request request )
    {
        Preconditions.checkNotNull( request );

        commands.remove( request.getCommandId() );

        synchronized ( requests )
        {
            Set<Request> hostRequests = requests.get( request.getId() );
            if ( !CollectionUtil.isCollectionEmpty( hostRequests ) )
            {
                hostRequests.remove( request );
            }
        }
    }


    public void dispose()
    {
        commands.dispose();

        requests.dispose();
    }
}
