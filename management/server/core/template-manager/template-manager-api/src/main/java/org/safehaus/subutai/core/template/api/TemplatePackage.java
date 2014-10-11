package org.safehaus.subutai.core.template.api;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;


/**
 * This class encapsulates the information about template packages for preparing templates in physical host.
 */
public class TemplatePackage implements Serializable
{
    private UUID peerId;
    private UUID environmentId;
    private UUID agentId;

    private List<SubutaiPackage> subutaiPackages;


    public TemplatePackage( UUID peerId, UUID environmentId, UUID agentId, List<SubutaiPackage> subutaiPackages )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.agentId = agentId;
        this.subutaiPackages = subutaiPackages;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }


    public UUID getAgentId()
    {
        return agentId;
    }


    public void setAgentId( final UUID agentId )
    {
        this.agentId = agentId;
    }


    public List<SubutaiPackage> getSubutaiPackages()
    {
        return subutaiPackages;
    }


    public void setSubutaiPackages( final List<SubutaiPackage> subutaiPackages )
    {
        this.subutaiPackages = subutaiPackages;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }
}
