package org.safehaus.subutai.core.container.api;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Created by timur on 9/13/14.
 */
public interface Container {

    /**
     * Execute command
     */
    public void execute( Command command );

    public void execute( RequestBuilder requestBuilder ) throws CommandException;

    public boolean start();

    public boolean stop();

    public boolean isConnected();

    public ContainerState getContainerState();

    public ContainerType getContainerType();

    /**
     * Destroy container.
     */
    public void destroy() throws ContainerDestroyException;

    public Set<Container> getLogicalContainers();

    org.safehaus.subutai.common.protocol.Agent getAgent();
}
