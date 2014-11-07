package org.safehaus.subutai.core.messenger.cli;


import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * TestCommand
 */
@Command(scope = "messenger", name = "test", description = "test command")
public class TestCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    private final Messenger messenger;
    private final PeerManager peerManager;


    public TestCommand( final Messenger messenger, final PeerManager peerManager )
    {
        this.messenger = messenger;
        this.peerManager = peerManager;
        final MyMessageListener listener = new MyMessageListener( "Test" );
        messenger.addMessageListener( listener );
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Message message = messenger.createMessage( new CustomObject( 123, "hello world" ) );

        messenger.sendMessage( peerManager.getLocalPeer(), message, "Test", 30 );

        return null;
    }


    static class MyMessageListener extends MessageListener
    {

        protected MyMessageListener( final String recipient )
        {
            super( recipient );
        }


        @Override
        public void onMessage( Message message )
        {

            LOG.info( message.getPayload( CustomObject.class ).toString() );
            LOG.info( message.toString() );
        }
    }
}
