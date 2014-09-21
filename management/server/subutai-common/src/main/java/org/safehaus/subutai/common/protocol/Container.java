package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Created by timur on 9/20/14.
 */
public class Container
{
    private UUID agentId;
    private UUID peerId;
    private String hostname;
    private String name;
    private String description;


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
}
