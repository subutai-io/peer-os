package org.safehaus.subutai.core.command.api;


import java.util.Set;

import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.base.Preconditions;


/**
 * This class is used to hold commands specific to client. One needs to call {@code init} method prior to using it.
 * Clients should extend this class and add custom commands creation methods e.g. <p/>
 * <blockquote><pre>
 * public class Commands extends CommandsSingleton{
 *   public static Command getStartCommand(Set<Agent> agents) {
 *     return createCommand(
 *       new RequestBuilder("service solr start").withTimeout(60),
 *       agents);
 *   }
 * }
 * </pre></blockquote>
 * <p/> Then somewhere in client code prior to using this class: <p/> {@code Commands.init(commandRunnner);}
 */
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
