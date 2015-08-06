package io.subutai.core.communication.cli;


import java.net.URI;

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

    @Argument( index = 1, name = "text to send", required = true, multiValued = false, description = "Text to post" )
    private String data;

    @Argument( index = 2, name = "recipient URI", required = false, multiValued = false, description = "recipient "
            + "URL. Default is https://172.16.193.109:444/ws/peer" )
    private String uri = null;


    public PostCommand( final CommunicationManager communicationManager )
    {
        this.communicationManager = communicationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Target keyId (fingerprint) : " + recipientKeyId );

        URI target = new URI( uri == null ? REST_URI : uri );

        System.out.println( "Target URI : " + target.toASCIIString() );

        String result = communicationManager.post( target, recipientKeyId, data );
        System.out.println( String.format( "Returned result: [%s]", result ) );
        return null;
    }
}
