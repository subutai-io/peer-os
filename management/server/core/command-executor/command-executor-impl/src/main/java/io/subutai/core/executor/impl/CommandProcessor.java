package io.subutai.core.executor.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.cache.CacheEntry;
import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.Response;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.HeartbeatListener;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.settings.Common;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.executor.api.RestProcessor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;


/**
 * Executes commands and processes responses
 */
public class CommandProcessor implements RestProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcessor.class.getName() );
    private static final int NOTIFIER_INTERVAL_MS = 300;
    private static final int EXTRA_TIMEOUT_SEC = 60;
    private final HostRegistry hostRegistry;
    private IdentityManager identityManager;
    ExpiringCache<UUID, CommandProcess> commands = new ExpiringCache<>();
    private ScheduledExecutorService notifier = Executors.newSingleThreadScheduledExecutor();
    ExecutorService notifierPool = Executors.newCachedThreadPool();
    Set<HeartbeatListener> listeners = Collections.newSetFromMap( new ConcurrentHashMap<HeartbeatListener, Boolean>() );

    JsonUtil jsonUtil = new JsonUtil();
    IPUtil ipUtil = new IPUtil();


    public void addListener( HeartbeatListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void removeListener( HeartbeatListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }


    public void dispose()
    {
        commands.dispose();

        notifier.shutdown();

        notifierPool.shutdown();
    }


    public CommandProcessor( final HostRegistry hostRegistry, final IdentityManager identityManager )
    {
        Preconditions.checkNotNull( hostRegistry );
        Preconditions.checkNotNull( identityManager );

        this.hostRegistry = hostRegistry;
        this.identityManager = identityManager;

        notifier.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    notifyAgents();
                }
                catch ( Exception e )
                {
                    LOG.error( "Error in notifier task", e );
                }
            }
        }, 0, NOTIFIER_INTERVAL_MS, TimeUnit.MILLISECONDS );
    }


    @Override
    public void handleHeartbeat( final HeartBeat heartBeat )
    {
        LOG.debug( String.format( "Heartbeat:%n%s", jsonUtil.to( heartBeat ) ) );

        for ( final HeartbeatListener listener : listeners )
        {
            notifierPool.submit( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        listener.onHeartbeat( heartBeat );
                    }
                    catch ( Exception e )
                    {
                        LOG.error( "Error in handleHeartbeat", e );
                    }
                }
            } );
        }
    }


    void execute( final Request request, CommandCallback callback ) throws CommandException
    {
        //find target host
        ResourceHostInfo resourceHostInfo;

        try
        {
            resourceHostInfo = getResourceHostInfo( request.getId() );

            if ( !resourceHostInfo.getId().equals( request.getId() ) )
            {
                //in case the target host is a container, ensure its state is RUNNING, otherwise throw exception
                if ( hostRegistry.getContainerHostInfoById( request.getId() ).getState() != ContainerHostState.RUNNING )
                {
                    throw new CommandException( "Container is not running" );
                }
            }
        }
        catch ( HostDisconnectedException e )
        {
            throw new CommandException( e );
        }


        //create command process
        CommandProcess commandProcess = createCommandProcess( callback, request, resourceHostInfo.getId() );

        boolean queued = commands.put( request.getCommandId(), commandProcess,
                ( request.getTimeout() + EXTRA_TIMEOUT_SEC ) * 1000L, new CommandProcessExpiryCallback() );

        if ( !queued )
        {
            throw new CommandException( "Command id is null " );
        }

        //send command
        try
        {
            commandProcess.start();

            String command = jsonUtil.to( new RequestWrapper( request ) );

            LOG.debug( String.format( "Sending:%n%s", command ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error sending request", e );

            remove( request );

            commandProcess.stop();

            throw new CommandException( e );
        }
    }


    CommandProcess createCommandProcess( CommandCallback callback, Request request, String rhId )
    {
        Session session = getActiveSession();

        if ( session == null )
        {
            LOG.warn( " **** Command:  '" + request.getCommand() + "' is running without user privileges" );
        }

        return new CommandProcess( this, callback, request, rhId, session );
    }


    @Override
    public Set<String> getRequests( final String hostId )
    {
        Set<String> hostRequests = Sets.newHashSet();

        Collection<CacheEntry<CommandProcess>> commandProcessEntries = commands.getEntries().values();

        for ( CacheEntry<CommandProcess> commandProcessEntry : commandProcessEntries )
        {
            CommandProcess commandProcess = commandProcessEntry.getValue();

            if ( commandProcess.getRhId().equals( hostId ) && commandProcess.markAsSent() )
            {
                hostRequests.add( commandProcess.getEncryptedRequest() );
            }
        }

        return hostRequests;
    }


    private void notifyAgents()
    {
        Collection<CacheEntry<CommandProcess>> commandProcessEntries = commands.getEntries().values();

        Set<String> notifiedRhIds = Sets.newHashSet();

        for ( CacheEntry<CommandProcess> commandProcessEntry : commandProcessEntries )
        {
            final CommandProcess commandProcess = commandProcessEntry.getValue();

            if ( !commandProcess.isSent() && !notifiedRhIds.contains( commandProcess.getRhId() ) )
            {
                notifiedRhIds.add( commandProcess.getRhId() );

                notifierPool.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            ResourceHostInfo resourceHostInfo = getResourceHostInfo( commandProcess.getRhId() );

                            notifyAgent( resourceHostInfo );
                        }
                        catch ( Exception e )
                        {
                            //ignore
                        }
                    }
                } );
            }
        }
    }


    void notifyAgent( ResourceHostInfo resourceHostInfo )
    {
        WebClient webClient = null;
        javax.ws.rs.core.Response response = null;

        try
        {
            webClient = getWebClient( resourceHostInfo );

            response = webClient.form( new Form() );

            if ( response.getStatus() == javax.ws.rs.core.Response.Status.OK.getStatusCode()
                    || response.getStatus() == javax.ws.rs.core.Response.Status.ACCEPTED.getStatusCode() )
            {
                hostRegistry.updateResourceHostEntryTimestamp( resourceHostInfo.getId() );
            }
        }
        finally
        {
            RestUtil.close( response, webClient );
        }
    }


    WebClient getWebClient( ResourceHostInfo resourceHostInfo )
    {
        return RestUtil.createWebClient(
                String.format( "http://%s:%d/trigger", hostRegistry.getResourceHostIp( resourceHostInfo ),
                        Common.DEFAULT_AGENT_PORT ), 3000, 5000, 1 );
    }


    ResourceHostInfo getResourceHostInfo( String requestHostId ) throws HostDisconnectedException
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


    Session getActiveSession()
    {
        return identityManager.getActiveSession();
    }


    CommandResult getResult( UUID commandId ) throws CommandException
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
                LOG.debug( "Response: {}", response );

                commandProcess.processResponse( response );
            }
            else
            {
                LOG.warn( String.format( "Callback not found for response: %s", jsonUtil.to( response ) ) );
            }

            //update rh timestamp
            ResourceHostInfo resourceHostInfo = getResourceHostInfo( response.getId() );

            hostRegistry.updateResourceHostEntryTimestamp( resourceHostInfo.getId() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing response", e );
        }
    }


    void remove( Request request )
    {
        Preconditions.checkNotNull( request );

        commands.remove( request.getCommandId() );
    }
}
