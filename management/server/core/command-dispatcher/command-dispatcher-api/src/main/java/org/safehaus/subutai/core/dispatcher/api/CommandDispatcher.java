package org.safehaus.subutai.core.dispatcher.api;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;


/**
 * This class allows to send commands to local and remote agents.
 */
public interface CommandDispatcher extends CommandRunnerBase {

    public void executeRequests( final String IP, final Set<BatchRequest> requests );

    public void processResponses( final Set<Response> responses );
}
