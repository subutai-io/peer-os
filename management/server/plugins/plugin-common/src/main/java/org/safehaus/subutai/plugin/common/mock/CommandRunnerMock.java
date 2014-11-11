package org.safehaus.subutai.plugin.common.mock;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.common.command.RequestBuilder;


public class CommandRunnerMock implements CommandRunner
{

    @Override
    public void runCommandAsync( Command command, CommandCallback commandCallback )
    {

    }


    @Override
    public void runCommandAsync( Command command )
    {

    }


    @Override
    public void runCommand( Command command )
    {

    }


    @Override
    public void runCommand( Command command, CommandCallback commandCallback )
    {

    }


    @Override
    public Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents )
    {
        Request request = requestBuilder.build( UUIDUtil.generateTimeBasedUUID(), UUIDUtil.generateTimeBasedUUID() );
        return new CommandMock().setDescription( request.getProgram() );
    }


    @Override
    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents )
    {
        return null;
    }


    @Override
    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return null;
    }


    @Override
    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return null;
    }


    @Override
    public Command createBroadcastCommand( final RequestBuilder requestBuilder )
    {
        return null;
    }
}
