package org.safehaus.subutai.core.executor.cli;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.executor.api.CommandExecutor;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "command", name = "exec-async", description = "Executes command asynchronously" )
public class ExecAsyncCommand extends OsgiCommandSupport
{
    private final CommandExecutor executor;

    @Argument( index = 0, name = "host id", required = true, multiValued = false, description = "id of host on which "
            + "to run the command" )
    String hostId;
    @Argument( index = 1, name = "command", required = true, multiValued = false, description = "command to execute" )
    String command;
    @Argument( index = 2, name = "timeout", required = false, multiValued = false, description = "command timeout" )
    int timeout = 30;
    @Argument( index = 3, name = "daemon", required = false, multiValued = false, description = "is daemon" )
    boolean daemon = false;


    public ExecAsyncCommand( final CommandExecutor executor )
    {
        Preconditions.checkNotNull( executor );

        this.executor = executor;
    }


    @Override
    protected Object doExecute() throws CommandException
    {

        if ( UUIDUtil.isStringAUuid( hostId ) )
        {
            UUID id = UUIDUtil.generateUUIDFromString( hostId );

            RequestBuilder requestBuilder = new RequestBuilder( command ).withTimeout( timeout );
            executor.executeAsync( id, daemon ? requestBuilder.daemon() : requestBuilder, new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    System.out.println( response );
                }
            } );
        }
        else
        {
            System.out.println( "Invalid host id" );
        }

        return null;
    }
}
