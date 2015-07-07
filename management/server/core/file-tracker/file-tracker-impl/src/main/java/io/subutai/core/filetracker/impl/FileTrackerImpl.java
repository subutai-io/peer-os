package io.subutai.core.filetracker.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.Topic;
import io.subutai.core.filetracker.api.ConfigPointListener;
import io.subutai.core.filetracker.api.FileTracker;
import io.subutai.core.filetracker.api.FileTrackerException;
import io.subutai.core.filetracker.api.InotifyEventType;
import io.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class FileTrackerImpl implements FileTracker, ByteMessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( FileTrackerImpl.class.getName() );
    private static final String HOST_IS_NULL_MSG = "Host is null";

    protected Set<ConfigPointListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<ConfigPointListener, Boolean>() );
    private final PeerManager peerManager;

    protected JsonUtil jsonUtil = new JsonUtil();
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExecutorService notifier = Executors.newCachedThreadPool();


    public FileTrackerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    public void destroy()
    {
        notifier.shutdown();
    }


    public void addListener( ConfigPointListener listener )
    {
        Preconditions.checkNotNull( listener );

        listeners.add( listener );
    }


    public void removeListener( ConfigPointListener listener )
    {
        Preconditions.checkNotNull( listener );

        listeners.remove( listener );
    }


    @Override
    public void createConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, HOST_IS_NULL_MSG );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( configPoints ), "Invalid config points" );

        try
        {
            commandUtil.execute( new RequestBuilder( "pwd" ).withType( RequestType.SET_INOTIFY_REQUEST )
                                                            .withConfigPoints( configPoints ), host );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( e );
        }
    }


    @Override
    public void removeConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, HOST_IS_NULL_MSG );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( configPoints ), "Invalid config points" );

        try
        {
            commandUtil.execute( new RequestBuilder( "pwd" ).withType( RequestType.UNSET_INOTIFY_REQUEST )
                                                            .withConfigPoints( configPoints ), host );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( e );
        }
    }


    @Override
    public Set<String> listConfigPoints( final Host host ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, HOST_IS_NULL_MSG );

        final Set<String> configPoints = Sets.newHashSet();

        try
        {
            host.execute( new RequestBuilder( "pwd" ).withType( RequestType.LIST_INOTIFY_REQUEST ),
                    new CommandCallback()
                    {
                        @Override
                        public void onResponse( final Response response, final CommandResult commandResult )
                        {
                            if ( !CollectionUtil.isCollectionEmpty( response.getConfigPoints() ) )
                            {
                                configPoints.addAll( response.getConfigPoints() );
                            }
                        }
                    } );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( e );
        }

        return configPoints;
    }


    @Override
    public void onMessage( final byte[] message )
    {
        try
        {
            String response = new String( message, "UTF-8" );
            InotifyEvent inotifyEvent = jsonUtil.from( response, InotifyEvent.class );
            if ( inotifyEvent.getResponse() != null )
            {
                Host host = peerManager.getLocalPeer().bindHost( inotifyEvent.getResponse().getId() );
                notifyListeners( host, inotifyEvent.getResponse().getEventType(),
                        inotifyEvent.getResponse().getConfigPoint() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in onMessage", e );
        }
    }


    protected void notifyListeners( final Host host, final InotifyEventType eventType, final String configPoint )
    {
        for ( final ConfigPointListener listener : listeners )
        {
            notifier.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onConfigPointChangeEvent( host, eventType, configPoint );
                }
            } );
        }
    }


    @Override
    public Topic getTopic()
    {
        return Topic.INOTIFY_TOPIC;
    }
}