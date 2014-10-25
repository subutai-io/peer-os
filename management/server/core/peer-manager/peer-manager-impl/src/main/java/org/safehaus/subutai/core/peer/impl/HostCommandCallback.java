package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;


/**
 * CommandCallback wrapper for Host.execute method
 */
public class HostCommandCallback extends CommandCallback
{
    private final org.safehaus.subutai.common.protocol.CommandCallback commandCallback;


    public HostCommandCallback( final org.safehaus.subutai.common.protocol.CommandCallback commandCallback )
    {
        this.commandCallback = commandCallback;
    }


    @Override
    public void onResponse( final Response response, final AgentResult agentResult, final Command command )
    {
        commandCallback.onResponse( response,
                new CommandResult( agentResult.getExitCode(), agentResult.getStdOut(), agentResult.getStdErr(),
                        command.getCommandStatus() ) );
    }
}
