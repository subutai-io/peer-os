package org.safehaus.subutai.core.command.api;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;


/**
 * This class is used to hold commands specific to client.
 *
 * @deprecated Do not use this class. Use factories and run commands using Command.execute
 */
@Deprecated
public class CommandsSingleton
{
    private static CommandsSingleton INSTANCE = new CommandsSingleton();
    private CommandRunner commandRunner;


    protected CommandsSingleton()
    {
    }


    public static void init( CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        INSTANCE.commandRunner = commandRunner;
    }


    protected static Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents )
    {

        return getCommandRunner().createCommand( requestBuilder, agents );
    }


    private static CommandRunner getCommandRunner()
    {
        Preconditions
                .checkNotNull( INSTANCE.commandRunner, "Command Runner is null or not set. Call init method first" );

        return INSTANCE.commandRunner;
    }


    protected static Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return getCommandRunner().createCommand( agentRequestBuilders );
    }


    protected static Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents )
    {

        return getCommandRunner().createCommand( description, requestBuilder, agents );
    }


    protected static Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return getCommandRunner().createCommand( description, agentRequestBuilders );
    }
}
