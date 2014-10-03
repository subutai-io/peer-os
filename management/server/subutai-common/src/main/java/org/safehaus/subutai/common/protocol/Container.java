package org.safehaus.subutai.common.protocol;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;


/**
 * Created by timur on 9/20/14.
 */
public abstract class Container
{
    protected UUID agentId;
    protected UUID peerId;
    protected String hostname;
    protected String name;
    protected String description;
    protected ContainerState state;
    public abstract UUID getEnvironmentId();


    public UUID getAgentId()
    {
        return agentId;
    }


    public void setAgentId( final UUID agentId )
    {
        this.agentId = agentId;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    public ContainerState getState()
    {
        return state;
    }


    public void setState( final ContainerState state )
    {
        this.state = state;
    }


    public abstract boolean start() throws ContainerException;

    public abstract boolean stop() throws ContainerException;

    public abstract boolean isConnected() throws ContainerException;
}
