package io.subutai.core.executor.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Karaf CLI support command Execute command synchronously hostId: target hostId to execute desired command command:
 * command to execute timeout: set command execution timeout daemon: trigger command execution process as daemon or not
 */
@Command( scope = "command", name = "exec-sync", description = "Executes command synchronously" )
public class ExecSyncCommand extends SubutaiShellCommandSupport
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


    public ExecSyncCommand( final CommandExecutor executor )
    {
        Preconditions.checkNotNull( executor );

        this.executor = executor;
    }


    @Override
    protected Object doExecute() throws CommandException
    {

        RequestBuilder requestBuilder = new RequestBuilder( command ).withTimeout( timeout );
        CommandResult result = executor.execute( hostId, daemon ? requestBuilder.daemon() : requestBuilder );

        System.out.println( result );


        return null;
    }
}
