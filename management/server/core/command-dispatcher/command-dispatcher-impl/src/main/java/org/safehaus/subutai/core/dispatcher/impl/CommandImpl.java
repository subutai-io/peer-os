package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.AbstractCommand;
import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.UUIDUtil;

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
    public CommandImpl( String description, RequestBuilder requestBuilder, Set<Agent> agents ) {

        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agents ), "Agents are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = agents.size();
        this.timeout = requestBuilder.getTimeout();

        for ( Agent agent : agents ) {
            Request request = requestBuilder.build( agent.getUuid(), commandUUID );
            if ( agent.isLocal() ) {
                requests.add( request );
            }
            else {
                Set<BatchRequest> batchRequests = remoteRequests.get( agent.getOwnerId() );
                if ( batchRequests == null ) {
                    batchRequests = new HashSet<>();
                    remoteRequests.put( agent.getOwnerId(), batchRequests );
                    batchRequests.add( new BatchRequest( request, agent.getUuid() ) );
                }
                else {
                    batchRequests.iterator().next().addTargetUUID( agent.getUuid() );
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
    public CommandImpl( Set<BatchRequest> batchRequests ) {

        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( batchRequests ),
                "Batch Requests are null or empty" );

        this.description = "REMOTE";
        Set<Request> requests = new HashSet<>();
        for ( BatchRequest batchRequest : batchRequests ) {
            requests.addAll( batchRequest.getRequests() );
        }
        int timeout = 0;
        for ( Request request : requests ) {
            if ( request.getTimeout() > timeout ) {
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
     * Constructor which initializes request based on supplied request builders. Each agent will receive own custom
     * request produced by corresponding AgentRequestBuilder
     *
     * @param description - command description
     * @param requestBuilders - request builder used to produce request
     */
    public CommandImpl( String description, Set<AgentRequestBuilder> requestBuilders ) {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requestBuilders ),
                "Request Builders are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = requestBuilders.size();

        int maxTimeout = 0;
        for ( AgentRequestBuilder requestBuilder : requestBuilders ) {
            Agent agent = requestBuilder.getAgent();
            Request request = requestBuilder.build( commandUUID );
            if ( requestBuilder.getTimeout() > maxTimeout ) {
                maxTimeout = requestBuilder.getTimeout();
            }
            if ( agent.isLocal() ) {
                requests.add( request );
            }
            else {
                Set<BatchRequest> batchRequests = remoteRequests.get( agent.getOwnerId() );
                if ( batchRequests == null ) {
                    batchRequests = new HashSet<>();
                    remoteRequests.put( agent.getOwnerId(), batchRequests );
                }
                batchRequests.add( new BatchRequest( request, agent.getUuid() ) );
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
