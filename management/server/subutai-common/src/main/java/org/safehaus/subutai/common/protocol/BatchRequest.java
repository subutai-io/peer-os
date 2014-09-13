package org.safehaus.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;


/**
 * Batch request
 */
public class BatchRequest {

    private final Request request;
    private final Set<UUID> targetUUIDs = new HashSet<>();


    public BatchRequest( final Request request, UUID targetUUID ) {
        Preconditions.checkNotNull( request, "Request is null" );
        Preconditions.checkNotNull( targetUUID, "Target UUID is null" );

        this.request = request;
        targetUUIDs.add( targetUUID );
    }


    public void addTargetUUID( UUID targetUUID ) {
        Preconditions.checkNotNull( targetUUID, "Target UUID is null" );

        targetUUIDs.add( targetUUID );
    }


    public int getRequestsCount() {
        return targetUUIDs.size();
    }


    public UUID getCommandId() {
        return request.getTaskUuid();
    }


    public Set<Request> getRequests() {
        Set<Request> requests = new HashSet<>();
        for ( UUID targetUUID : targetUUIDs ) {
            Request req = new Request( request.getSource(), request.getType(), targetUUID, request.getTaskUuid(),
                    request.getRequestSequenceNumber(), request.getWorkingDirectory(), request.getProgram(),
                    request.getStdOut(), request.getStdErr(), request.getStdOutPath(), request.getStdErrPath(),
                    request.getRunAs(), request.getArgs(), request.getEnvironment(), request.getPid(),
                    request.getTimeout() );
            requests.add( req );
        }
        return requests;
    }
}
