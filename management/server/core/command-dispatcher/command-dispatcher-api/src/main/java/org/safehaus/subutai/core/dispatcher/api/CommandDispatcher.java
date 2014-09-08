package org.safehaus.subutai.core.dispatcher.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.CommandRunner;


/**
 * This class allows to send commands to local and remote agents.
 */
public interface CommandDispatcher extends CommandRunner {

    public void executeRequests( final UUID ownerId, final Set<BatchRequest> requests );

    public void processResponses( final Set<Response> responses );
}
