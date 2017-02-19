package io.subutai.core.executor.api;


import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;


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
    CommandResult execute( String hostId, RequestBuilder requestBuilder ) throws CommandException;

    /**
     * Executes command on host synchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     * @param callback - callback to trigger on each response from host
     *
     * @return - result of command execution
     */
    CommandResult execute( String hostId, RequestBuilder requestBuilder, CommandCallback callback )
            throws CommandException;

    /**
     * Executes command on host asynchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     */
    void executeAsync( String hostId, RequestBuilder requestBuilder ) throws CommandException;

    /**
     * Executes command on host asynchronously
     *
     * @param hostId - target host id
     * @param requestBuilder - command to execute
     * @param callback - callback to trigger on each response from host
     */
    void executeAsync( String hostId, RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;
}
