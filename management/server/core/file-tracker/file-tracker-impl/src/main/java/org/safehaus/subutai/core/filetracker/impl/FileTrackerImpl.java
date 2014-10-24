package org.safehaus.subutai.core.filetracker.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.filetracker.api.FileTracker;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class FileTrackerImpl implements FileTracker, ResponseListener
{

    private final Set<ResponseListener> listeners = new HashSet<>();

    private CommandRunner commandRunner;

    private CommunicationManager communicationManager;


    public void setCommandRunner( CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "CommandRunner is null." );
        this.commandRunner = commandRunner;
    }


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
    public void createConfigPoints( Agent agent, String[] configPoints )
    {

        Command command = commandRunner.createCommand(
                new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_CREATE_REQUEST )
                                           .withConfPoints( configPoints ), Sets.newHashSet( agent ) );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public void removeConfigPoints( Agent agent, String[] configPoints )
    {

        Command command = commandRunner.createCommand(
                new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_REMOVE_REQUEST )
                                           .withConfPoints( configPoints ), Sets.newHashSet( agent ) );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public String[] listConfigPoints( final Agent agent )
    {

        Command command = commandRunner
                .createCommand( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_LIST_REQUEST ),
                        Sets.newHashSet( agent ) );

        commandRunner.runCommandAsync( command );

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
