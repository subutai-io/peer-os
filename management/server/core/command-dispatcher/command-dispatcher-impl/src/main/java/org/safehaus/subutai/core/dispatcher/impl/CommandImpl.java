package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.AbstractCommand;
import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.CommandRunnerBase;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;


/**
 * Command which can contain both local and remote requests
 */
public class CommandImpl extends AbstractCommand {

    private final Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();


    /**
     * Constructor which initializes request based on supplied request builder and set of agents. The same request
     * produced by request builder will be sent to supplied set of agents
     *
     * @param description - command description
     * @param requestBuilder - request builder used to produce request
     * @param agents - target agents
     */
    public CommandImpl( String description, RequestBuilder requestBuilder, Set<Agent> agents, PeerManager peerManager,
                        CommandRunnerBase commandRunner ) {
        super( commandRunner );
        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agents ), "Agents are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = agents.size();
        this.timeout = requestBuilder.getTimeout();
        UUID environmentId = null;

        for ( Agent agent : agents )
        {
            //check that all agents belong to the same environment
            if ( environmentId == null )
            {
                environmentId = agent.getEnvironmentId();
            }
            else
            {
                Preconditions.checkState( environmentId.compareTo( agent.getEnvironmentId() ) == 0 );
            }
            Request request = requestBuilder.build( agent.getUuid(), commandUUID );
            //this is a local agent
            //TODO remove below line
            if ( false )
            {
                //            if ( peerManager.getSiteId().compareTo( agent.getSiteId() ) == 0 ) {
                requests.add( request );
            }
            else
            {
                Set<BatchRequest> batchRequests = remoteRequests.get( agent.getSiteId() );
                if ( batchRequests == null )
                {
                    batchRequests = new HashSet<>();
                    remoteRequests.put( agent.getSiteId(), batchRequests );
                    batchRequests.add( new BatchRequest( request, agent.getUuid(), agent.getEnvironmentId() ) );
                }
                else
                {
                    batchRequests.iterator().next().addAgentId( agent.getUuid() );
                }
            }
        }
    }


    /**
     * Creates command using supplied set of {@code BatchRequest} objects. Used when remote command is received for
     * execution.
     *
     * @param batchRequests - requests to execute
     */
    protected CommandImpl( Set<BatchRequest> batchRequests, CommandRunnerBase commandRunner ) {
        super( commandRunner );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( batchRequests ),
                "Batch Requests are null or empty" );

        Set<Request> requests = new HashSet<>();
        for ( BatchRequest batchRequest : batchRequests )
        {
            requests.addAll( batchRequest.getRequests() );
        }
        int timeout = 0;
        for ( Request request : requests )
        {
            if ( request.getTimeout() > timeout )
            {
                timeout = request.getTimeout();
            }
        }
        this.requestsCount = requests.size();
        this.timeout = timeout;
        //take any taskUUID since all requests in the batch must belong to the same command
        this.commandUUID = requests.iterator().next().getTaskUuid();
        this.requests.addAll( requests );
    }


    /**
     * Creates command using supplied set of {@code Request} objects. Used when command involves also local agents
     *
     * @param requests - requests to execute
     */
    protected CommandImpl( Collection<Request> requests, CommandRunnerBase commandRunner ) {
        super( commandRunner );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requests ), "Requests are null or empty" );
        this.requestsCount = requests.size();
        int timeout = 0;
        for ( Request request : requests )
        {
            if ( request.getTimeout() > timeout )
            {
                timeout = request.getTimeout();
            }
        }
        this.timeout = timeout;
        //take any taskUUID since all requests in the batch must belong to the same command
        this.commandUUID = requests.iterator().next().getTaskUuid();
        this.requests.addAll( requests );
    }


    /**
     * Constructor which initializes request based on supplied request builders. Each agent will receive own custom
     * request produced by corresponding AgentRequestBuilder
     *
     * @param description - command description
     * @param requestBuilders - request builder used to produce request
     */
    public CommandImpl( String description, Set<AgentRequestBuilder> requestBuilders, PeerManager peerManager,
                        CommandRunnerBase commandRunner ) {
        super( commandRunner );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requestBuilders ),
                "Request Builders are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = requestBuilders.size();

        int maxTimeout = 0;
        UUID environmentId = null;
        for ( AgentRequestBuilder requestBuilder : requestBuilders )
        {
            Agent agent = requestBuilder.getAgent();
            //check that all agents belong to the same environment
            if ( environmentId == null )
            {
                environmentId = agent.getEnvironmentId();
            }
            else
            {
                Preconditions.checkState( environmentId == agent.getEnvironmentId() );
            }
            Request request = requestBuilder.build( commandUUID );
            if ( requestBuilder.getTimeout() > maxTimeout )
            {
                maxTimeout = requestBuilder.getTimeout();
            }
            //this is a local agent
            if ( peerManager.getSiteId().compareTo( agent.getSiteId() ) == 0 )
            {
                requests.add( request );
            }
            else
            {
                Set<BatchRequest> batchRequests = remoteRequests.get( agent.getSiteId() );
                if ( batchRequests == null )
                {
                    batchRequests = new HashSet<>();
                    remoteRequests.put( agent.getSiteId(), batchRequests );
                }
                batchRequests.add( new BatchRequest( request, agent.getUuid(), agent.getEnvironmentId() ) );
            }
        }

        this.timeout = maxTimeout;
    }


    /**
     * Returns all remote requests of this command
     *
     * @return - remote requests of command
     */
    public Map<UUID, Set<BatchRequest>> getRemoteRequests() {
        return Collections.unmodifiableMap( remoteRequests );
    }
}
