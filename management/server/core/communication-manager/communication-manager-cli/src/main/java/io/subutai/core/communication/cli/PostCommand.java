package io.subutai.core.communication.cli;


import java.net.URI;
import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.communication.api.CommunicationManager;


@Command( scope = "cm", name = "post", description = "Post data to recipient" )
public class PostCommand extends OsgiCommandSupport
{
    final static String REST_URI = "https://172.16.193.109:444/ws/peer";
    private CommunicationManager communicationManager;

    @Argument( index = 0, name = "recipient keyID", required = true, multiValued = false, description = "recipient "
            + "keyID, usually GPG fingerprint" )
    private String recipientKeyId;


    public PostCommand( final CommunicationManager communicationManager )
    {
        this.communicationManager = communicationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Post data to : " + recipientKeyId );
        URI uri = new URI( REST_URI );
        String result = communicationManager.post( uri, recipientKeyId, "This is an original data." );
        System.out.println( result );
        return null;
    }
}
