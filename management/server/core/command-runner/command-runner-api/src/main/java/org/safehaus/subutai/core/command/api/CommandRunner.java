/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api;


import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Command Runner i used to send requests to agents and obtain responses from them using either Command.getResults() or
 * by parsing AgetnResult inside command callbacks.
 */
public interface CommandRunner extends CommandRunnerBase
{


    /**
     * Creates broadcast command. Command is sent to all connected agents
     *
     * @param requestBuilder - request builder
     *
     * @return - command
     */
    public Command createBroadcastCommand( RequestBuilder requestBuilder );
}
