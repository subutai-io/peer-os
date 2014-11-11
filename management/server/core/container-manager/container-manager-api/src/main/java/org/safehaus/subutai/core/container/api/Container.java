package org.safehaus.subutai.core.container.api;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Created by timur on 9/13/14.
 */
public interface Container
{

    /**
     * Execute command
     */
    public void execute( Command command );

    /**
     * Execute command
     */
    public void execute( RequestBuilder requestBuilder ) throws CommandException;

    /**
     * Starts LXC container
     *
     * @return true if LXC container started successfully.
     */
    public boolean start();

    /**
     * Stops LXC container
     *
     * @return true if LXC container stopped successfully.
     */
    public boolean stop();

    /**
     * Checks state of container
     *
     * @return true, if the container is alive
     */
    public boolean isConnected();

    /**
     * Returns state of container
     */
    public ContainerState getContainerState();

    /**
     * Returns type of container.
     *
     * @return ContainerType.PHYSICAL or ContainerType.LOGICAL
     */
    public ContainerType getContainerType();

    /**
     * Destroy container.
     */
    public void destroy() throws ContainerDestroyException;

    /**
     * Returns set of logical containers
     */
    public Set<Container> getLogicalContainers();

    /**
     * Returns Agent
     */
    Agent getAgent();
}
