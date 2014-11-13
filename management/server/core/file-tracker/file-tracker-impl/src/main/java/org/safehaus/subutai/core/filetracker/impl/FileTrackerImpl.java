package org.safehaus.subutai.core.filetracker.impl;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.filetracker.api.FileTracker;
import org.safehaus.subutai.core.filetracker.api.FileTrackerException;
import org.safehaus.subutai.core.peer.api.Host;

import java.util.HashSet;
import java.util.Set;


public class FileTrackerImpl implements FileTracker, ResponseListener
{

    private final Set<ResponseListener> listeners = new HashSet<>();

    private CommunicationManager communicationManager;


    public void setCommunicationManager( CommunicationManager communicationManager )
    {
        Preconditions.checkNotNull( communicationManager, "CommunicationManager is null." );
        this.communicationManager = communicationManager;
    }


    public void init()
    {
        communicationManager.addListener( this );
    }


    public void destroy()
    {
        communicationManager.removeListener( this );
    }


    @Override
    public void addListener( ResponseListener listener )
    {
        listeners.add( listener );
    }


    @Override
    public void removeListener( ResponseListener listener )
    {
        listeners.remove( listener );
    }


    @Override
    public void createConfigPoints( Host host, String[] configPoints ) throws FileTrackerException
    {
        try
        {
            host.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_CREATE_REQUEST )
                                                     .withConfPoints( configPoints ) );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( "Could not create config points: " + e.toString() );
        }
    }


    @Override
    public void removeConfigPoints( Host host, String[] configPoints ) throws FileTrackerException
    {
        try
        {
            host.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_REMOVE_REQUEST )
                                                     .withConfPoints( configPoints ) );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( "Could not remove config points: " + e.toString() );
        }
    }


    @Override
    public String[] listConfigPoints( final Host host ) throws FileTrackerException
    {
        try
        {
            host.execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_LIST_REQUEST ) );
        }
        catch ( CommandException e )
        {
            throw new FileTrackerException( "Could not list config points: " + e.toString() );
        }
        return new String[] { };
    }


    @Override
    public void onResponse( Response response )
    {
        if ( response == null || response.getType() != ResponseType.INOTIFY_ACTION_RESPONSE )
        {
            //System.out.println( "Listener is null." );
            return;
        }

        for ( ResponseListener listener : listeners )
        {
            listener.onResponse( response );
        }
    }
}
