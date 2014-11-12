package org.safehaus.subutai.core.command.api.command;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Base interface for Command Runner implementations
 */
public interface CommandRunnerBase
{

    /**
     * Runs command on agents. Runs asynchronously for calling party. The supplied callback is triggered every time a
     * response is received from agent. Calling party may examine the command to see its status and results of each
     * agent.
     *
     * @param command - command to run
     * @param commandCallback - callback to trigger on every response
     */
    public void runCommandAsync( Command command, CommandCallback commandCallback );

    /**
     * Runs command on agents. Runs asynchronously for calling party. Calling party may examine the command to see its
     * status and results of each agent.
     *
     * @param command - command to run
     */
    public void runCommandAsync( Command command );

    /**
     * Runs command on agents. Runs synchronously for calling party. Calling party may examine the command to see its
     * status and results of each agent after this call returns.
     *
     * @param command - command to run
     */
    public void runCommand( Command command );

    /**
     * Runs command on agents. Runs synchronously for calling party. The supplied callback is triggered every time a
     * response is received from agent. Calling party may examine the command to see its status and results of each
     * agent after this call returns.
     *
     * @param command - command to run
     * @param commandCallback - - callback to trigger on every response
     */
    public void runCommand( Command command, CommandCallback commandCallback );

    /**
     * Creates command based on supplied RequestBuilder and target agents on which to run the command.
     *
     * @param requestBuilder - request builder
     * @param agents - target agents
     *
     * @return - command
     */
    public Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents );


    /**
     * Creates command based on supplied RequestBuilder and target agents on which to run the command.
     *
     * @param description - description of command
     * @param requestBuilder - request builder
     * @param agents - target agents
     *
     * @return - command
     */
    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents );

    /**
     * Creates command based on supplied set of AgentRequestBuilders.
     *
     * @param agentRequestBuilders - agent request builders
     *
     * @return - command
     */
    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders );

    /**
     * Creates command based on supplied set of AgentRequestBuilders.
     *
     * @param description - description of command
     * @param agentRequestBuilders - agent request builders
     *
     * @return - command
     */
    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders );
}
