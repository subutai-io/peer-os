package org.safehaus.subutai.core.template.impl;


import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.template.api.ActionType;


class ScriptExecutor
{

    private final CommandRunner commandRunner;
    int defaultTimeout = 360;


    public ScriptExecutor( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setDefaultTimeout( int defaultTimeout )
    {
        this.defaultTimeout = defaultTimeout;
    }


    public boolean execute( Agent agent, ActionType actionType, String... args )
    {
        return execute( agent, actionType, defaultTimeout, TimeUnit.SECONDS, args );
    }


    public boolean execute( Agent agent, ActionType actionType, long timeout, TimeUnit unit, String... args )
    {
        if ( agent == null || actionType == null )
        {
            return false;
        }

        RequestBuilder rb = new RequestBuilder( actionType.buildCommand( args ) );
        rb.withTimeout( ( int ) unit.toSeconds( timeout ) );
        Command cmd = commandRunner.createCommand( rb, new HashSet<>( Arrays.asList( agent ) ) );

        commandRunner.runCommand( cmd );
        return cmd.hasSucceeded();
    }
}
