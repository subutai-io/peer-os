package org.safehaus.subutai.core.message.cli;


import java.util.UUID;

import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.safehaus.subutai.core.message.api.Queue;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * TestCommand
 */
@Command(scope = "message-queue", name = "test", description = "test command")
public class TestCommand extends OsgiCommandSupport
{

    private final Queue queue;
    private final PeerManager peerManager;


    public TestCommand( final Queue queue, final PeerManager peerManager )
    {
        this.queue = queue;
        this.peerManager = peerManager;

        final MyMessageListener listener = new MyMessageListener( "Test" );
        queue.addMessageListener( listener );
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Message message = queue.createMessage( new CustomObject( 123, "hello world" ) );

        queue.sendMessage( peerManager.getLocalPeer(), message, "Test", 30 );

        return null;
    }


    static class MyMessageListener extends MessageListener
    {

        protected MyMessageListener( final String recipient )
        {
            super( recipient );
        }


        @Override
        public void onMessage( UUID sourcePeerId, Message message )
        {

            System.out.println( sourcePeerId + " : " + message.getPayload( CustomObject.class ) );
        }
    }
}
