package org.safehaus.subutai.core.dispatcher.api;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


/**
 * This class allows to send commands to local and remote agents.
 */
public interface CommandDispatcher extends CommandRunnerBase
{
    /**
     * Creates command based on supplied RequestBuilder and target containers on which to run the command.
     *
     * @param requestBuilder - request builder
     * @param containers - set of target containers
     *
     * @return - command
     */
    public Command createContainerCommand( final RequestBuilder requestBuilder, final Set<Container> containers );

    /**
     * Creates command based on supplied set of ContainerRequestBuilders.
     *
     * @param requestBuilders - container request builders
     *
     * @return - command
     */
    public Command createContainerCommand( final Set<ContainerRequestBuilder> requestBuilders );
}
