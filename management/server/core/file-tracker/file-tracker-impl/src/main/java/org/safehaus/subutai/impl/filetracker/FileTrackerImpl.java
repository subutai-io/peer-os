package org.safehaus.subutai.impl.filetracker;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.api.filetracker.FileTracker;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import java.util.HashSet;


public class FileTrackerImpl implements FileTracker, ResponseListener
{

    private final HashSet<ResponseListener> listeners = new HashSet<>();

    private CommandRunner commandRunner;

    private CommunicationManager communicationManager;


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setCommunicationManager( CommunicationManager communicationManager )
    {
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
    public void createConfigPoints( Agent agent, String configPoints[] )
    {

        Command command = commandRunner.createCommand(
            new RequestBuilder( "pwd" )
                .withType( RequestType.INOTIFY_CREATE_REQUEST )
                .withConfPoints( configPoints ),
            Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public void removeConfigPoints( Agent agent, String configPoints[] )
    {

        Command command = commandRunner.createCommand(
            new RequestBuilder( "pwd" )
                .withType( RequestType.INOTIFY_REMOVE_REQUEST )
                .withConfPoints( configPoints ),
            Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public String[] listConfigPoints( final Agent agent )
    {

        Command command = commandRunner.createCommand(
            new RequestBuilder( "pwd" )
                .withType( RequestType.INOTIFY_LIST_REQUEST ),
            Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );

        return null;
    }


    @Override
    public void onResponse( Response response )
    {
        if ( response == null || response.getType() != ResponseType.INOTIFY_ACTION_RESPONSE )
        {
            return;
        }

        for ( ResponseListener listener : listeners )
        {
            listener.onResponse( response );
        }
    }
}
