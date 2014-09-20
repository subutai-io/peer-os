package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Request;

import com.google.common.base.Preconditions;


/**
 * Batch request
 */
class BatchRequest
{

    private final Request request;
    private final Set<UUID> agentIds = new HashSet<>();
    //assume that environment id is the same for all requests in the batch
    private final UUID environmentId;


    public BatchRequest( final Request request, UUID agentId, UUID environmentId )
    {
        Preconditions.checkNotNull( request, "Request is null" );
        Preconditions.checkNotNull( agentId, "Agent id is null" );
        Preconditions.checkNotNull( environmentId, "Environment id is null" );

        this.environmentId = environmentId;
        this.request = request;
        agentIds.add( agentId );
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void addAgentId( UUID agentId )
    {
        Preconditions.checkNotNull( agentId, "Agent id is null" );

        agentIds.add( agentId );
    }


    public Set<UUID> getAgentIds()
    {
        return Collections.unmodifiableSet( agentIds );
    }


    public int getRequestsCount()
    {
        return agentIds.size();
    }


    public UUID getCommandId()
    {
        return request.getTaskUuid();
    }


    public Set<Request> getRequests()
    {
        Set<Request> requests = new HashSet<>();
        for ( UUID agentId : agentIds )
        {
            Request req = new Request( request.getSource(), request.getType(), agentId, request.getTaskUuid(),
                    request.getRequestSequenceNumber(), request.getWorkingDirectory(), request.getProgram(),
                    request.getStdOut(), request.getStdErr(), request.getStdOutPath(), request.getStdErrPath(),
                    request.getRunAs(), request.getArgs(), request.getEnvironment(), request.getPid(),
                    request.getTimeout() );
            requests.add( req );
        }
        return requests;
    }
}
