package io.subutai.core.executor.cli;


import java.util.UUID;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Karaf CLI command support for command executor bundle
 * Executes command asynchronously with parameters passed:
 * hostId: id of target host to execute command
 * command: command to execute
 * timeout: optional parameter to kill a process executing the command
 * daemon: specify command execution as a daemon or not
 */
@Command( scope = "command", name = "exec-async", description = "Executes command asynchronously" )
public class ExecAsyncCommand extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( ExecAsyncCommand.class.getName() );

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
                    LOG.info( response.toString() );
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
