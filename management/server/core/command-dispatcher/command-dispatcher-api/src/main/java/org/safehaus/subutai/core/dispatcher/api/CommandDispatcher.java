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

    public Command createContainerCommand( final RequestBuilder requestBuilder, final Set<Container> containers );

    public Command createContainerCommand( final Set<ContainerRequestBuilder> requestBuilders );
}
