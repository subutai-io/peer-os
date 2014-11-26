package org.safehaus.subutai.core.executor.api;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Allows to execute commands on hosts
 */
public interface CommandExecutor
{
    /**
     * Executes command on host synchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     *
     * @return - result of command execution
     */
    public CommandResult execute( UUID hostId, RequestBuilder requestBuilder ) throws CommandException;

    /**
     * Executes command on host synchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     * @param callback - callback to trigger on each response from host
     *
     * @return - result of command execution
     */
    public CommandResult execute( UUID hostId, RequestBuilder requestBuilder, CommandCallback callback )
            throws CommandException;

    /**
     * Executes command on host asynchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     */
    public void executeAsync( UUID hostId, RequestBuilder requestBuilder ) throws CommandException;

    /**
     * Executes command on host asynchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     * @param callback - callback to trigger on each response from host
     */
    public void executeAsync( UUID hostId, RequestBuilder requestBuilder, CommandCallback callback )
            throws CommandException;
}
