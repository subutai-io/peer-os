package org.safehaus.subutai.core.message.cli;


import java.io.Serializable;

import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.Queue;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * TestCommand
 */
@Command(scope = "message-queue", name = "test", description = "test command")
public class TestCommand extends OsgiCommandSupport
{

    private final Queue queue;


    public TestCommand( final Queue queue )
    {
        Preconditions.checkNotNull( queue, "Queue is null" );

        this.queue = queue;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Message message = queue.createMessage( new CustomObject( 123, "hello world" ) );

        queue.sendMessage( null, message, null, 0 );

        return null;
    }


    static class CustomObject implements Serializable
    {
        private int num;
        private String str;


        CustomObject( final int num, final String str )
        {
            this.num = num;
            this.str = str;
        }


        @Override
        public String toString()
        {
            return Objects.toStringHelper( this ).add( "num", num ).add( "str", str ).toString();
        }
    }
}
