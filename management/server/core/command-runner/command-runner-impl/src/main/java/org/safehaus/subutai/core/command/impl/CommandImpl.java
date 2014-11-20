/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.command.api.command.AbstractCommand;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;

import com.google.common.base.Preconditions;


/**
 * This is implementation of Command interface
 */
public class CommandImpl extends AbstractCommand
{


    /**
     * Constructor which initializes request based on supplied request builder. The same request produced by request
     * builder will be sent to all connected agents. This is a broadcast command.
     *
     * @param requestBuilder - request builder used to produce request
     * @param requestsCount - number of request to send
     */
    public CommandImpl( RequestBuilder requestBuilder, int requestsCount, CommandRunnerBase commandRunner )
    {
        super( commandRunner );
        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( requestsCount > 0, "Request Count <= 0" );

        this.broadcastCommand = true;
        this.commandUUID = UUID.randomUUID();
        this.requestsCount = requestsCount;
        this.timeout = requestBuilder.getTimeout();

        requests.add( requestBuilder.build( commandUUID, commandUUID ) );
    }


    /**
     * Constructor which initializes request based on supplied request builder and set of agents. The same request
     * produced by request builder will be sent to supplied set of agents
     *
     * @param description - command description
     * @param requestBuilder - request builder used to produce request
     * @param agents - target agents
     */
    public CommandImpl( String description, RequestBuilder requestBuilder, Set<Agent> agents,
                        CommandRunnerBase commandRunner )
    {
        super( commandRunner );
        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agents ), "Agents are null or empty" );

        this.description = description;
        this.commandUUID = UUID.randomUUID();
        this.requestsCount = agents.size();
        this.timeout = requestBuilder.getTimeout();

        for ( Agent agent : agents )
        {
            requests.add( requestBuilder.build( agent.getUuid(), commandUUID ) );
        }
    }


    /**
     * Constructor which initializes request based on supplied request builders. Each agent will receive own custom
     * request produced by corresponding AgentRequestBuilder
     *
     * @param description - command description
     * @param requestBuilders - request builder used to produce request
     */
    public CommandImpl( String description, Set<AgentRequestBuilder> requestBuilders, CommandRunnerBase commandRunner )
    {
        super( commandRunner );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requestBuilders ),
                "Request Builders are null or empty" );

        this.description = description;
        this.commandUUID = UUID.randomUUID();
        this.requestsCount = requestBuilders.size();

        int maxTimeout = 0;
        for ( AgentRequestBuilder requestBuilder : requestBuilders )
        {
            requests.add( requestBuilder.build( commandUUID ) );
            if ( requestBuilder.getTimeout() > maxTimeout )
            {
                maxTimeout = requestBuilder.getTimeout();
            }
        }

        this.timeout = maxTimeout;
    }
}
