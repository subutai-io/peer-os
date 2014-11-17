package org.safehaus.subutai.core.filetracker.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.safehaus.subutai.core.filetracker.api.ConfigPointListener;
import org.safehaus.subutai.core.filetracker.api.FileTracker;
import org.safehaus.subutai.core.filetracker.api.FileTrackerException;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.Host;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


//TODO use proper RequestType and update RequestBuilder.RequestImpl after migration to new agent
public class FileTrackerImpl implements FileTracker, ByteMessageListener
{

    private final Set<ConfigPointListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<ConfigPointListener, Boolean>() );

    private Broker broker;
    protected CommandUtil commandUtil = new CommandUtil();


    public FileTrackerImpl( final Broker broker )
    {
        Preconditions.checkNotNull( broker );

        this.broker = broker;
    }


    public void init() throws FileTrackerException
    {
        try
        {
            broker.addByteMessageListener( this );
        }
        catch ( BrokerException e )
        {
            throw new FileTrackerException( e );
        }
    }


    public void destroy()
    {
        broker.removeMessageListener( this );
    }


    @Override
    public void addListener( ConfigPointListener listener )
    {
        Preconditions.checkNotNull( listener );

        listeners.add( listener );
    }


    @Override
    public void removeListener( ConfigPointListener listener )
    {
        Preconditions.checkNotNull( listener );

        listeners.remove( listener );
    }


    @Override
    public void createConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, "Host is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( configPoints ), "Invalid config points" );

        try
        {
            commandUtil.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_CREATE_REQUEST )
                                                            .withConfPoints( configPoints ), host );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( e );
        }
    }


    @Override
    public void removeConfigPoints( Host host, Set<String> configPoints ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, "Host is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( configPoints ), "Invalid config points" );

        try
        {
            commandUtil.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_REMOVE_REQUEST )
                                                            .withConfPoints( configPoints ), host );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( e );
        }
    }


    @Override
    public Set<String> listConfigPoints( final Host host ) throws FileTrackerException
    {
        Preconditions.checkNotNull( host, "Host is null" );

        final Set<String> configPoints = Sets.newHashSet();

        try
        {
            host.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_LIST_REQUEST ),
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
        //TODO process inotify event here
    }


    @Override
    public Topic getTopic()
    {
        return Topic.INOTIFY_TOPIC;
    }
}
