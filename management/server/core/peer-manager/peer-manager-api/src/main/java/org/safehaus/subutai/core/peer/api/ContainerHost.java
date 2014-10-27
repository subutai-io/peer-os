package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * ContainerHost class.
 */
public class ContainerHost extends SubutaiHost
{
    private UUID environmentId;
    private UUID ownerId;
    private String templateName;
    private String templateArch;


    public ContainerHost( final Agent agent )
    {
        super( agent );
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public UUID getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final UUID ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    @Override
    public boolean isConnected( Host host )
    {
        throw new UnsupportedOperationException( "Container host has no child host." );
    }
}
