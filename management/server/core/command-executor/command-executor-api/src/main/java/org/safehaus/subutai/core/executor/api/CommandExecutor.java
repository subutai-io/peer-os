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
    public CommandResult execute( UUID hostId, RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( UUID hostId, RequestBuilder requestBuilder, CommandCallback callback )
            throws CommandException;

    public void executeAsync( UUID hostId, RequestBuilder requestBuilder ) throws CommandException;

    public void executeAsync( UUID hostId, RequestBuilder requestBuilder, CommandCallback callback )
            throws CommandException;
}
