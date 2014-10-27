package org.safehaus.subutai.core.message.cli;


import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.safehaus.subutai.core.message.api.Queue;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * TestCommand
 */
@Command(scope = "message-queue", name = "test", description = "test command")
public class TestCommand extends OsgiCommandSupport
{

    private final Queue queue;
    private MyMessageListener listener;


    public TestCommand( final Queue queue )
    {
        Preconditions.checkNotNull( queue, "Queue is null" );

        this.queue = queue;

        listener = new MyMessageListener( "Test" );
        queue.addMessageListener( listener );
    }



    @Override
    protected Object doExecute() throws Exception
    {

        Message message = queue.createMessage( new CustomObject( 123, "hello world" ) );

        queue.sendMessage( null, message, "Test", 0 );

        return null;
    }


    static class MyMessageListener extends MessageListener
    {

        protected MyMessageListener( final String recipientId )
        {
            super( recipientId );
        }


        @Override
        public void onMessage( final Message message )
        {
            try
            {
                System.out.println( message.getPayload( CustomObject.class ) );
            }
            catch ( MessageException e )
            {
                e.printStackTrace();
            }
        }
    }
}
